package io.github._ndrewss.rota_vital.estoque.dados;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

@Repository
public class BolsaRepositoryJpa implements BolsaRepository {

    @Override
    public Bolsa salvar(Bolsa bolsa) {
        throw new UnsupportedOperationException("não implementado");
    }

    @Override
    public Optional<Bolsa> buscarPorId(UUID id) {
        throw new UnsupportedOperationException("não implementado");
    }

    @Override
    public List<Bolsa> listar() {
        throw new UnsupportedOperationException("não implementado");
    }
}
