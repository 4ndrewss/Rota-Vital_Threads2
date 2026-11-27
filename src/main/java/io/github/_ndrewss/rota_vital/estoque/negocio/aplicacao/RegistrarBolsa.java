package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

@Service
public class RegistrarBolsa {

    public RegistrarBolsa(BolsaRepository repository) {
    }

    public Bolsa executar(String tipoSanguineo, LocalDate validade) {
        throw new UnsupportedOperationException("não implementado");
    }
}
