package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.UUID;

public class BolsaVencida extends RuntimeException {

    public BolsaVencida(UUID bolsaId) {
        throw new UnsupportedOperationException("não implementado");
    }

    public UUID getBolsaId() {
        throw new UnsupportedOperationException("não implementado");
    }
}
