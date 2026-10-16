package io.github._ndrewss.rota_vital.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Transforma exceções de validação em respostas HTTP claras.
 * Sem isto, um parâmetro inválido virava erro 500 (erro do servidor); com isto vira
 * 400 (erro de quem chamou) e um JSON {"erro": "..."} dizendo o que estava errado.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Converte a validação de parâmetros em HTTP 400 com uma mensagem em português. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(IllegalArgumentException e) {
        String mensagem = (e.getMessage() == null) ? "parâmetro inválido" : e.getMessage();
        return ResponseEntity.badRequest().body(Map.of("erro", mensagem));
    }
    /** Explica indisponibilidade por orçamento de armazenamento sem expor detalhes internos. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> indisponivel(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("erro", e.getReason() == null ? "serviço indisponível" : e.getReason()));
    }
}
