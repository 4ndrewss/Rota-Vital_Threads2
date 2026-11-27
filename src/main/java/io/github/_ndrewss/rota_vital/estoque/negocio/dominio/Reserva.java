package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

/**
 * Reserva de uma bolsa para uma requisição. Imutável.
 * O requisicaoId é String, o mesmo tipo do id de {@code Requisicao}.
 */
public final class Reserva {

    private final String requisicaoId;

    /** @throws IllegalArgumentException se o requisicaoId for nulo ou em branco. */
    public Reserva(String requisicaoId) {
        if (requisicaoId == null || requisicaoId.isBlank()) {
            throw new IllegalArgumentException("requisicaoId é obrigatório");
        }
        this.requisicaoId = requisicaoId;
    }

    public String getRequisicaoId() {
        return requisicaoId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Reserva outra && requisicaoId.equals(outra.requisicaoId);
    }

    @Override
    public int hashCode() {
        return requisicaoId.hashCode();
    }
}
