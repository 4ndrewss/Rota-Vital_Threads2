# Rota Vital

Projeto de estudo sobre processamento concorrente em Java: gerar um grande volume de
requisições de atendimento hospitalar e processar as estatísticas de forma sequencial
e com threads, comparando os tempos.

> Este README é atualizado conforme o projeto anda. Ao adicionar uma classe ou funcionalidade,
> atualize a seção **Estrutura** e marque o item no **Roadmap**.

## Tecnologias

| Item | Versão |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.0.8 |
| Build | Maven (wrapper incluso) |

## Como rodar

```bash
# subir a aplicação
./mvnw spring-boot:run

# compilar
./mvnw clean compile

# rodar os testes
./mvnw test
```

No Windows (PowerShell/cmd), use `mvnw.cmd` no lugar de `./mvnw`.

> **Memória:** gerar 1 milhão de requisições ocupa por volta de 150–200 MB de heap.
> Se der `OutOfMemoryError`, rode com `-Xmx2g`.

## Estrutura

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

### `GeradorRequisicoes`

Cria listas de `Requisicao` com valores aleatórios, parametrizável pelo tamanho.

```java
// atalho com a configuração padrão
List<Requisicao> cemMil   = GeradorRequisicoes.gerar(GeradorRequisicoes.CEM_MIL);
List<Requisicao> umMilhao = GeradorRequisicoes.gerar(GeradorRequisicoes.UM_MILHAO);

// semente fixa: gera sempre a mesma massa de dados
List<Requisicao> mesmosDados = GeradorRequisicoes.gerar(GeradorRequisicoes.CEM_MIL, 42L);

// configuração própria: 20 hospitais, 100–3000 ms, janela de 30 dias, semente 42
var gerador = new GeradorRequisicoes(20, 100, 3_000, 30, 42L);
List<Requisicao> dados = gerador.gerarLista(GeradorRequisicoes.UM_MILHAO);
```

Padrões: 10 hospitais (`HOSP-01`..`HOSP-10`), tempo entre 50 e 5000 ms,
datas espalhadas nos últimos 7 dias.

Usar a **semente fixa** é o jeito de comparar sequencial vs. threads sobre
exatamente os mesmos dados.

### `EstatisticaHospital`

Acumulador dos números de **um** hospital. Começa zerado e vai somando a cada
`adicionar(tempoAtendimento)`.

| Método | Retorno | Descrição |
| --- | --- | --- |
| `adicionar(long)` | `void` | soma mais uma requisição ao acumulador |
| `getTotalRequisicoes()` | `int` | quantas requisições entraram |
| `getSomaTempoAtendimento()` | `Long` | soma dos tempos, em ms |
| `getMedia()` | `Long` | média dos tempos, em ms (divisão inteira) |

> Não é thread-safe: na versão com threads, cada thread deve ter o **seu próprio**
> mapa de acumuladores e os resultados parciais são juntados no final.

### `EstatisticaService`

Onde ficam as contas. É um `@Service`, então o Spring injeta sozinho no controller.

```java
// percorre a lista inteira em uma thread só, agrupando por hospitalId
Map<String, EstatisticaHospital> porHospital = estatisticaService.calcularSequencial(requisicoes);

// quebra a lista em N pedaços (o último leva a sobra da divisão)
List<List<Requisicao>> pedacos = estatisticaService.dividirLista(requisicoes, 4);
```

`dividirLista` usa `subList`, ou seja, os pedaços são **views** da lista original —
não copiam os dados. Serve de base para o processamento com threads.

### `EstatisticaController`

Expõe o processamento via HTTP e já devolve o tempo gasto, que é o número que
interessa para a comparação.

```
GET /estatisticas/sequencial?tamanho=100000
```

Gera a massa de dados, roda `calcularSequencial` cronometrando com `System.nanoTime()`
e responde:

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

## Roadmap

- [x] `Requisicao` (POJO)
- [x] Gerador de dados fake (100 mil / 1 milhão)
- [x] `EstatisticaHospital` (acumulador por hospital)
- [x] Processamento sequencial das estatísticas
- [x] Divisão da lista em pedaços para as threads
- [ ] Processamento com threads (juntar os resultados parciais)
- [ ] Comparação de tempos entre as duas abordagens
- [ ] Exposição dos resultados via endpoint REST — falta o endpoint da versão com threads

## Convenções

- Tempos de atendimento sempre em **milissegundos**.
- Código e comentários em português.
- Indentação com tab, seguindo o padrão que o Spring Initializr gerou.
