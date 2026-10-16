package io.github._ndrewss.rota_vital.util;

/**
 * Regras de validação dos parâmetros da API, separadas do controller para poderem ser
 * testadas sozinhas (teste unitário), sem subir Spring nem banco.
 */
public final class ValidadorParametros {

    /** Maior quantidade de requisições aceita numa chamada, para limitar o uso de memória. */
    public static final int TAMANHO_MAXIMO = 1_000_000;

    private ValidadorParametros() {
    }

    /**
     * Confere se o {@code tamanho} está entre 1 e {@link #TAMANHO_MAXIMO}.
     * A mensagem cita "tamanho" (o nome do parâmetro na URL) em vez de "quantidade"
     * (o nome usado dentro do {@code GeradorRequisicoes}).
     *
     * @throws IllegalArgumentException se o valor estiver fora da faixa
     */
    public static void validarTamanho(int tamanho) {
        if (tamanho <= 0) {
            throw new IllegalArgumentException("tamanho deve ser maior que zero");
        }
        if (tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("tamanho deve ser no máximo " + TAMANHO_MAXIMO);
        }
    }
}
