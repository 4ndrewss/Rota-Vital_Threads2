package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaNaoEncontrada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

/** Casos de uso de leitura do estoque: listar e consultar por id. */
@Service
public class ConsultarBolsas {

    private final BolsaRepository repository;

    public ConsultarBolsas(BolsaRepository repository) {
        this.repository = repository;
    }

    /** Todas as bolsas, da validade mais próxima para a mais distante. */
    public List<Bolsa> listar() {
        return repository.listar();
    }

    /** @throws BolsaNaoEncontrada se não existir bolsa com o id */
    public Bolsa buscar(UUID id) {
        return repository.buscarPorId(id).orElseThrow(() -> new BolsaNaoEncontrada(id));
    }
}
