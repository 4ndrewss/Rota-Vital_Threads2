package io.github._ndrewss.rota_vital.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Limita cada IP de transporte e preserva um teto global de concorrência por instância. */
@Component
public class LimiteRequisicoesFilter extends OncePerRequestFilter {
    private static final long MINUTO = TimeUnit.MINUTES.toNanos(1);
    private final int porMinuto;
    private final int simultaneasPorCliente;
    private final int maxClientes;
    private final Semaphore vagas;
    private final LongSupplier relogio;
    private final Map<String, Cota> clientes = new HashMap<>();
    private long ultimaLimpeza;

    /** Configura a cota por IP e pelo menos duas vagas globais para permitir isolamento. */
    @Autowired
    public LimiteRequisicoesFilter(
            @Value("${rota.limites.requisicoes-por-minuto:60}") int porMinuto,
            @Value("${rota.limites.requisicoes-simultaneas:2}") int simultaneas) {
        this(porMinuto, simultaneas, System::nanoTime);
    }

    /** Permite testar as janelas com um relógio monotônico controlado. */
    LimiteRequisicoesFilter(int porMinuto, int simultaneas, LongSupplier relogio) {
        this(porMinuto, simultaneas, relogio, 4096);
    }

    /** Define um orçamento finito de clientes para impedir crescimento ilimitado do mapa. */
    LimiteRequisicoesFilter(int porMinuto, int simultaneas, LongSupplier relogio, int maxClientes) {
        if (porMinuto <= 0 || simultaneas < 2 || maxClientes <= 0) {
            throw new IllegalArgumentException("cota e clientes devem ser positivos; simultâneas deve ser no mínimo 2");
        }
        this.porMinuto = porMinuto;
        this.simultaneasPorCliente = Math.max(1, simultaneas / 2);
        this.maxClientes = maxClientes;
        this.vagas = new Semaphore(simultaneas);
        this.relogio = relogio;
        this.ultimaLimpeza = relogio.getAsLong();
    }

    /** Mantém a sonda de saúde disponível independentemente das cotas da aplicação. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return (request.getContextPath() + "/actuator/health").equals(request.getRequestURI());
    }

    /** Usa o endereço da conexão, sem confiar em Forwarded ou X-Forwarded-For enviados pelo cliente. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        String cliente = request.getRemoteAddr();
        if (cliente == null) {
            cliente = "desconhecido";
        }
        int espera = admitir(cliente);
        if (espera > 0) {
            recusar(response, espera);
            return;
        }
        try {
            chain.doFilter(request, response);
        } finally {
            liberar(cliente);
        }
    }

    /** Admite atomicamente pelas cotas do cliente e pelas vagas globais, sem consumir cota de outro IP. */
    private synchronized int admitir(String cliente) {
        long agora = relogio.getAsLong();
        Cota cota = clientes.get(cliente);
        if (cota == null) {
            if (vagas.availablePermits() == 0) {
                return 1;
            }
            if (clientes.size() >= maxClientes && agora - ultimaLimpeza >= TimeUnit.SECONDS.toNanos(1)) {
                clientes.values().removeIf(valor -> valor.emAndamento == 0 && agora - valor.inicio >= MINUTO);
                ultimaLimpeza = agora;
            }
            if (clientes.size() >= maxClientes) {
                return 60;
            }
            cota = new Cota(agora);
            clientes.put(cliente, cota);
        }
        if (agora - cota.inicio >= MINUTO) {
            cota.inicio = agora;
            cota.recebidas = 0;
        }
        if (cota.recebidas >= porMinuto) {
            return 60;
        }
        if (cota.emAndamento >= simultaneasPorCliente || !vagas.tryAcquire()) {
            return 1;
        }
        cota.recebidas++;
        cota.emAndamento++;
        return 0;
    }

    /** Devolve as vagas mesmo após falhas; cotas com chamadas ativas nunca são removidas. */
    private synchronized void liberar(String cliente) {
        clientes.get(cliente).emAndamento--;
        vagas.release();
    }

    /** Devolve HTTP 429 sem executar a aplicação nem consultar o banco. */
    private void recusar(HttpServletResponse response, int espera) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", Integer.toString(espera));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"limite temporário de requisições atingido\"}");
    }

    /** Estado de uma janela por cliente, sempre protegido pelo monitor do filtro. */
    private static final class Cota {
        private long inicio;
        private int recebidas;
        private int emAndamento;

        /** Inicia a janela sem chamadas consumidas ou em andamento. */
        private Cota(long inicio) {
            this.inicio = inicio;
        }
    }
}
