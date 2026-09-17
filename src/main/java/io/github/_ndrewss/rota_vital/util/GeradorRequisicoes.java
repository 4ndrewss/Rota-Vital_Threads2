package io.github._ndrewss.rota_vital.util;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import io.github._ndrewss.rota_vital.model.Requisicao;

/**
 * Gera listas de {@link Requisicao} com valores aleatórios para testes de carga.
 *
 * <p>Uso rápido, com a configuração padrão:
 * <pre>
 * List&lt;Requisicao&gt; cemMil = GeradorRequisicoes.gerar(GeradorRequisicoes.CEM_MIL);
 * List&lt;Requisicao&gt; umMilhao = GeradorRequisicoes.gerar(GeradorRequisicoes.UM_MILHAO);
 * </pre>
 *
 * <p>Para mudar a faixa de valores ou repetir sempre a mesma massa de dados,
 * instancie a classe:
 * <pre>
 * var gerador = new GeradorRequisicoes(20, 100, 3_000, 30, 42L);
 * List&lt;Requisicao&gt; dados = gerador.gerarLista(GeradorRequisicoes.UM_MILHAO);
 * </pre>
 */
public class GeradorRequisicoes {

	public static final int CEM_MIL = 100_000;
	public static final int UM_MILHAO = 1_000_000;

	private static final int QTD_HOSPITAIS_PADRAO = 10;
	private static final long TEMPO_MINIMO_PADRAO = 50L;
	private static final long TEMPO_MAXIMO_PADRAO = 5_000L;
	private static final int JANELA_DIAS_PADRAO = 7;

	private static final int SEGUNDOS_POR_DIA = 86_400;

	/** Ids de hospital usados no sorteio ("HOSP-01", "HOSP-02", ...). */
	private final String[] hospitais;

	/** Menor tempo de atendimento sorteado, em milissegundos (inclusivo). */
	private final long tempoMinimo;

	/** Maior tempo de atendimento sorteado, em milissegundos (inclusivo). */
	private final long tempoMaximo;

	/** Quantos dias para trás as datas podem cair, contados a partir de agora. */
	private final int janelaDias;

	private final SplittableRandom random;

	/** Gerador com a configuração padrão e semente aleatória. */
	public GeradorRequisicoes() {
		this(QTD_HOSPITAIS_PADRAO, TEMPO_MINIMO_PADRAO, TEMPO_MAXIMO_PADRAO, JANELA_DIAS_PADRAO, null);
	}

	/** Gerador com a configuração padrão e semente fixa (gera sempre a mesma massa de dados). */
	public GeradorRequisicoes(long semente) {
		this(QTD_HOSPITAIS_PADRAO, TEMPO_MINIMO_PADRAO, TEMPO_MAXIMO_PADRAO, JANELA_DIAS_PADRAO, semente);
	}

	/**
	 * @param qtdHospitais quantos hospitais diferentes aparecem nas requisições
	 * @param tempoMinimo  menor tempo de atendimento em ms (inclusivo)
	 * @param tempoMaximo  maior tempo de atendimento em ms (inclusivo)
	 * @param janelaDias   quantos dias para trás as datas podem cair
	 * @param semente      semente do sorteio, ou {@code null} para usar uma aleatória
	 */
	public GeradorRequisicoes(int qtdHospitais, long tempoMinimo, long tempoMaximo, int janelaDias, Long semente) {
		if (qtdHospitais <= 0) {
			throw new IllegalArgumentException("qtdHospitais deve ser maior que zero");
		}
		if (tempoMinimo < 0 || tempoMaximo < tempoMinimo) {
			throw new IllegalArgumentException("faixa de tempo inválida: " + tempoMinimo + ".." + tempoMaximo);
		}
		if (janelaDias <= 0) {
			throw new IllegalArgumentException("janelaDias deve ser maior que zero");
		}
		this.hospitais = montarHospitais(qtdHospitais);
		this.tempoMinimo = tempoMinimo;
		this.tempoMaximo = tempoMaximo;
		this.janelaDias = janelaDias;
		this.random = (semente == null) ? new SplittableRandom() : new SplittableRandom(semente);
	}

	/** Atalho: gera {@code quantidade} requisições com a configuração padrão. */
	public static List<Requisicao> gerar(int quantidade) {
		return new GeradorRequisicoes().gerarLista(quantidade);
	}

	/** Atalho: gera {@code quantidade} requisições com semente fixa (resultado reproduzível). */
	public static List<Requisicao> gerar(int quantidade, long semente) {
		return new GeradorRequisicoes(semente).gerarLista(quantidade);
	}

	/**
	 * Gera a lista de requisições.
	 *
	 * @param quantidade quantos registros criar (ex.: {@link #CEM_MIL}, {@link #UM_MILHAO})
	 */
	public List<Requisicao> gerarLista(int quantidade) {
		if (quantidade <= 0) {
			throw new IllegalArgumentException("quantidade deve ser maior que zero");
		}
		LocalDateTime inicioJanela = LocalDateTime.now().minusDays(janelaDias);
		long segundosNaJanela = (long) janelaDias * SEGUNDOS_POR_DIA;

		List<Requisicao> requisicoes = new ArrayList<>(quantidade);
		for (int i = 0; i < quantidade; i++) {
			requisicoes.add(new Requisicao(
					"REQ-" + i,
					hospitais[random.nextInt(hospitais.length)],
					random.nextLong(tempoMinimo, tempoMaximo + 1),
					inicioJanela.plusSeconds(random.nextLong(segundosNaJanela))));
		}
		return requisicoes;
	}

	private static String[] montarHospitais(int qtdHospitais) {
		String[] ids = new String[qtdHospitais];
		for (int i = 0; i < qtdHospitais; i++) {
			ids[i] = String.format("HOSP-%02d", i + 1);
		}
		return ids;
	}

}