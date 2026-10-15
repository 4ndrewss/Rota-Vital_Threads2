package io.github._ndrewss.rota_vital.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;
import io.github._ndrewss.rota_vital.repository.ExecucaoHistoricoRepository;

/**
 * Teste de aceitação da história H2 (registrar cada execução), no estilo Dado / Quando / Então.
 * Também é teste de integração: a chamada passa por controller, service, repositório e H2 de verdade.
 */
@SpringBootTest
class HistoricoAceitacaoTest {

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

    // H2 / T2.3
    @Test
    @DisplayName("Dado o histórico vazio, quando chamo /estatisticas/paralelo com parâmetros válidos, então 1 execução PARALELO fica gravada")
    void chamadaParaleloValidaGravaNoHistorico() throws Exception {
        mockMvc.perform(get("/estatisticas/paralelo")
                        .param("tamanho", "1000")
                        .param("threads", "4"))
                .andExpect(status().isOk());

        List<ExecucaoHistorico> gravadas = repository.findAll();
        assertEquals(1, gravadas.size());
        ExecucaoHistorico execucao = gravadas.get(0);
        assertEquals("PARALELO", execucao.getTipo());
        assertEquals(1000, execucao.getTamanho());
        assertEquals(4, execucao.getThreads());
    }

    // H2 / T2.3
    @Test
    @DisplayName("Dado o histórico vazio, quando chamo /estatisticas/sequencial com tamanho válido, então 1 execução SEQUENCIAL com 1 thread fica gravada")
    void chamadaSequencialValidaGravaNoHistorico() throws Exception {
        mockMvc.perform(get("/estatisticas/sequencial")
                        .param("tamanho", "1000"))
                .andExpect(status().isOk());

        List<ExecucaoHistorico> gravadas = repository.findAll();
        assertEquals(1, gravadas.size());
        ExecucaoHistorico execucao = gravadas.get(0);
        assertEquals("SEQUENCIAL", execucao.getTipo());
        assertEquals(1000, execucao.getTamanho());
        assertEquals(1, execucao.getThreads());
    }

    // H2 / T2.3 — este teste já passa antes da implementação: serve de trava para o futuro,
    // garantindo que quando o registro for ligado, chamadas com erro continuem sem gravar nada.
    @Test
    @DisplayName("Dado o histórico vazio, quando chamo a API com threads inválido, então recebo 400 e nada é gravado")
    void chamadaInvalidaNaoGravaNoHistorico() throws Exception {
        mockMvc.perform(get("/estatisticas/paralelo")
                        .param("tamanho", "1000")
                        .param("threads", "0"))
                .andExpect(status().isBadRequest());

        assertEquals(0, repository.count());
    }
}
