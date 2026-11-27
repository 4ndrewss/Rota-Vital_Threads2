package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.UUID;

public class BolsaJaReservada extends RuntimeException {

    public BolsaJaReservada(UUID bolsaId) {
        throw new UnsupportedOperationException("não implementado");
    }

    public UUID getBolsaId() {
        throw new UnsupportedOperationException("não implementado");
    }
}
