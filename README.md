# 🏥 Rota Vital — Processamento Sequencial e Paralelo

Aplicação em Java com Spring Boot desenvolvida para comparar processamento sequencial e
paralelo. O sistema gera requisições de atendimento hospitalar, agrupa os dados por hospital e
calcula estatísticas utilizando uma ou várias threads.

Os testes utilizam cargas de 100 mil e 1 milhão de requisições, processadas com
1, 2, 4 e 8 threads.

---

## 🚀 Como executar

Requisitos: Java 21, Spring Boot 4.0.8 e Maven Wrapper incluso no projeto.

```bash
# iniciar a aplicação
./mvnw spring-boot:run

# compilar
./mvnw clean compile

# executar os testes
./mvnw test
```

No Windows, utilize `mvnw.cmd` no lugar de `./mvnw`.

💡 Gerar 1 milhão de requisições pode consumir entre 150 e 200 MB de heap.
Caso ocorra `OutOfMemoryError`, execute a aplicação com `-Xmx2g`.

---

## ✅ Escopo entregue

- Cálculo de estatísticas por hospital (sequencial e paralelo)
- Divisão de carga entre threads com junção dos resultados parciais
- Endpoints REST para as duas versões, com tempo de resposta medido
- Comparação de desempenho documentada no relatório técnico

---

## 🗂️ Estrutura do projeto

```
src/main/java/io/github/_ndrewss/rota_vital/
├── RotaVitalApplication.java       # entrada da aplicação Spring Boot
├── controller/
│   └── EstatisticaController.java  # endpoints REST que disparam o processamento
├── model/
│   ├── Requisicao.java             # POJO com os dados usados nos cálculos
│   └── EstatisticaHospital.java    # acumulador por hospital (total, soma, média)
├── service/
│   └── EstatisticaService.java     # cálculo das estatísticas + divisão da carga
└── util/
    └── GeradorRequisicoes.java     # gerador de massa de dados fake
```

### `Requisicao`

POJO simples com os campos usados nas contas:

| Campo | Tipo | Descrição |
| --- | --- | --- |
| `id` | `String` | identificador da requisição |
| `hospitalId` | `String` | hospital que atendeu |
| `tempoAtendimento` | `long` | tempo de atendimento **em milissegundos** |
| `dataHora` | `LocalDateTime` | momento do registro |

Tem construtor vazio (para desserialização), construtor de atalho
`Requisicao(hospitalId, tempoAtendimento)` que preenche a data com `now()`,
construtor completo, getters/setters, `equals`/`hashCode` e `toString`.

### `EstatisticaController`

Expõe o processamento via HTTP e já devolve o tempo gasto, que é o número que
interessa para a comparação.

**Versão sequencial**

```http
GET /estatisticas/sequencial?tamanho=100000
```

**Versão paralela**

```http
GET /estatisticas/paralelo?tamanho=100000&threads=4
```

Ambas geram a massa de dados, rodam o processamento correspondente cronometrando com
`System.nanoTime()` e respondem no mesmo formato:

```json
{
  "tempoGastoMs": 42,
  "totalHospitais": 10,
  "resultado": {
    "HOSP-01": {
      "totalRequisicoes": 10012,
      "somaTempoAtendimento": 25318744,
      "media": 2528
    }
  }
}
```

O tempo medido cobre **só o processamento** — a geração dos dados fica de fora.

> Hoje o endpoint chama `GeradorRequisicoes.gerar(tamanho)` sem semente, então cada
> chamada trabalha sobre uma massa diferente. Para comparar sequencial vs. threads de
> forma justa, vale passar uma semente fixa.

---

## 👥 Membros da Equipe

| Integrante | GitHub |
|---|---|
| Andrews Queiroz | [@4ndrewss](https://github.com/4ndrewss) |
| Caio Gilles | [@CaioGilles](https://github.com/CaioGilles) |
| Enzo Amorim | [@ENZOBRS](https://github.com/ENZOBRS) |
| Gabriela Bayo | [@gabibayo](https://github.com/gabibayo) |
| Glauco Santos| [@glaucosantos002](https://github.com/glaucosantos002) |
| Gustavo Veloso | [@velosogustavo](https://github.com/velosogustavo) |

---

## 📌 Gestão e Organização

O acompanhamento das etapas de construção do Rota Vital, a divisão técnica da equipe e o backlog
do projeto foram gerenciados via Trello.

📋 **Acesso ao Quadro:** [Acessar Trello da Equipe](https://trello.com/b/nTGVCC8F/rota-vitalthreads2)

<img width="1914" height="846" alt="image" src="https://github.com/user-attachments/assets/c060417c-996a-44bd-a18e-3e4e4f50c8f5" />

---

## 📋 Convenções

- Tempos de atendimento sempre em **milissegundos**.
- Código e comentários em português.
- Indentação com tab, seguindo o padrão que o Spring Initializr gerou.

---

## 📄 Relatório Técnico

O relatório completo com justificativa, metodologia, resultados e análise está disponível em:
[`relatorio_rota_vital.pdf`](./docs/relatorio_rota_vital.pdf)
