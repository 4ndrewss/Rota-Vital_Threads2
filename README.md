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
├── RotaVitalApplication.java     # entrada da aplicação Spring Boot
├── model/
│   └── Requisicao.java           # POJO com os dados usados nos cálculos
└── util/
    └── GeradorRequisicoes.java   # gerador de massa de dados fake
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

## Roadmap

- [x] `Requisicao` (POJO)
- [x] Gerador de dados fake (100 mil / 1 milhão)
- [ ] Processamento sequencial das estatísticas
- [ ] Processamento com threads
- [ ] Comparação de tempos entre as duas abordagens
- [ ] Exposição dos resultados via endpoint REST

## Convenções

- Tempos de atendimento sempre em **milissegundos**.
- Código e comentários em português.
- Indentação com tab, seguindo o padrão que o Spring Initializr gerou.
