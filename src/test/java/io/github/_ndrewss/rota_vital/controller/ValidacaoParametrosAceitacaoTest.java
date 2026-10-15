package io.github._ndrewss.rota_vital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Teste de aceitação da história H1 (validar parâmetros), escrito no estilo
 * Dado / Quando / Então. Sobe a aplicação inteira (com H2) e chama a API como um cliente chamaria.
 */
@SpringBootTest
class ValidacaoParametrosAceitacaoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mockMvc;

    @BeforeEach
    void montarMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    // H1 / T1.2
    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    @DisplayName("Dado threads <= 0, quando chamo /estatisticas/paralelo, então recebo 400 com a mensagem de erro")
    void threadsInvalidoRetorna400(int threads) throws Exception {
        mockMvc.perform(get("/estatisticas/paralelo")
                        .param("tamanho", "1000")
                        .param("threads", String.valueOf(threads)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("threads deve ser maior que zero"));
    }

    // H1 / T1.3
    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    @DisplayName("Dado tamanho <= 0, quando chamo /estatisticas/sequencial, então recebo 400 com a mensagem de erro")
    void tamanhoInvalidoNoSequencialRetorna400(int tamanho) throws Exception {
        mockMvc.perform(get("/estatisticas/sequencial")
                        .param("tamanho", String.valueOf(tamanho)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("tamanho deve ser maior que zero"));
    }

    // H1 / T1.3
    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    @DisplayName("Dado tamanho <= 0, quando chamo /estatisticas/paralelo, então recebo 400 com a mensagem de erro")
    void tamanhoInvalidoNoParaleloRetorna400(int tamanho) throws Exception {
        mockMvc.perform(get("/estatisticas/paralelo")
                        .param("tamanho", String.valueOf(tamanho))
                        .param("threads", "4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("tamanho deve ser maior que zero"));
    }
}
