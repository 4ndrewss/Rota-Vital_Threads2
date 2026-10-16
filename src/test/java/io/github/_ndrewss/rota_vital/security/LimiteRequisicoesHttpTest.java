package io.github._ndrewss.rota_vital.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Confere que o filtro está registrado no servidor HTTP real da aplicação. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "rota.limites.requisicoes-por-minuto=2",
        "management.server.port=0",
        "management.server.address=127.0.0.1",
        "management.endpoints.web.exposure.include=health",
        "management.endpoint.health.show-components=always",
        "spring.datasource.url=jdbc:h2:mem:limite-http"
})
class LimiteRequisicoesHttpTest {
    @LocalServerPort
    private int porta;
    @LocalManagementPort
    private int portaSaude;
    private int encaminhado;

    /** A porta pública não expõe saúde; a sonda interna continua disponível após HTTP 429. */
    @Test
    void limitaHttpSemBloquearHealthcheck() throws Exception {
        try (var cliente = HttpClient.newHttpClient()) {
            assertTrue(porta != portaSaude);
            assertEquals(404, consultar(cliente, porta, "/actuator/health").statusCode());
            assertEquals(200, consultar(cliente, porta, "/historico").statusCode());
            var excesso = consultar(cliente, porta, "/historico");
            assertEquals(429, excesso.statusCode());
            assertTrue(excesso.body().contains("erro"));
            int espera = Integer.parseInt(excesso.headers().firstValue("Retry-After").orElseThrow());
            assertTrue(espera >= 1 && espera <= 60);
            assertEquals(429, consultar(cliente, porta, "/actuator/health").statusCode());
            for (int i = 0; i < 3; i++) {
                var saude = consultar(cliente, portaSaude, "/actuator/health");
                assertEquals(200, saude.statusCode());
                assertTrue(saude.body().contains("\"db\":{\"status\":\"UP\"}"));
                assertTrue(saude.headers().firstValue("Retry-After").isEmpty());
            }
            assertEquals(404, consultar(cliente, portaSaude, "/historico").statusCode());
        }
    }

    /** Envia uma requisição ao servidor de teste com tempo de espera limitado. */
    private HttpResponse<String> consultar(HttpClient cliente, int portaDestino, String caminho) throws Exception {
        var pedido = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + portaDestino + caminho))
                .header("X-Forwarded-For", "203.0.113." + ++encaminhado)
                .header("Forwarded", "for=203.0.113." + encaminhado)
                .timeout(Duration.ofSeconds(10)).GET().build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }
}
