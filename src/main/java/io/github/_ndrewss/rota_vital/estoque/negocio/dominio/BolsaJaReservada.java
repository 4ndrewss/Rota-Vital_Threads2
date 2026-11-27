package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.UUID;

/** Exceção de negócio do Estoque (sem dependência de Spring): a bolsa já tem uma reserva. */
public class BolsaJaReservada extends RuntimeException {

    private final UUID bolsaId;

    public BolsaJaReservada(UUID bolsaId) {
        super("bolsa já reservada: " + bolsaId);
        this.bolsaId = bolsaId;
    }

    public UUID getBolsaId() {
        return bolsaId;
    }
}
