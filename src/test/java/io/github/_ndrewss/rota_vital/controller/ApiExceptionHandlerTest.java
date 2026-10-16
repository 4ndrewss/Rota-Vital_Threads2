package io.github._ndrewss.rota_vital.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * Testes unitários da H1 / T1.2: o tratador de erros chamado direto, sem subir Spring.
 * Obs.: o código já existia, então estes testes nascem verdes (testes de caracterização).
 */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    // H1 / T1.2
    @Test
    void parametroInvalidoDevolve400ComAMensagemDoErro() {
        ResponseEntity<Map<String, String>> resposta =
                handler.parametroInvalido(new IllegalArgumentException("threads deve ser maior que zero"));

        assertEquals(400, resposta.getStatusCode().value());
        assertEquals(Map.of("erro", "threads deve ser maior que zero"), resposta.getBody());
    }

    // H1 / T1.2
    @Test
    void parametroInvalidoSemMensagemUsaTextoPadrao() {
        ResponseEntity<Map<String, String>> resposta = handler.parametroInvalido(new IllegalArgumentException());

        assertEquals(400, resposta.getStatusCode().value());
        assertEquals(Map.of("erro", "parâmetro inválido"), resposta.getBody());
    }

    // H3 — histórico cheio vira 503 mantendo a mensagem
    @Test
    void indisponivelMantemOStatusEAMensagem() {
        ResponseEntity<Map<String, String>> resposta = handler.indisponivel(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "histórico cheio"));

        assertEquals(503, resposta.getStatusCode().value());
        assertEquals(Map.of("erro", "histórico cheio"), resposta.getBody());
    }

    @Test
    void indisponivelSemMotivoUsaTextoPadrao() {
        ResponseEntity<Map<String, String>> resposta = handler.indisponivel(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));

        assertEquals(Map.of("erro", "serviço indisponível"), resposta.getBody());
    }
}
