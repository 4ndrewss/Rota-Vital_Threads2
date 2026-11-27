package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistência das bolsas; a implementação fica em estoque/dados. */
public interface BolsaRepository {

    /** Grava a bolsa (inclusão ou atualização) e devolve o estado gravado. */
    Bolsa salvar(Bolsa bolsa);

    Optional<Bolsa> buscarPorId(UUID id);

    /** Lista todas as bolsas, da validade mais próxima para a mais distante. */
    List<Bolsa> listar();
}
