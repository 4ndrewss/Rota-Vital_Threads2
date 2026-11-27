package io.github._ndrewss.rota_vital.estoque.dados;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

/** Implementação JPA da porta {@link BolsaRepository}, convertendo com {@link BolsaMapper}. */
@Repository
public class BolsaRepositoryJpa implements BolsaRepository {

    private final BolsaJpaSpringRepository jpa;

    BolsaRepositoryJpa(BolsaJpaSpringRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Bolsa salvar(Bolsa bolsa) {
        return BolsaMapper.paraDominio(jpa.save(BolsaMapper.paraJpa(bolsa)));
    }

    @Override
    public Optional<Bolsa> buscarPorId(UUID id) {
        return jpa.findById(id).map(BolsaMapper::paraDominio);
    }

    @Override
    public List<Bolsa> listar() {
        return jpa.findAllByOrderByValidadeAscIdAsc().stream().map(BolsaMapper::paraDominio).toList();
    }
}
