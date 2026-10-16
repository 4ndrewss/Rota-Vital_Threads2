package io.github._ndrewss.rota_vital.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;
import io.github._ndrewss.rota_vital.service.HistoricoService;

/**
 * Teste unitário da H3 / T3.2: o controller do histórico com o service de mentira (mock).
 * Obs.: o código já existia, então este teste nasce verde (teste de caracterização). Os valores
 * padrão da URL (pagina=0 e limite=20) vivem na anotação e são cobertos pelos testes de aceitação.
 */
class HistoricoControllerTest {

    private final HistoricoService service = mock(HistoricoService.class);
    private final HistoricoController controller = new HistoricoController(service);

    // H3 / T3.2
    @Test
    void listarDevolveAPaginaQueOServiceRetornouPedindoOsValoresRecebidos() {
        List<ExecucaoHistorico> pagina = List.of(new ExecucaoHistorico("PARALELO", 10, 2, 5L));
        when(service.listar(1, 5)).thenReturn(pagina);

        List<ExecucaoHistorico> resposta = controller.listar(1, 5);

        verify(service).listar(1, 5);
        assertEquals(pagina, resposta);
    }
}
