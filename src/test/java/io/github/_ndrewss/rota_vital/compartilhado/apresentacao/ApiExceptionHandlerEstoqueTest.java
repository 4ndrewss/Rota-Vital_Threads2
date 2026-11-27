package io.github._ndrewss.rota_vital.compartilhado.apresentacao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaJaReservada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaNaoEncontrada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaVencida;

/** Cartão #12: exceções do Estoque viram HTTP no formato {"erro": "..."} já usado pelo handler. */
class ApiExceptionHandlerEstoqueTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();
    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void bolsaNaoEncontradaDevolve404() {
        ResponseEntity<Map<String, String>> r = handler.bolsaNaoEncontrada(new BolsaNaoEncontrada(id));

        assertEquals(404, r.getStatusCode().value());
        assertEquals(Map.of("erro", "bolsa não encontrada: " + id), r.getBody());
    }

    @Test
    void bolsaJaReservadaDevolve409() {
        ResponseEntity<Map<String, String>> r = handler.bolsaJaReservada(new BolsaJaReservada(id));

        assertEquals(409, r.getStatusCode().value());
        assertEquals(Map.of("erro", "bolsa já reservada: " + id), r.getBody());
    }

    @Test
    void bolsaVencidaDevolve422() {
        ResponseEntity<Map<String, String>> r = handler.bolsaVencida(new BolsaVencida(id));

        assertEquals(422, r.getStatusCode().value());
        assertEquals(Map.of("erro", "bolsa vencida: " + id), r.getBody());
    }
}
