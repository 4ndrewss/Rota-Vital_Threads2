package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/** Raiz de agregado do Estoque: uma bolsa de sangue. */
public class Bolsa {

    private final UUID id;
    private final TipoSanguineo tipo;
    private final Validade validade;
    private Reserva reserva;

    public Bolsa(UUID id, TipoSanguineo tipo, Validade validade) {
        if (id == null) {
            throw new IllegalArgumentException("id da bolsa é obrigatório");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("tipo sanguíneo é obrigatório");
        }
        if (validade == null) {
            throw new IllegalArgumentException("validade é obrigatória");
        }
        this.id = id;
        this.tipo = tipo;
        this.validade = validade;
    }

    public static Bolsa nova(TipoSanguineo tipo, Validade validade) {
        return new Bolsa(UUID.randomUUID(), tipo, validade);
    }

    public static Bolsa reconstituir(UUID id, TipoSanguineo tipo, Validade validade, Reserva reserva) {
        Bolsa bolsa = new Bolsa(id, tipo, validade);
        bolsa.reserva = reserva;
        return bolsa;
    }

    public void reservar(String requisicaoId, LocalDate hoje) {
        if (reserva != null) {
            throw new BolsaJaReservada(id);
        }
        if (validade.estaVencida(hoje)) {
            throw new BolsaVencida(id);
        }
        reserva = new Reserva(requisicaoId);
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

    @Override
    public boolean equals(Object o) {
        return o instanceof Bolsa outra && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
