package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaNaoEncontrada;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

/**
 * Caso de uso: reservar uma bolsa para uma requisição.
 * "Hoje" vem de um {@link Clock} injetado, assim o controller não precisa conhecer datas
 * e os testes fixam o dia com {@code Clock.fixed}.
 */
@Service
public class ReservarBolsa {

    private final BolsaRepository repository;
    private final Clock relogio;

    public ReservarBolsa(BolsaRepository repository, Clock relogio) {
        this.repository = repository;
        this.relogio = relogio;
    }

    /**
     * @throws BolsaNaoEncontrada se não existir bolsa com o id
     * @throws io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaJaReservada se já reservada
     * @throws io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaVencida se vencida
     */
    public Bolsa executar(UUID bolsaId, String requisicaoId) {
        Bolsa bolsa = repository.buscarPorId(bolsaId).orElseThrow(() -> new BolsaNaoEncontrada(bolsaId));
        bolsa.reservar(requisicaoId, LocalDate.now(relogio));
        return repository.salvar(bolsa);
    }
}
