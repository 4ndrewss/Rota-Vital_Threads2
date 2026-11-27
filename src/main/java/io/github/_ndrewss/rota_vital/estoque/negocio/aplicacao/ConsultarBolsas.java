package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

@Service
public class ConsultarBolsas {

    public ConsultarBolsas(BolsaRepository repository) {
    }

    public List<Bolsa> listar() {
        throw new UnsupportedOperationException("não implementado");
    }

    public Bolsa buscar(UUID id) {
        throw new UnsupportedOperationException("não implementado");
    }
}
