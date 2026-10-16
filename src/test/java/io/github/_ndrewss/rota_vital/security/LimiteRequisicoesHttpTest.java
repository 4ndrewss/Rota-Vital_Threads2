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
import org.springframework.boot.test.web.server.LocalServerPort;

/** Confere que o filtro está registrado no servidor HTTP real da aplicação. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "rota.limites.requisicoes-por-minuto=1",
        "spring.datasource.url=jdbc:h2:mem:limite-http"
})
class LimiteRequisicoesHttpTest {
    @LocalServerPort
    private int porta;
    private int encaminhado;

    /** A cota produz 429 via HTTP e o healthcheck continua disponível. */
    @Test
    void limitaHttpSemBloquearHealthcheck() throws Exception {
        try (var cliente = HttpClient.newHttpClient()) {
            assertEquals(200, consultar(cliente, "/historico").statusCode());
            var excesso = consultar(cliente, "/historico");
            assertEquals(429, excesso.statusCode());
            assertTrue(excesso.body().contains("erro"));
            assertEquals("60", excesso.headers().firstValue("Retry-After").orElseThrow());
            assertEquals(200, consultar(cliente, "/actuator/health").statusCode());
        }
    }

    /** Envia uma requisição ao servidor de teste com tempo de espera limitado. */
    private HttpResponse<String> consultar(HttpClient cliente, String caminho) throws Exception {
        var pedido = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
                .header("X-Forwarded-For", "203.0.113." + ++encaminhado)
                .header("Forwarded", "for=203.0.113." + encaminhado)
                .timeout(Duration.ofSeconds(10)).GET().build();
        return cliente.send(pedido, HttpResponse.BodyHandlers.ofString());
    }
}
