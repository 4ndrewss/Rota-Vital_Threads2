package io.github._ndrewss.rota_vital.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Transforma exceções de validação em respostas HTTP claras.
 * Sem isto, um parâmetro inválido virava erro 500 (erro do servidor); com isto vira
 * 400 (erro de quem chamou) e um JSON {"erro": "..."} dizendo o que estava errado.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(IllegalArgumentException e) {
        String mensagem = (e.getMessage() == null) ? "parâmetro inválido" : e.getMessage();
        return ResponseEntity.badRequest().body(Map.of("erro", mensagem));
    }
}
