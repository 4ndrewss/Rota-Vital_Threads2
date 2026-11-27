package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.TipoSanguineo;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Validade;

/**
 * Caso de uso: cadastrar uma bolsa no estoque.
 * Aceita validade no passado (o cadastro registra o que existe; a regra de vencimento vale na reserva).
 */
@Service
public class RegistrarBolsa {

    private final BolsaRepository repository;

    public RegistrarBolsa(BolsaRepository repository) {
        this.repository = repository;
    }

    /** @throws IllegalArgumentException se o tipo ou a validade forem inválidos */
    public Bolsa executar(String tipoSanguineo, LocalDate validade) {
        return repository.salvar(Bolsa.nova(new TipoSanguineo(tipoSanguineo), new Validade(validade)));
    }
}
