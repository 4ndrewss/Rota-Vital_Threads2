package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.UUID;

/** Exceção de negócio do Estoque (sem dependência de Spring): não existe bolsa com o id informado. */
public class BolsaNaoEncontrada extends RuntimeException {

    private final UUID bolsaId;

    public BolsaNaoEncontrada(UUID bolsaId) {
        super("bolsa não encontrada: " + bolsaId);
        this.bolsaId = bolsaId;
    }

    public UUID getBolsaId() {
        return bolsaId;
    }
}
