package io.github._ndrewss.rota_vital.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github._ndrewss.rota_vital.service.EstatisticaService;
import io.github._ndrewss.rota_vital.service.HistoricoService;

/**
 * Testes unitários do controller (H1 / T1.3 e H2 / T2.3): o histórico é um dublê (mock), então
 * dá para conferir se o controller mandou gravar, ou não, sem banco e sem subir Spring.
 * Obs.: o código já existia, então estes testes nascem verdes (testes de caracterização).
 */
class EstatisticaControllerTest {

    private final HistoricoService historicoService = mock(HistoricoService.class);
    private final EstatisticaController controller =
            new EstatisticaController(new EstatisticaService(), historicoService);

    // H2 / T2.3 — chamada válida manda gravar
    /** Confere que a execução sequencial solicita a gravação e retorna o tempo medido. */
    @Test
    void sequencialValidoRegistraNoHistoricoComOTamanhoPedido() {
        Map<String, Object> resposta = controller.calcularSequencial(1_000);

        verify(historicoService).registrarSequencial(eq(1_000), anyLong());
        assertTrue(resposta.containsKey("tempoGastoMs"));
    }

    // H2 / T2.3 — chamada válida manda gravar
    /** Confere que o paralelo envia tamanho, threads e tempo ao serviço de histórico. */
    @Test
    void paraleloValidoRegistraNoHistoricoComTamanhoEThreads() {
        Map<String, Object> resposta = controller.calcularParalelo(1_000, 4);

        verify(historicoService).registrarParalelo(eq(1_000), eq(4), anyLong());
        assertTrue(resposta.containsKey("tempoGastoMs"));
    }

    // H1 / T1.3 e H2 / T2.3 — tamanho inválido é recusado e nada é gravado
    /** Garante que tamanho inválido interrompe o sequencial antes da gravação. */
    @ParameterizedTest
    @ValueSource(ints = {0, -5, 1_000_001})
    void sequencialComTamanhoInvalidoLancaErroENaoRegistra(int tamanho) {
        assertThrows(IllegalArgumentException.class, () -> controller.calcularSequencial(tamanho));

        verify(historicoService, never()).registrarSequencial(anyInt(), anyLong());
    }

    // H1 / T1.3 e H2 / T2.3
    /** Garante que tamanho inválido interrompe o paralelo antes da gravação. */
    @ParameterizedTest
    @ValueSource(ints = {0, -5, 1_000_001})
    void paraleloComTamanhoInvalidoLancaErroENaoRegistra(int tamanho) {
        assertThrows(IllegalArgumentException.class, () -> controller.calcularParalelo(tamanho, 4));

        verify(historicoService, never()).registrarParalelo(anyInt(), anyInt(), anyLong());
    }

    // H1 / T1.2 e H2 / T2.3 — threads inválido é recusado e nada é gravado
    /** Garante que threads inválidas impedem o processamento e o registro. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 33})
    void paraleloComThreadsInvalidoLancaErroENaoRegistra(int threads) {
        assertThrows(IllegalArgumentException.class, () -> controller.calcularParalelo(1_000, threads));

        verify(historicoService, never()).registrarParalelo(anyInt(), anyInt(), anyLong());
    }
    /** Falhas de gravação não derrubam os resultados nem expõem a mensagem interna. */
    @Test
    void falhaDeGravacaoNaoImpedeCalculo() {
        org.mockito.Mockito.doThrow(new org.springframework.dao.DataAccessResourceFailureException("detalhe interno"))
                .when(historicoService).registrarSequencial(anyInt(), anyLong());
        var resposta = controller.calcularSequencial(10);
        org.junit.jupiter.api.Assertions.assertEquals(false, resposta.get("historicoRegistrado"));
        assertTrue(resposta.containsKey("resultado"));
        org.junit.jupiter.api.Assertions.assertFalse(resposta.toString().contains("detalhe interno"));
    }

    /** O paralelo também continua disponível se o orçamento de gravação estiver esgotado. */
    @Test
    void historicoCheioNaoImpedeParalelo() {
        org.mockito.Mockito.doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "histórico cheio"))
                .when(historicoService).registrarParalelo(anyInt(), anyInt(), anyLong());
        var resposta = controller.calcularParalelo(10, 2);
        org.junit.jupiter.api.Assertions.assertEquals(false, resposta.get("historicoRegistrado"));
        assertTrue(resposta.containsKey("resultado"));
    }
}
