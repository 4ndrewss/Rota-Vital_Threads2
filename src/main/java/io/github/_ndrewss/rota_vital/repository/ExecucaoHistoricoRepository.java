package io.github._ndrewss.rota_vital.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;

public interface ExecucaoHistoricoRepository extends JpaRepository<ExecucaoHistorico, Long> {

    // O Spring Data monta o SQL pelo nome do método: "ordenar por id, do maior para o menor".
    // O id é gerado em sequência pelo banco, então o maior id é sempre a execução mais recente.
    List<ExecucaoHistorico> findAllByOrderByIdDesc();
}