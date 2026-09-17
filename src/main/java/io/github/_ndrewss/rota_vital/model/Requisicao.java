package io.github._ndrewss.rota_vital.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa uma requisição atendida por um hospital.
 * Guarda apenas os dados usados nos cálculos (tempo médio, contagem por hospital, etc.).
 */
public class Requisicao {

	/** Identificador da requisição, útil para rastrear qual thread a processou. */
	private String id;

	/** Hospital que atendeu a requisição. */
	private String hospitalId;

	/** Tempo de atendimento em milissegundos. */
	private long tempoAtendimento;

	/** Momento em que a requisição foi registrada. */
	private LocalDateTime dataHora;

	public Requisicao() {
	}

	public Requisicao(String hospitalId, long tempoAtendimento) {
		this(null, hospitalId, tempoAtendimento, LocalDateTime.now());
	}

	public Requisicao(String id, String hospitalId, long tempoAtendimento, LocalDateTime dataHora) {
		this.id = id;
		this.hospitalId = hospitalId;
		this.tempoAtendimento = tempoAtendimento;
		this.dataHora = dataHora;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHospitalId() {
		return hospitalId;
	}

	public void setHospitalId(String hospitalId) {
		this.hospitalId = hospitalId;
	}

	public long getTempoAtendimento() {
		return tempoAtendimento;
	}

	public void setTempoAtendimento(long tempoAtendimento) {
		this.tempoAtendimento = tempoAtendimento;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Requisicao outra)) {
			return false;
		}
		return tempoAtendimento == outra.tempoAtendimento
				&& Objects.equals(id, outra.id)
				&& Objects.equals(hospitalId, outra.hospitalId)
				&& Objects.equals(dataHora, outra.dataHora);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, hospitalId, tempoAtendimento, dataHora);
	}

	@Override
	public String toString() {
		return "Requisicao{" +
				"id='" + id + '\'' +
				", hospitalId='" + hospitalId + '\'' +
				", tempoAtendimento=" + tempoAtendimento +
				", dataHora=" + dataHora +
				'}';
	}

}
