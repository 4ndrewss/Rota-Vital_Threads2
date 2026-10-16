package io.github._ndrewss.rota_vital.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.server.ResponseStatusException;

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

    // H3 / T3.1 — nasce verde: o código já existia (teste de caracterização)
    /** A consulta pede ao repositório só a página solicitada, não o histórico inteiro. */
    @Test
    void listarPedeAoRepositorioApenasAPaginaSolicitada() {
        service.listar(2, 50);

        verify(repository).findAllByOrderByIdDesc(PageRequest.of(2, 50));
    }

    // H3 / T3.1 — nasce verde
    /** Limite fora de 1 a 100 é recusado antes de consultar o banco. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 101})
    void listarRejeitaLimiteForaDaFaixa(int limite) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.listar(0, limite));

        assertEquals("limite deve estar entre 1 e 100", erro.getMessage());
        verify(repository, never()).findAllByOrderByIdDesc(any(Pageable.class));
    }

    // H3 / T3.1 — nasce verde
    /** Página fora de 0 a 10000 é recusada antes de consultar o banco. */
    @ParameterizedTest
    @ValueSource(ints = {-1, 10_001})
    void listarRejeitaPaginaForaDaFaixa(int pagina) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.listar(pagina, 20));

        assertEquals("pagina deve estar entre 0 e 10000", erro.getMessage());
        verify(repository, never()).findAllByOrderByIdDesc(any(Pageable.class));
    }

    // H2 — nasce verde: histórico cheio recusa novas execuções com 503
    /** Com o teto de registros atingido, a verificação recusa com 503. */
    @Test
    void verificarCapacidadeRecusaComStatus503QuandoHistoricoEstaCheio() {
        var servicoComTeto = new HistoricoService(repository, mock(PlatformTransactionManager.class), 3);
        when(repository.count()).thenReturn(3L);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                servicoComTeto::verificarCapacidade);

        assertEquals(503, erro.getStatusCode().value());
    }

    // H2 — nasce verde
    /** Com espaço sobrando no histórico, a verificação deixa a execução seguir. */
    @Test
    void verificarCapacidadeAceitaQuandoAindaHaEspaco() {
        var servicoComTeto = new HistoricoService(repository, mock(PlatformTransactionManager.class), 3);
        when(repository.count()).thenReturn(2L);

        assertDoesNotThrow(servicoComTeto::verificarCapacidade);
    }

    // H2 — nasce verde
    /** O teto configurado precisa ficar entre 1 e 10000. */
    @ParameterizedTest
    @ValueSource(ints = {0, 10_001})
    void construtorRejeitaTetoDeRegistrosForaDaFaixa(int teto) {
        assertThrows(IllegalArgumentException.class,
                () -> new HistoricoService(repository, mock(PlatformTransactionManager.class), teto));
    }

    /** Captura a entidade passada ao repositório sem depender de um banco real. */
    private ExecucaoHistorico capturarGravado() {
        ArgumentCaptor<ExecucaoHistorico> captor = ArgumentCaptor.forClass(ExecucaoHistorico.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }
}
