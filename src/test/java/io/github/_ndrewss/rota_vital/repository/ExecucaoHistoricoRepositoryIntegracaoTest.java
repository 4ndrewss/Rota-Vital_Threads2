package io.github._ndrewss.rota_vital.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;

/**
 * Teste de integração com o banco: grava e lê de verdade no H2 (em memória),
 * sem dublês. Garante que a consulta SQL gerada pelo Spring Data faz o que o nome promete.
 */
@SpringBootTest
class ExecucaoHistoricoRepositoryIntegracaoTest {

    @Autowired
    private ExecucaoHistoricoRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    // H3 / T3.1
    @Test
    void devolveExecucoesDaMaisNovaParaAMaisAntiga() {
        repository.save(new ExecucaoHistorico("SEQUENCIAL", 100, 1, 10L));
        repository.save(new ExecucaoHistorico("PARALELO", 200, 2, 20L));
        repository.save(new ExecucaoHistorico("PARALELO", 300, 4, 30L));

        List<ExecucaoHistorico> lista = repository.findAllByOrderByIdDesc();

        // a ordem é conferida pelo tamanho, que é diferente em cada linha gravada
        assertEquals(List.of(300, 200, 100), lista.stream().map(ExecucaoHistorico::getTamanho).toList());
    }

    // H3 / T3.1
    @Test
    void devolveListaVaziaQuandoNaoHaExecucoes() {
        assertEquals(0, repository.findAllByOrderByIdDesc().size());
    }
}
