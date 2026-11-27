package io.github._ndrewss.rota_vital.estoque.negocio.aplicacao;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.github._ndrewss.rota_vital.estoque.negocio.dominio.Bolsa;
import io.github._ndrewss.rota_vital.estoque.negocio.dominio.BolsaRepository;

/**
 * Repositório falso para os testes dos casos de uso (sem Spring, sem banco e sem Mockito).
 * Guarda cópias, como um banco faria: mudar a Bolsa devolvida não muda o que está gravado.
 */
class BolsaRepositoryEmMemoria implements BolsaRepository {

    private final Map<UUID, Bolsa> bolsas = new LinkedHashMap<>();
    int salvamentos;

    @Override
    public Bolsa salvar(Bolsa bolsa) {
        salvamentos++;
        bolsas.put(bolsa.getId(), copia(bolsa));
        return copia(bolsa);
    }

    @Override
    public Optional<Bolsa> buscarPorId(UUID id) {
        return Optional.ofNullable(bolsas.get(id)).map(BolsaRepositoryEmMemoria::copia);
    }

    @Override
    public List<Bolsa> listar() {
        return bolsas.values().stream()
                .sorted(Comparator.comparing((Bolsa b) -> b.getValidade().getData()).thenComparing(Bolsa::getId))
                .map(BolsaRepositoryEmMemoria::copia)
                .toList();
    }

    private static Bolsa copia(Bolsa b) {
        return Bolsa.reconstituir(b.getId(), b.getTipo(), b.getValidade(), b.getReserva().orElse(null));
    }
}
