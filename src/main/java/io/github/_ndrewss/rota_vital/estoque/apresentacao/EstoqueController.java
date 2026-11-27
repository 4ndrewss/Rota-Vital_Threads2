package io.github._ndrewss.rota_vital.estoque.apresentacao;

import org.springframework.web.bind.annotation.RestController;

import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.ConsultarBolsas;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.RegistrarBolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.aplicacao.ReservarBolsa;

@RestController
public class EstoqueController {

    public EstoqueController(RegistrarBolsa registrarBolsa, ConsultarBolsas consultarBolsas,
            ReservarBolsa reservarBolsa) {
    }
}
