package io.github._ndrewss.rota_vital.estoque.dados;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Reserva;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.TipoSanguineo;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Validade;

/** Converte entre a raiz de domínio {@link Bolsa} e a entidade de persistência {@link BolsaJpa}. */
final class BolsaMapper {

    private BolsaMapper() {
    }

    static BolsaJpa paraJpa(Bolsa bolsa) {
        String requisicaoId = bolsa.getReserva().map(Reserva::getRequisicaoId).orElse(null);
        return new BolsaJpa(bolsa.getId(), bolsa.getTipo().getValor(), bolsa.getValidade().getData(), requisicaoId);
    }

    static Bolsa paraDominio(BolsaJpa jpa) {
        Reserva reserva = jpa.getRequisicaoId() == null ? null : new Reserva(jpa.getRequisicaoId());
        return Bolsa.reconstituir(jpa.getId(), new TipoSanguineo(jpa.getTipoSanguineo()),
                new Validade(jpa.getValidade()), reserva);
    }
}
