package io.github._ndrewss.rota_vital.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 8})
    void paraleloDeveBaterComSequencialParaVariasQuantidadesDeThreads(int numThreads) {
        List<Requisicao> requisicoes = GeradorRequisicoes.gerar(GeradorRequisicoes.CEM_MIL, 42L);

        Map<String, EstatisticaHospital> resultadoSequencial = service.calcularSequencial(requisicoes);
        Map<String, EstatisticaHospital> resultadoParalelo = service.calcularParalelo(requisicoes, numThreads);

        assertEstatisticasIguais(resultadoSequencial, resultadoParalelo);
    }

    @Test
    void paraleloDeveBaterComSequencialComTamanhoNaoDivisivelPeloNumeroDeThreads() {
        // 100_007 não é múltiplo de 7: força pedaços de tamanhos diferentes
        List<Requisicao> requisicoes = GeradorRequisicoes.gerar(100_007, 123L);

        Map<String, EstatisticaHospital> resultadoSequencial = service.calcularSequencial(requisicoes);
        Map<String, EstatisticaHospital> resultadoParalelo = service.calcularParalelo(requisicoes, 7);

        assertEstatisticasIguais(resultadoSequencial, resultadoParalelo);
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
