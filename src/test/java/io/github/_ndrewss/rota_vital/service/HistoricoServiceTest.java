package io.github._ndrewss.rota_vital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;
import io.github._ndrewss.rota_vital.repository.ExecucaoHistoricoRepository;

/**
 * Testes unitários da H2: o repositório é um "dublê" (mock), então aqui não tem banco.
 * O que se confere é o QUE o service manda gravar.
 */
class HistoricoServiceTest {

    private final ExecucaoHistoricoRepository repository = mock(ExecucaoHistoricoRepository.class);
    private final HistoricoService service = new HistoricoService(repository, mock(PlatformTransactionManager.class), 10_000);

    // H2 / T2.1
    /** Verifica os metadados e o tempo enviados ao repositório para o sequencial. */
    @Test
    void registrarSequencialGravaTipoSequencialComUmaThread() {
        service.registrarSequencial(100_000, 42L);

        ExecucaoHistorico gravado = capturarGravado();
        assertEquals("SEQUENCIAL", gravado.getTipo());
        assertEquals(1, gravado.getThreads());
        assertEquals(100_000, gravado.getTamanho());
        assertEquals(42L, gravado.getTempoMs());
        assertNotNull(gravado.getCriadoEm());
    }

    // H2 / T2.2
    /** Verifica os metadados e as threads enviados ao repositório para o paralelo. */
    @Test
    void registrarParaleloGravaTipoParaleloComONumeroDeThreadsPedido() {
        service.registrarParalelo(1_000_000, 8, 17L);

        ExecucaoHistorico gravado = capturarGravado();
        assertEquals("PARALELO", gravado.getTipo());
        assertEquals(8, gravado.getThreads());
        assertEquals(1_000_000, gravado.getTamanho());
        assertEquals(17L, gravado.getTempoMs());
        assertNotNull(gravado.getCriadoEm());
    }

    /** Captura a entidade passada ao repositório sem depender de um banco real. */
    private ExecucaoHistorico capturarGravado() {
        ArgumentCaptor<ExecucaoHistorico> captor = ArgumentCaptor.forClass(ExecucaoHistorico.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
}
