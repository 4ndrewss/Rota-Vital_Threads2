package io.github._ndrewss.rota_vital.security;

import java.io.IOException;
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

/** Limita a taxa e a concorrência por instância, sem manter um mapa ilimitado de clientes. */
@Component
public class LimiteRequisicoesFilter extends OncePerRequestFilter {
    private final int porMinuto;
    private final Semaphore vagas;
    private final LongSupplier relogio;
    private long inicioJanela;
    private int recebidas;

    /** Configura a cota global da instância e a quantidade de chamadas simultâneas. */
    @Autowired
    public LimiteRequisicoesFilter(
            @Value("${rota.limites.requisicoes-por-minuto:60}") int porMinuto,
            @Value("${rota.limites.requisicoes-simultaneas:2}") int simultaneas) {
        this(porMinuto, simultaneas, System::nanoTime);
    }

    /** Permite testar as janelas com um relógio monotônico controlado. */
    LimiteRequisicoesFilter(int porMinuto, int simultaneas, LongSupplier relogio) {
        if (porMinuto <= 0 || simultaneas <= 0) {
            throw new IllegalArgumentException("os limites de requisições devem ser positivos");
        }
        this.porMinuto = porMinuto;
        this.vagas = new Semaphore(simultaneas);
        this.relogio = relogio;
        this.inicioJanela = relogio.getAsLong();
    }

    /** Mantém a sonda de saúde disponível independentemente da cota da aplicação. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return (request.getContextPath() + "/actuator/health").equals(request.getRequestURI());
    }

    /** Recusa excesso com 429 e sempre devolve a vaga após a cadeia de filtros. */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!admitirNaJanela()) {
            recusar(response, "limite de requisições por minuto atingido", 60);
            return;
        }
        if (!vagas.tryAcquire()) {
            recusar(response, "limite de requisições simultâneas atingido", 1);
            return;
        }
        try {
            chain.doFilter(request, response);
        } finally {
            vagas.release();
        }
    }

    /** Conta chamadas de forma atômica em uma janela fixa de um minuto. */
    private synchronized boolean admitirNaJanela() {
        long agora = relogio.getAsLong();
        if (agora - inicioJanela >= TimeUnit.MINUTES.toNanos(1)) {
            inicioJanela = agora;
            recebidas = 0;
        }
        if (recebidas >= porMinuto) {
            return false;
        }
        recebidas++;
        return true;
    }

    /** Devolve um erro JSON sem iniciar o processamento nem acessar o banco. */
    private void recusar(HttpServletResponse response, String mensagem, int espera) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", Integer.toString(espera));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
