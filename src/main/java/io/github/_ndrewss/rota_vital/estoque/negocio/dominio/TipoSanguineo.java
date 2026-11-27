package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.util.Set;

/**
 * Objeto de valor: tipo sanguíneo de uma bolsa.
 * Imutável e validado no construtor; só aceita os oito tipos ABO/Rh, escritos exatamente assim.
 */
public final class TipoSanguineo {

    private static final Set<String> VALIDOS = Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    private final String valor;

    /** @throws IllegalArgumentException se o valor for nulo ou não for um dos oito tipos. */
    public TipoSanguineo(String valor) {
        if (valor == null || !VALIDOS.contains(valor)) {
            throw new IllegalArgumentException("tipo sanguíneo inválido: " + valor
                    + " (use A+, A-, B+, B-, AB+, AB-, O+ ou O-)");
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
        return valor.hashCode();
    }

    @Override
    public String toString() {
        return valor;
    }
}
