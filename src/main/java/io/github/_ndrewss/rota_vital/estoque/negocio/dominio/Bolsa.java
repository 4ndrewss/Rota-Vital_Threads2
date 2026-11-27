package io.github._ndrewss.rota_vital.estoque.negocio.dominio;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/** Raiz de agregado do Estoque: uma bolsa de sangue. */
public class Bolsa {

    public Bolsa(UUID id, TipoSanguineo tipo, Validade validade) {
        throw new UnsupportedOperationException("não implementado");
    }

    public static Bolsa nova(TipoSanguineo tipo, Validade validade) {
        throw new UnsupportedOperationException("não implementado");
    }

    public static Bolsa reconstituir(UUID id, TipoSanguineo tipo, Validade validade, Reserva reserva) {
        throw new UnsupportedOperationException("não implementado");
    }

    public void reservar(String requisicaoId, LocalDate hoje) {
        throw new UnsupportedOperationException("não implementado");
    }

    public boolean estaReservada() {
        throw new UnsupportedOperationException("não implementado");
    }

    public UUID getId() {
        throw new UnsupportedOperationException("não implementado");
    }

    public TipoSanguineo getTipo() {
        throw new UnsupportedOperationException("não implementado");
    }

    public Validade getValidade() {
        throw new UnsupportedOperationException("não implementado");
    }

    public Optional<Reserva> getReserva() {
        throw new UnsupportedOperationException("não implementado");
    }
}
