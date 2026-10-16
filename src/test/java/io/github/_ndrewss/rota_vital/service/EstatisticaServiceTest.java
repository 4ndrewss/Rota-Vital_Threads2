package io.github._ndrewss.rota_vital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github._ndrewss.rota_vital.model.EstatisticaHospital;
import io.github._ndrewss.rota_vital.model.Requisicao;
import io.github._ndrewss.rota_vital.util.GeradorRequisicoes;

/**
 * Garante que a versão paralela produz exatamente o mesmo resultado que a
 * versão sequencial. Se essas comparações falharem, é sinal de race condition
 * (provavelmente na construção dos mapas parciais ou na agregação final).
 */
class EstatisticaServiceTest {

    private final EstatisticaService service = new EstatisticaService();

    /** Compara os resultados por hospital usando a mesma massa determinística. */
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 8})
    void paraleloDeveBaterComSequencialParaVariasQuantidadesDeThreads(int numThreads) {
        List<Requisicao> requisicoes = GeradorRequisicoes.gerar(GeradorRequisicoes.CEM_MIL, 42L);

        Map<String, EstatisticaHospital> resultadoSequencial = service.calcularSequencial(requisicoes);
        Map<String, EstatisticaHospital> resultadoParalelo = service.calcularParalelo(requisicoes, numThreads);

        assertEstatisticasIguais(resultadoSequencial, resultadoParalelo);
    }

    /** Detecta perda de dados na partição que recebe o resto da divisão. */
    @Test
    void paraleloDeveBaterComSequencialComTamanhoNaoDivisivelPeloNumeroDeThreads() {
        // 100_007 não é múltiplo de 7: força pedaços de tamanhos diferentes
        List<Requisicao> requisicoes = GeradorRequisicoes.gerar(100_007, 123L);

        Map<String, EstatisticaHospital> resultadoSequencial = service.calcularSequencial(requisicoes);
        Map<String, EstatisticaHospital> resultadoParalelo = service.calcularParalelo(requisicoes, 7);

        assertEstatisticasIguais(resultadoSequencial, resultadoParalelo);
    }

    // H1 / T1.1 — regra: threads precisa ser maior que zero
    /** Confere a validação e a mensagem para números de threads não positivos. */
    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void paraleloComThreadsMenorOuIgualAZeroDeveLancarIllegalArgumentException(int numThreads) {
        List<Requisicao> requisicoes = GeradorRequisicoes.gerar(1_000, 42L);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.calcularParalelo(requisicoes, numThreads));

        assertEquals("threads deve ser maior que zero", erro.getMessage());
    }

    /** Limita a criação de threads e mantém o valor máximo suportado funcionando. */
    @Test
    void rejeitaExcessoDeThreadsEAceitaLimite() {
        var requisicoes = GeradorRequisicoes.gerar(32, 42L);
        assertThrows(IllegalArgumentException.class, () -> service.calcularParalelo(requisicoes, 33));
        assertThrows(IllegalArgumentException.class, () -> service.dividirLista(requisicoes, 33));
        assertEstatisticasIguais(service.calcularSequencial(requisicoes), service.calcularParalelo(requisicoes, 32));
    }

    /**
     * Compara os dois mapas "byte a byte": mesmas chaves de hospital, e para
     * cada hospital o mesmo total de requisições e a mesma soma de tempo de
     * atendimento (a média é derivada desses dois valores).
     */
    private void assertEstatisticasIguais(Map<String, EstatisticaHospital> esperado,
                                           Map<String, EstatisticaHospital> obtido) {
        assertEquals(new TreeMap<>(esperado).keySet(), new TreeMap<>(obtido).keySet(),
                "Os hospitais presentes nos dois resultados deveriam ser os mesmos");

        for (String hospitalId : esperado.keySet()) {
            EstatisticaHospital statsEsperado = esperado.get(hospitalId);
            EstatisticaHospital statsObtido = obtido.get(hospitalId);

            assertEquals(statsEsperado.getTotalRequisicoes(), statsObtido.getTotalRequisicoes(),
                    "totalRequisicoes divergente para " + hospitalId);
            assertEquals(statsEsperado.getSomaTempoAtendimento(), statsObtido.getSomaTempoAtendimento(),
                    "somaTempoAtendimento divergente para " + hospitalId);
            assertEquals(statsEsperado.getMedia(), statsObtido.getMedia(),
                    "media divergente para " + hospitalId);
        }
    }
}
