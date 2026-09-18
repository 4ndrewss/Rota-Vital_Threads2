package io.github._ndrewss.rota_vital.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.model.EstatisticaHospital;
import io.github._ndrewss.rota_vital.model.Requisicao;

@Service
public class EstatisticaService {
    public Map<String, EstatisticaHospital> calcularSequencial(List<Requisicao> requisicoes){
        Map<String, EstatisticaHospital> mapaEstatisticas = new HashMap<>();

        for(int i=0 ; i < requisicoes.size() ; i++){
            Requisicao req = requisicoes.get(i);
            String hospitalId = req.getHospitalId();

            EstatisticaHospital estatistica = mapaEstatisticas.get(hospitalId);
            if (estatistica == null) {
                estatistica = new EstatisticaHospital();
                mapaEstatisticas.put(hospitalId, estatistica);
            }

            estatistica.adicionar(req.getTempoAtendimento());
        }

        return mapaEstatisticas;
    }

    public List<List<Requisicao>> dividirLista(List<Requisicao> requisicoes, int numThreads) {

        // Lista que vai guardar os pedaços (cada pedaço é uma lista menor)
        List<List<Requisicao>> pedacos = new ArrayList<>();

        int tamanhoTotal = requisicoes.size();

        // Quantos itens cada pedaço vai ter, em média
        // Ex.: 100 itens / 4 threads = 25 itens por pedaço
        int tamanhoPedaco = tamanhoTotal / numThreads;

        int inicio = 0;

        for (int i = 0; i < numThreads; i++) {

            int fim = inicio + tamanhoPedaco;

            // Na ÚLTIMA thread, garante que pega todo o resto,
            // caso a divisão não seja exata (ex.: 101 itens / 4 threads)
            if (i == numThreads - 1) {
                fim = tamanhoTotal;
            }

            // subList() pega um pedaço da lista original, do índice "inicio" (incluso)
            // até o índice "fim" (excluído)
            List<Requisicao> pedaco = requisicoes.subList(inicio, fim);

            pedacos.add(pedaco);

            // O próximo pedaço começa onde esse terminou
            inicio = fim;
        }

        return pedacos;
    }
}