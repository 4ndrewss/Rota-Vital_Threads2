package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.time.LocalDate;

/**
 * Objeto de valor: data de validade de uma bolsa. Imutável e validado no construtor.
 * Regra do dia exato: a bolsa é utilizável até o fim do dia do vencimento,
 * ou seja, só está vencida a partir do dia seguinte.
 */
public final class Validade {

    private final LocalDate data;

    /** @throws IllegalArgumentException se a data for nula. */
    public Validade(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("validade é obrigatória");
        }
        this.data = data;
    }

    public LocalDate getData() {
        return data;
    }

    /** Verdadeiro só quando {@code hoje} é posterior à data de validade. */
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
        return data.hashCode();
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
