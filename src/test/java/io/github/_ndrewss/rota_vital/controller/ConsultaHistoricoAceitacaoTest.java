package io.github._ndrewss.rota_vital.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.github._ndrewss.rota_vital.repository.ExecucaoHistoricoRepository;

/**
 * Teste de aceitação da história H3 (consultar o histórico), no estilo Dado / Quando / Então.
 */
@SpringBootTest
class ConsultaHistoricoAceitacaoTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private ExecucaoHistoricoRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    void prepararBancoVazio() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        repository.deleteAll();
    }

    // H3 / T3.2
    @Test
    @DisplayName("Dado que rodei o sequencial e depois o paralelo, quando consulto /historico, então o paralelo (mais recente) vem primeiro")
    void historicoListaAMaisRecentePrimeiro() throws Exception {
        mockMvc.perform(get("/estatisticas/sequencial").param("tamanho", "500"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/estatisticas/paralelo").param("tamanho", "600").param("threads", "2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("PARALELO"))
                .andExpect(jsonPath("$[0].tamanho").value(600))
                .andExpect(jsonPath("$[0].threads").value(2))
                .andExpect(jsonPath("$[1].tipo").value("SEQUENCIAL"))
                .andExpect(jsonPath("$[1].tamanho").value(500))
                .andExpect(jsonPath("$[1].threads").value(1));
    }

    // H3 / T3.2
    @Test
    @DisplayName("Dado o histórico vazio, quando consulto /historico, então recebo uma lista vazia")
    void historicoVazioRetornaListaVazia() throws Exception {
        mockMvc.perform(get("/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
