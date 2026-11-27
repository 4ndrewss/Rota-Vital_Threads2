package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Raiz de agregado do Estoque: uma bolsa de sangue.
 * Tem identidade (id), tipo e validade imutáveis e uma reserva opcional.
 * Não há setters: o único jeito de mudar o estado é {@link #reservar(String, LocalDate)}.
 * Classe de domínio pura: a persistência fica em estoque/dados (BolsaJpa + mapper).
 */
public class Bolsa {

    private final UUID id;
    private final TipoSanguineo tipo;
    private final Validade validade;
    private Reserva reserva;

    /** Cria uma bolsa sem reserva. @throws IllegalArgumentException se algum argumento for nulo. */
    public Bolsa(UUID id, TipoSanguineo tipo, Validade validade) {
        this.id = obrigatorio(id, "id da bolsa é obrigatório");
        this.tipo = obrigatorio(tipo, "tipo sanguíneo é obrigatório");
        this.validade = obrigatorio(validade, "validade é obrigatória");
    }

    /** Cadastra uma bolsa nova, gerando o id no próprio domínio. */
    public static Bolsa nova(TipoSanguineo tipo, Validade validade) {
        return new Bolsa(UUID.randomUUID(), tipo, validade);
    }

    /** Recria uma bolsa já existente (por exemplo, lida do banco); {@code reserva} pode ser nula. */
    public static Bolsa reconstituir(UUID id, TipoSanguineo tipo, Validade validade, Reserva reserva) {
        Bolsa bolsa = new Bolsa(id, tipo, validade);
        bolsa.reserva = reserva;
        return bolsa;
    }

    /**
     * Reserva a bolsa para uma requisição.
     * Ordem de verificação: primeiro "já reservada", depois "vencida". Se falhar, o estado não muda.
     *
     * @throws BolsaJaReservada se a bolsa já tem reserva
     * @throws BolsaVencida se {@code hoje} é posterior à validade
     * @throws IllegalArgumentException se requisicaoId ou hoje forem inválidos
     */
    public void reservar(String requisicaoId, LocalDate hoje) {
        if (estaReservada()) {
            throw new BolsaJaReservada(id);
        }
        if (validade.estaVencida(hoje)) {
            throw new BolsaVencida(id);
        }
        this.reserva = new Reserva(requisicaoId);
    }

    public boolean estaReservada() {
        return reserva != null;
    }

    public UUID getId() {
        return id;
    }

    public TipoSanguineo getTipo() {
        return tipo;
    }

    public Validade getValidade() {
        return validade;
    }

    public Optional<Reserva> getReserva() {
        return Optional.ofNullable(reserva);
    }

    /** Entidade: duas bolsas são a mesma quando têm o mesmo id. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Bolsa outra && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private static <T> T obrigatorio(T valor, String mensagem) {
        if (valor == null) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }
}
