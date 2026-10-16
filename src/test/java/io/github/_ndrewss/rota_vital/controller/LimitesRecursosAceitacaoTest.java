package io.github._ndrewss.rota_vital.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.github._ndrewss.rota_vital.repository.ExecucaoHistoricoRepository;

/** Exercita os limites HTTP e o orçamento de registros usando H2 de verdade. */
@SpringBootTest(properties = "rota.historico.max-registros=3")
class LimitesRecursosAceitacaoTest {
    @Autowired
    private WebApplicationContext contexto;
    @Autowired
    private ExecucaoHistoricoRepository repository;
    private MockMvc mockMvc;

    /** Isola cada cenário com um histórico vazio. */
    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        repository.deleteAll();
    }

    /** Verifica que o teto de armazenamento preserva os registros já existentes. */
    @Test
    void concluiCalculoQuandoHistoricoEstaCheio() throws Exception {
        for (int tamanho = 1; tamanho <= 3; tamanho++) {
            mockMvc.perform(get("/estatisticas/sequencial").param("tamanho", "" + tamanho))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.historicoRegistrado").value(true));
        }
        mockMvc.perform(get("/estatisticas/sequencial").param("tamanho", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historicoRegistrado").value(false))
                .andExpect(jsonPath("$.aviso").exists())
                .andExpect(jsonPath("$.resultado").exists());
        mockMvc.perform(get("/estatisticas/paralelo").param("tamanho", "4").param("threads", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historicoRegistrado").value(false));
        assertEquals(3, repository.count());
        mockMvc.perform(get("/historico").param("limite", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tamanho").value(3))
                .andExpect(jsonPath("$[1].tamanho").value(2));
        mockMvc.perform(get("/historico").param("limite", "2").param("pagina", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tamanho").value(1));
    }

    /** Rejeita cargas e números de threads excessivos antes de gerar os dados. */
    @Test
    void rejeitaCargasAcimaDoLimiteSemGravar() throws Exception {
        mockMvc.perform(get("/estatisticas/sequencial").param("tamanho", "1000001"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/estatisticas/paralelo").param("tamanho", "1000000").param("threads", "33"))
                .andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }

    /** Impede páginas sem limite, negativas ou com deslocamento excessivo. */
    @Test
    void rejeitaPaginacaoInvalida() throws Exception {
        for (String limite : new String[] {"0", "-1", "101"}) {
            mockMvc.perform(get("/historico").param("limite", limite))
                    .andExpect(status().isBadRequest());
        }
        for (String pagina : new String[] {"-1", "10001"}) {
            mockMvc.perform(get("/historico").param("pagina", pagina))
                    .andExpect(status().isBadRequest());
        }
    }
    /** A consulta sem parâmetros continua limitada mesmo com muitos registros antigos. */
    @Test
    void paginaPadraoNaoCarregaTodoHistorico() throws Exception {
        for (int i = 1; i <= 21; i++) {
            repository.save(new io.github._ndrewss.rota_vital.model.ExecucaoHistorico("SEQUENCIAL", i, 1, 0));
        }
        mockMvc.perform(get("/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20))
                .andExpect(jsonPath("$[0].tamanho").value(21));
    }

    /** Escritas concorrentes respeitam o teto inclusive ao disputar a última vaga. */
    @Test
    void gravacoesConcorrentesNaoUltrapassamOrcamento() throws Exception {
        var service = contexto.getBean(io.github._ndrewss.rota_vital.service.HistoricoService.class);
        var inicio = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(6)) {
            var tarefas = new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
            for (int i = 0; i < 6; i++) {
                tarefas.add(executor.submit(() -> {
                    inicio.await();
                    try {
                        service.registrarSequencial(1, 0);
                        return true;
                    } catch (org.springframework.web.server.ResponseStatusException e) {
                        assertEquals(503, e.getStatusCode().value());
                        return false;
                    }
                }));
            }
            inicio.countDown();
            int aceitas = 0;
            for (var tarefa : tarefas) {
                if (tarefa.get(10, java.util.concurrent.TimeUnit.SECONDS)) {
                    aceitas++;
                }
            }
            assertEquals(3, aceitas);
            assertEquals(3, repository.count());
        }
    }
}
