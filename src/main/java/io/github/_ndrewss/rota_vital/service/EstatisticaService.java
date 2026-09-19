package io.github._ndrewss.rota_vital.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    /**
     * Junta vários mapas parciais (um por thread) num único mapa,
     * somando os valores dos hospitais que aparecem em mais de um pedaço.
     */
    public Map<String, EstatisticaHospital> agregar(List<Map<String, EstatisticaHospital>> mapasParciais) {
        Map<String, EstatisticaHospital> mapaFinal = new HashMap<>();

        for (Map<String, EstatisticaHospital> parcial : mapasParciais) {
            for (Map.Entry<String, EstatisticaHospital> entry : parcial.entrySet()) {
                String hospitalId = entry.getKey();
                EstatisticaHospital estatisticaParcial = entry.getValue();

                EstatisticaHospital estatisticaFinal = mapaFinal.get(hospitalId);
                if (estatisticaFinal == null) {
                    estatisticaFinal = new EstatisticaHospital();
                    mapaFinal.put(hospitalId, estatisticaFinal);
                }

                estatisticaFinal.somar(estatisticaParcial);
            }
        }

        return mapaFinal;
    }

    /**
     * Versão paralela: divide a lista em numThreads pedaços, processa cada
     * pedaço numa task submetida ao pool de threads (cada task devolve seu
     * próprio mapa parcial) e no final agrega os mapas parciais num único mapa.
     */
    public Map<String, EstatisticaHospital> calcularParalelo(List<Requisicao> requisicoes, int numThreads) {
        List<List<Requisicao>> pedacos = dividirLista(requisicoes, numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            List<Future<Map<String, EstatisticaHospital>>> futures = new ArrayList<>();

            for (List<Requisicao> pedaco : pedacos) {
                Callable<Map<String, EstatisticaHospital>> task = () -> calcularSequencial(pedaco);
                futures.add(executor.submit(task));
            }

            List<Map<String, EstatisticaHospital>> mapasParciais = new ArrayList<>();
            for (Future<Map<String, EstatisticaHospital>> future : futures) {
                mapasParciais.add(future.get());
            }

            return agregar(mapasParciais);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processamento paralelo interrompido", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Erro ao processar pedaço em paralelo", e.getCause());
        } finally {
            executor.shutdown();
        }
    }
}