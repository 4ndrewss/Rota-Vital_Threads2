package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.UUID;

/** Exceção de negócio do Estoque (sem dependência de Spring): a bolsa passou da validade e não pode ser reservada. */
public class BolsaVencida extends RuntimeException {

    private final UUID bolsaId;

    public BolsaVencida(UUID bolsaId) {
        super("bolsa vencida: " + bolsaId);
        this.bolsaId = bolsaId;
    }

    public UUID getBolsaId() {
        return bolsaId;
    }
}
