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
    @Test
    void sequencialValidoRegistraNoHistoricoComOTamanhoPedido() {
        Map<String, Object> resposta = controller.calcularSequencial(1_000);

        verify(historicoService).verificarCapacidade();
        verify(historicoService).registrarSequencial(eq(1_000), anyLong());
        assertTrue(resposta.containsKey("tempoGastoMs"));
    }

    // H2 / T2.3 — chamada válida manda gravar
    @Test
    void paraleloValidoRegistraNoHistoricoComTamanhoEThreads() {
        Map<String, Object> resposta = controller.calcularParalelo(1_000, 4);

        verify(historicoService).verificarCapacidade();
        verify(historicoService).registrarParalelo(eq(1_000), eq(4), anyLong());
        assertTrue(resposta.containsKey("tempoGastoMs"));
    }

    // H1 / T1.3 e H2 / T2.3 — tamanho inválido é recusado e nada é gravado
    @ParameterizedTest
    @ValueSource(ints = {0, -5, 1_000_001})
    void sequencialComTamanhoInvalidoLancaErroENaoRegistra(int tamanho) {
        assertThrows(IllegalArgumentException.class, () -> controller.calcularSequencial(tamanho));

        verify(historicoService, never()).registrarSequencial(anyInt(), anyLong());
    }

    // H1 / T1.3 e H2 / T2.3
    @ParameterizedTest
    @ValueSource(ints = {0, -5, 1_000_001})
    void paraleloComTamanhoInvalidoLancaErroENaoRegistra(int tamanho) {
        assertThrows(IllegalArgumentException.class, () -> controller.calcularParalelo(tamanho, 4));

        verify(historicoService, never()).registrarParalelo(anyInt(), anyInt(), anyLong());
    }

    // H1 / T1.2 e H2 / T2.3 — threads inválido é recusado e nada é gravado
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 33})
    void paraleloComThreadsInvalidoLancaErroENaoRegistra(int threads) {
        assertThrows(IllegalArgumentException.class, () -> controller.calcularParalelo(1_000, threads));

        verify(historicoService, never()).registrarParalelo(anyInt(), anyInt(), anyLong());
    }
}
