package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import java.time.Clock;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

@Service
public class ReservarBolsa {

    public ReservarBolsa(BolsaRepository repository, Clock relogio) {
    }

    public Bolsa executar(UUID bolsaId, String requisicaoId) {
        throw new UnsupportedOperationException("não implementado");
    }
}
