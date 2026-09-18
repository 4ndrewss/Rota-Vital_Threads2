package io.github._ndrewss.rota_vital.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github._ndrewss.rota_vital.model.EstatisticaHospital;
import io.github._ndrewss.rota_vital.model.Requisicao;
import io.github._ndrewss.rota_vital.service.EstatisticaService;
import io.github._ndrewss.rota_vital.util.GeradorRequisicoes;

@RestController
public class EstatisticaController {

    // O Spring cria e injeta essa dependência automaticamente
    // por causa do @Service que colocamos lá no EstatisticaService
    private final EstatisticaService estatisticaService;

    public EstatisticaController(EstatisticaService estatisticaService) {
        this.estatisticaService = estatisticaService;
    }

    // Exemplo de chamada: GET http://localhost:8080/estatisticas/sequencial?tamanho=100000
    @GetMapping("/estatisticas/sequencial")
    public Map<String, Object> calcularSequencial(@RequestParam int tamanho) {

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

        // 6. Monta a resposta que vai virar JSON:
        //    um "pacote" contendo o tempo gasto e o resultado do cálculo
        Map<String, Object> resposta = new java.util.HashMap<>();
        resposta.put("tempoGastoMs", tempoGastoMs);
        resposta.put("totalHospitais", resultado.size());
        resposta.put("resultado", resultado);

        return resposta;
    }
}