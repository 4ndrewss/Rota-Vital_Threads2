package io.github._ndrewss.rota_vital.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import io.github._ndrewss.rota_vital.model.ExecucaoHistorico;
import io.github._ndrewss.rota_vital.repository.ExecucaoHistoricoRepository;

/** Guarda execuções com orçamento de armazenamento e consulta limitada no banco. */
@Service
public class HistoricoService {
    private static final String TIPO_SEQUENCIAL = "SEQUENCIAL";
    private static final String TIPO_PARALELO = "PARALELO";
    private final ExecucaoHistoricoRepository repository;
    private final TransactionTemplate transacao;
    private final int maxRegistros;

    /** Define um teto de até dez mil registros, sem excluir dados existentes automaticamente. */
    public HistoricoService(ExecucaoHistoricoRepository repository, PlatformTransactionManager manager,
            @Value("${rota.historico.max-registros:10000}") int maxRegistros) {
        if (maxRegistros < 1 || maxRegistros > 10_000) {
            throw new IllegalArgumentException("max-registros deve estar entre 1 e 10000");
        }
        this.repository = repository;
        this.maxRegistros = maxRegistros;
        this.transacao = new TransactionTemplate(manager);
        // O commit deve terminar antes de liberar o monitor, mesmo se o chamador tiver transação.
        this.transacao.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** A versão sequencial é registrada com uma única thread. */
    public void registrarSequencial(int tamanho, long tempoMs) {
        registrar(new ExecucaoHistorico(TIPO_SEQUENCIAL, tamanho, 1, tempoMs));
    }

    /** Registra o número de threads solicitado na execução paralela. */
    public void registrarParalelo(int tamanho, int threads, long tempoMs) {
        registrar(new ExecucaoHistorico(TIPO_PARALELO, tamanho, threads, tempoMs));
    }

    /** Recusa somente a gravação quando o orçamento do histórico está esgotado. */
    public void verificarCapacidade() {
        if (repository.count() >= maxRegistros) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "histórico cheio: exporte e remova registros antigos antes de novas execuções");
        }
    }

    /** Serializa contagem, inserção e commit para respeitar o teto em uma instância da API. */
    private synchronized void registrar(ExecucaoHistorico execucao) {
        transacao.executeWithoutResult(status -> {
            verificarCapacidade();
            repository.save(execucao);
        });
    }

    /** Consulta apenas a página solicitada, com até cem registros e IDs maiores primeiro. */
    public List<ExecucaoHistorico> listar(int pagina, int limite) {
        if (pagina < 0 || pagina > 10_000) {
            throw new IllegalArgumentException("pagina deve estar entre 0 e 10000");
        }
        if (limite < 1 || limite > 100) {
            throw new IllegalArgumentException("limite deve estar entre 1 e 100");
        }
        return repository.findAllByOrderByIdDesc(PageRequest.of(pagina, limite));
    }
}
