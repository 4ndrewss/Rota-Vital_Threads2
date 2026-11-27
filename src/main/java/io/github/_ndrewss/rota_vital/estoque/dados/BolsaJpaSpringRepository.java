package io.github._ndrewss.rota_vital.estoque.dados;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data interno da camada de dados; fora daqui use {@code BolsaRepository}. */
interface BolsaJpaSpringRepository extends JpaRepository<BolsaJpa, UUID> {

    List<BolsaJpa> findAllByOrderByValidadeAscIdAsc();
}
