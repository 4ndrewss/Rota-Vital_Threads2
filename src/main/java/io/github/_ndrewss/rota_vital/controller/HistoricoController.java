package io.github._ndrewss.rota_vital.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;
import io.github._ndrewss.rota_vital.service.HistoricoService;

/** Consulta paginada das execuções sem carregar todo o histórico em memória. */
@RestController
public class HistoricoController {
    private final HistoricoService historicoService;

    /** Recebe o serviço responsável pela consulta e pelos limites da página. */
    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    /** Retorna um array JSON; a primeira página tem vinte registros por padrão. */
    @GetMapping("/historico")
    public List<ExecucaoHistorico> listar(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int limite) {
        return historicoService.listar(pagina, limite);
    }
}
