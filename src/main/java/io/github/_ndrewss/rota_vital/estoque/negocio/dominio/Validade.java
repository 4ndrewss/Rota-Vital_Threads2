package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.time.LocalDate;

/** Objeto de valor: data de validade de uma bolsa. */
public final class Validade {

    public Validade(LocalDate data) {
        throw new UnsupportedOperationException("não implementado");
    }

    public LocalDate getData() {
        throw new UnsupportedOperationException("não implementado");
    }

    public boolean estaVencida(LocalDate hoje) {
        throw new UnsupportedOperationException("não implementado");
    }
}
