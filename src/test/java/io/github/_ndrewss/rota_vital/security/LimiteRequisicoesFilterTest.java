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
        var filtro = new LimiteRequisicoesFilter(2, 1, tempo::get);
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
        var filtro = new LimiteRequisicoesFilter(60, 1);
        var interna = new MockHttpServletResponse();
        filtro.doFilter(new MockHttpServletRequest("GET", "/historico"), new MockHttpServletResponse(),
                (req, res) -> filtro.doFilter(new MockHttpServletRequest("GET", "/estatisticas/sequencial"),
                        interna, (req2, res2) -> { throw new AssertionError("não deve executar"); }));
        assertEquals(429, interna.getStatus());
    }

    /** Exceções da aplicação não deixam a capacidade ocupada permanentemente. */
    @Test
    void liberaVagaAposErro() throws Exception {
        var filtro = new LimiteRequisicoesFilter(60, 1);
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
        var filtro = new LimiteRequisicoesFilter(1, 1);
        for (String rota : new String[] {"/historico", "/actuator/health"}) {
            var resposta = new MockHttpServletResponse();
            filtro.doFilter(new MockHttpServletRequest("GET", rota), resposta, (req, res) -> {});
            assertEquals(200, resposta.getStatus());
        }
    }
}
