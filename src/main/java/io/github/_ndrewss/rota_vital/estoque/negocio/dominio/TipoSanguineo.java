package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.Objects;
import java.util.Set;

/** Objeto de valor: tipo sanguíneo de uma bolsa (A+, A-, B+, B-, AB+, AB-, O+, O-). */
public final class TipoSanguineo {

    private final String valor;

    public TipoSanguineo(String valor) {
        if (valor == null || !Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").contains(valor)) {
            throw new IllegalArgumentException("tipo sanguíneo inválido: " + valor);
        }
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TipoSanguineo outro && valor.equals(outro.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}
