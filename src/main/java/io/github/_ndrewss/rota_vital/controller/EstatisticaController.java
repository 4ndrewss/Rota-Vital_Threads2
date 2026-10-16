package io.github._ndrewss.rota_vital.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github._ndrewss.rota_vital.model.EstatisticaHospital;
import io.github._ndrewss.rota_vital.model.Requisicao;
import io.github._ndrewss.rota_vital.service.EstatisticaService;
import io.github._ndrewss.rota_vital.service.HistoricoService;
import io.github._ndrewss.rota_vital.util.GeradorRequisicoes;
import io.github._ndrewss.rota_vital.util.ValidadorParametros;

@RestController
public class EstatisticaController {
    private static final Logger LOG = LoggerFactory.getLogger(EstatisticaController.class);

    // O Spring cria e injeta essa dependência automaticamente
    // por causa do @Service que colocamos lá no EstatisticaService
    private final EstatisticaService estatisticaService;
    private final HistoricoService historicoService;

    /** Recebe os serviços de cálculo e de persistência das execuções. */
    public EstatisticaController(EstatisticaService estatisticaService, HistoricoService historicoService) {
        this.estatisticaService = estatisticaService;
        this.historicoService = historicoService;
    }

    // Exemplo de chamada: GET http://localhost:8080/estatisticas/sequencial?tamanho=100000
    /** Valida a carga, calcula e informa se foi possível registrar a execução sequencial. */
    @GetMapping("/estatisticas/sequencial")
    public Map<String, Object> calcularSequencial(@RequestParam int tamanho) {
        ValidadorParametros.validarTamanho(tamanho);

        // 1. Gera os dados fake (ajusta "gerar" pro nome real do seu método)
        List<Requisicao> requisicoes = GeradorRequisicoes.gerar(tamanho);

        // 2. Marca o tempo ANTES de começar o processamento
        long inicio = System.nanoTime();

        // 3. Roda a versão sequencial
        Map<String, EstatisticaHospital> resultado = estatisticaService.calcularSequencial(requisicoes);

        // 4. Marca o tempo DEPOIS que terminou
        long fim = System.nanoTime();

        // 5. Calcula quanto tempo levou, em milissegundos
        long tempoGastoMs = (fim - inicio) / 1_000_000;

        // Guarda a execução no histórico (só chega aqui se os parâmetros eram válidos)
        boolean historicoRegistrado = registrarHistorico(() -> historicoService.registrarSequencial(tamanho, tempoGastoMs));

        // 6. Monta a resposta que vai virar JSON:
        //    um "pacote" contendo o tempo gasto e o resultado do cálculo
        Map<String, Object> resposta = new java.util.HashMap<>();
        resposta.put("tempoGastoMs", tempoGastoMs);
        resposta.put("totalHospitais", resultado.size());
        resposta.put("resultado", resultado);
        resposta.put("historicoRegistrado", historicoRegistrado);
        if (!historicoRegistrado) {
            resposta.put("aviso", "Cálculo concluído, mas não foi possível registrar no histórico.");
        }

        return resposta;
    }

    // Exemplo de chamada: GET http://localhost:8080/estatisticas/paralelo?tamanho=100000&threads=4
    /** Valida tamanho e threads, calcula e informa se foi possível registrar a execução paralela. */
    @GetMapping("/estatisticas/paralelo")
    public Map<String, Object> calcularParalelo(@RequestParam int tamanho, @RequestParam int threads) {
        ValidadorParametros.validarTamanho(tamanho);
        estatisticaService.validarThreads(threads);

        // 1. Gera os dados fake
        List<Requisicao> requisicoes = GeradorRequisicoes.gerar(tamanho);

        // 2. Marca o tempo ANTES de começar o processamento
        long inicio = System.nanoTime();

        // 3. Roda a versão paralela, dividindo o trabalho em "threads" pedaços
        Map<String, EstatisticaHospital> resultado = estatisticaService.calcularParalelo(requisicoes, threads);

        // 4. Marca o tempo DEPOIS que terminou
        long fim = System.nanoTime();

        // 5. Calcula quanto tempo levou, em milissegundos
        long tempoGastoMs = (fim - inicio) / 1_000_000;

        // Guarda a execução no histórico (só chega aqui se os parâmetros eram válidos)
        boolean historicoRegistrado = registrarHistorico(() -> historicoService.registrarParalelo(tamanho, threads, tempoGastoMs));

        // 6. Monta a resposta que vai virar JSON
        Map<String, Object> resposta = new java.util.HashMap<>();
        resposta.put("threads", threads);
        resposta.put("tempoGastoMs", tempoGastoMs);
        resposta.put("totalHospitais", resultado.size());
        resposta.put("resultado", resultado);
        resposta.put("historicoRegistrado", historicoRegistrado);
        if (!historicoRegistrado) {
            resposta.put("aviso", "Cálculo concluído, mas não foi possível registrar no histórico.");
        }

        return resposta;
    }
    /** Isola falhas de persistência para preservar o resultado calculado e avisar o cliente. */
    private boolean registrarHistorico(Runnable gravacao) {
        try {
            gravacao.run();
            return true;
        } catch (RuntimeException e) {
            LOG.warn("Cálculo concluído sem registro no histórico ({})", e.getClass().getSimpleName());
            return false;
        }
    }
}
