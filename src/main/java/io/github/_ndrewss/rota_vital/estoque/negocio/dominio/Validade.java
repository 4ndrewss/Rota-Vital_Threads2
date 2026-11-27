package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.time.LocalDate;
import java.util.Objects;

/** Objeto de valor: data de validade de uma bolsa. */
public final class Validade {

    private final LocalDate data;

    public Validade(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("validade é obrigatória");
        }
        this.data = data;
    }

    public LocalDate getData() {
        return data;
    }

    public boolean estaVencida(LocalDate hoje) {
        if (hoje == null) {
            throw new IllegalArgumentException("data de hoje é obrigatória");
        }
        return hoje.isAfter(data);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Validade outra && data.equals(outra.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
