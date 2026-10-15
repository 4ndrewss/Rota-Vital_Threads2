package io.github._ndrewss.rota_vital.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;
import io.github._ndrewss.rota_vital.service.HistoricoService;

@RestController
public class HistoricoController {

    private final HistoricoService historicoService;

    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    // Exemplo de chamada: GET http://localhost:8080/historico
    @GetMapping("/historico")
    public List<ExecucaoHistorico> listar() {
        return historicoService.listar();
    }
}
