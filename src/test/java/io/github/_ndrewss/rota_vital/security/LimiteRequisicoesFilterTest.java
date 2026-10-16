package io.github._ndrewss.rota_vital.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Valida admissão, janela de tempo e liberação de vagas sem depender do relógio real. */
class LimiteRequisicoesFilterTest {
    /** O excesso recebe 429 e a janela seguinte volta a aceitar chamadas. */
    @Test
    void limitaTaxaERenovaJanela() throws Exception {
        AtomicLong tempo = new AtomicLong();
        var filtro = new LimiteRequisicoesFilter(2, 2, tempo::get);
        AtomicInteger executadas = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            var resposta = new MockHttpServletResponse();
            filtro.doFilter(new MockHttpServletRequest("GET", "/historico"), resposta,
                    (req, res) -> executadas.incrementAndGet());
            assertEquals(i < 2 ? 200 : 429, resposta.getStatus());
            if (i == 2) {
                assertEquals("60", resposta.getHeader("Retry-After"));
            }
        }
        assertEquals(2, executadas.get());
        tempo.set(TimeUnit.MINUTES.toNanos(1));
        var resposta = new MockHttpServletResponse();
        filtro.doFilter(new MockHttpServletRequest("GET", "/historico"), resposta,
                (req, res) -> executadas.incrementAndGet());
        assertEquals(200, resposta.getStatus());
        assertEquals(3, executadas.get());
    }

    /** Uma chamada simultânea além da capacidade é recusada antes da execução. */
    @Test
    void limitaConcorrencia() throws Exception {
        var filtro = new LimiteRequisicoesFilter(60, 2);
        var interna = new MockHttpServletResponse();
        filtro.doFilter(new MockHttpServletRequest("GET", "/historico"), new MockHttpServletResponse(),
                (req, res) -> filtro.doFilter(new MockHttpServletRequest("GET", "/estatisticas/sequencial"),
                        interna, (req2, res2) -> { throw new AssertionError("não deve executar"); }));
        assertEquals(429, interna.getStatus());
    }

    /** Exceções da aplicação não deixam a capacidade ocupada permanentemente. */
    @Test
    void liberaVagaAposErro() throws Exception {
        var filtro = new LimiteRequisicoesFilter(60, 2);
        assertThrows(ServletException.class, () -> filtro.doFilter(
                new MockHttpServletRequest("GET", "/historico"), new MockHttpServletResponse(),
                (req, res) -> { throw new ServletException("falha simulada"); }));
        var resposta = new MockHttpServletResponse();
        filtro.doFilter(new MockHttpServletRequest("GET", "/historico"), resposta, (req, res) -> {});
        assertEquals(200, resposta.getStatus());
    }

    /** O healthcheck permanece disponível mesmo após esgotar a cota da aplicação. */
    @Test
    void healthcheckNaoConsomeCota() throws Exception {
        var filtro = new LimiteRequisicoesFilter(1, 2);
        for (String rota : new String[] {"/historico", "/actuator/health"}) {
            var resposta = new MockHttpServletResponse();
            filtro.doFilter(new MockHttpServletRequest("GET", rota), resposta, (req, res) -> {});
            assertEquals(200, resposta.getStatus());
        }
    }
    /** Um IP que esgota a própria cota não bloqueia outro IP. */
    @Test
    void isolaCotasPorCliente() throws Exception {
        var filtro = new LimiteRequisicoesFilter(1, 2);
        assertEquals(200, chamar(filtro, "192.0.2.1", null).getStatus());
        assertEquals(429, chamar(filtro, "192.0.2.1", null).getStatus());
        assertEquals(200, chamar(filtro, "192.0.2.2", null).getStatus());
    }

    /** Cabeçalhos enviados pelo cliente não permitem trocar a identidade usada na cota. */
    @Test
    void naoConfiaEmCabecalhosDeIp() throws Exception {
        var filtro = new LimiteRequisicoesFilter(1, 2);
        assertEquals(200, chamar(filtro, "192.0.2.1", "203.0.113.1").getStatus());
        assertEquals(429, chamar(filtro, "192.0.2.1", "203.0.113.2").getStatus());
    }

    /** A tabela de clientes tem teto e só reaproveita cotas expiradas sem chamadas ativas. */
    @Test
    void limitaClientesSemEvictarCotasAtivas() throws Exception {
        AtomicLong tempo = new AtomicLong();
        var filtro = new LimiteRequisicoesFilter(2, 2, tempo::get, 2);
        assertEquals(200, chamar(filtro, "192.0.2.1", null).getStatus());
        assertEquals(200, chamar(filtro, "192.0.2.2", null).getStatus());
        assertEquals(429, chamar(filtro, "192.0.2.3", null).getStatus());
        assertEquals(200, chamar(filtro, "192.0.2.1", null).getStatus());
        assertEquals(429, chamar(filtro, "192.0.2.1", null).getStatus());
        tempo.set(TimeUnit.MINUTES.toNanos(1));
        assertEquals(200, chamar(filtro, "192.0.2.3", null).getStatus());
    }

    /** Um cliente não pode ocupar todas as vagas globais com chamadas simultâneas. */
    @Test
    void reservaConcorrenciaParaOutroCliente() throws Exception {
        var filtro = new LimiteRequisicoesFilter(60, 2);
        var primeira = new MockHttpServletRequest("GET", "/historico");
        primeira.setRemoteAddr("192.0.2.1");
        filtro.doFilter(primeira, new MockHttpServletResponse(), (req, res) -> {
            assertEquals(429, chamar(filtro, "192.0.2.1", null).getStatus());
            assertEquals(200, chamar(filtro, "192.0.2.2", null).getStatus());
        });
    }

    /** Preserva o teto global e não ocupa o mapa com IPs recusados por falta de vaga global. */
    @Test
    void preservaTetoGlobalDeConcorrencia() throws Exception {
        var filtro = new LimiteRequisicoesFilter(60, 2, System::nanoTime, 3);
        var primeira = new MockHttpServletRequest("GET", "/historico");
        primeira.setRemoteAddr("192.0.2.1");
        filtro.doFilter(primeira, new MockHttpServletResponse(), (req, res) -> {
            var segunda = new MockHttpServletRequest("GET", "/historico");
            segunda.setRemoteAddr("192.0.2.2");
            filtro.doFilter(segunda, new MockHttpServletResponse(), (req2, res2) -> {
                assertEquals(429, chamar(filtro, "192.0.2.3", null).getStatus());
            });
        });
        assertEquals(200, chamar(filtro, "192.0.2.4", null).getStatus());
    }

    /** A limpeza nunca remove a entrada usada pelo finally de uma chamada ainda ativa. */
    @Test
    void preservaClienteAtivoDuranteExpiracaoDaJanela() throws Exception {
        AtomicLong tempo = new AtomicLong();
        var filtro = new LimiteRequisicoesFilter(2, 2, tempo::get, 1);
        var pedido = new MockHttpServletRequest("GET", "/historico");
        pedido.setRemoteAddr("192.0.2.1");
        filtro.doFilter(pedido, new MockHttpServletResponse(), (req, res) -> {
            tempo.set(TimeUnit.MINUTES.toNanos(1));
            assertEquals(429, chamar(filtro, "192.0.2.2", null).getStatus());
        });
        tempo.addAndGet(TimeUnit.SECONDS.toNanos(1));
        assertEquals(200, chamar(filtro, "192.0.2.2", null).getStatus());
    }

    /** Rejeita configuração que impediria a reserva de capacidade para outros clientes. */
    @Test
    void exigeConcorrenciaGlobalDePeloMenosDois() {
        assertThrows(IllegalArgumentException.class, () -> new LimiteRequisicoesFilter(60, 1));
    }

    /** Envia uma chamada simulada com endereço de transporte e cabeçalhos opcionais. */
    private MockHttpServletResponse chamar(LimiteRequisicoesFilter filtro, String ip, String encaminhado)
            throws java.io.IOException, ServletException {
        var pedido = new MockHttpServletRequest("GET", "/historico");
        pedido.setRemoteAddr(ip);
        if (encaminhado != null) {
            pedido.addHeader("X-Forwarded-For", encaminhado);
            pedido.addHeader("Forwarded", "for=" + encaminhado);
        }
        var resposta = new MockHttpServletResponse();
        filtro.doFilter(pedido, resposta, (req, res) -> {});
        return resposta;
    }
}
