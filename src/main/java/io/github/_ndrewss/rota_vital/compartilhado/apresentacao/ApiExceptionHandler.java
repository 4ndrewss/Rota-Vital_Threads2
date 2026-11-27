package io.github._ndrewss.rota_vital.compartilhado.apresentacao;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaJaReservada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaNaoEncontrada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaVencida;

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

    /** JSON malformado ou com data em formato inválido no corpo vira 400 (só afeta endpoints com corpo). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("erro", "corpo da requisição inválido (use JSON; datas no formato AAAA-MM-DD)"));
    }

    /** Estoque: bolsa inexistente vira 404. */
    @ExceptionHandler(BolsaNaoEncontrada.class)
    public ResponseEntity<Map<String, String>> bolsaNaoEncontrada(BolsaNaoEncontrada e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    /** Estoque: bolsa que já tem reserva vira 409 (conflito com o estado atual). */
    @ExceptionHandler(BolsaJaReservada.class)
    public ResponseEntity<Map<String, String>> bolsaJaReservada(BolsaJaReservada e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }

    /** Estoque: bolsa vencida vira 422 (pedido bem formado, mas a regra de negócio impede). */
    @ExceptionHandler(BolsaVencida.class)
    public ResponseEntity<Map<String, String>> bolsaVencida(BolsaVencida e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(Map.of("erro", e.getMessage()));
    }
}
