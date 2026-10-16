# PI — regras nascem dos testes

Este documento acompanha o PI: o story map está no [README](../README.md) e
aqui ficam as histórias da Release 1 quebradas em tarefas. O fluxo de commits segue o
[CONTRIBUTING](../CONTRIBUTING.md): primeiro o commit `test:` (o teste que ainda falha), depois
o commit `feat:` (o código que faz passar).

## Histórias e tarefas (Release 1)

### H1 — Validar os parâmetros da requisição

*Como pessoa que chama a API, quero receber um erro claro (400) quando mando `tamanho` ou
`threads` inválidos, em vez de um erro interno (500).*

| Tarefa | Regra | Tipo de teste |
| --- | --- | --- |
| T1.1 | `calcularParalelo` com `threads <= 0` lança `IllegalArgumentException` | unitário |
| T1.2 | API responde 400 com `{"erro": "..."}` para `threads <= 0` | aceitação |
| T1.3 | API responde 400 com `{"erro": "..."}` para `tamanho <= 0` | aceitação |

### H2 — Registrar cada execução no histórico

*Como pessoa que compara desempenho, quero que cada execução fique guardada no banco, para
não perder os tempos medidos.*

| Tarefa | Regra | Tipo de teste |
| --- | --- | --- |
| T2.1 | execução sequencial é gravada com tipo `SEQUENCIAL` e `threads = 1` | unitário |
| T2.2 | execução paralela é gravada com tipo `PARALELO` e o número de threads pedido | unitário |
| T2.3 | chamada válida grava 1 linha no banco; chamada inválida não grava nada | aceitação (com banco) |

### H3 — Consultar o histórico

*Como pessoa que compara desempenho, quero listar as execuções passadas, da mais recente para a
mais antiga.*

| Tarefa | Regra | Tipo de teste |
| --- | --- | --- |
| T3.1 | o repositório devolve uma página de execuções por IDs decrescentes | integração (banco) |
| T3.2 | `GET /historico` devolve a página solicitada em JSON | aceitação (com banco) |

## Teste unitário de cada tarefa de regra

Além do teste principal de cada tarefa (tabelas acima), toda tarefa de regra tem pelo menos um
**teste unitário**: ele chama a regra direto, sem subir Spring nem banco.

| Tarefa | Teste unitário | Como nasceu |
| --- | --- | --- |
| T1.1 | `EstatisticaServiceTest` (threads menor ou igual a zero e acima de 32) | vermelho → verde |
| T1.2 | `ApiExceptionHandlerTest` | **nasceu verde** (código já existia) |
| T1.3 | `ValidadorParametrosTest` (tamanho de 1 a 1000000) | vermelho → verde |
| T2.1 | `HistoricoServiceTest` (`registrarSequencial`) | vermelho → verde |
| T2.2 | `HistoricoServiceTest` (`registrarParalelo`) | vermelho → verde |
| T2.3 | `EstatisticaControllerTest` (válido grava, inválido não grava) | **nasceu verde** |
| T3.1 | `HistoricoServiceTest` (`listar`: página, limite e teto de registros) | **nasceu verde** |
| T3.2 | `HistoricoControllerTest` | **nasceu verde** |

**Por que alguns "nasceram verdes".** Esses testes unitários foram escritos *depois* do código,
que já existia (parte dele veio da revisão do CodeRabbit). Um teste escrito depois do código não
tem como falhar antes: ele é um *teste de caracterização*. Protege contra erros futuros, mas
**não prova** que a regra nasceu do teste. A prova de vermelho → verde está só nas tarefas
marcadas "vermelho → verde", e mesmo ela precisa ser conferida rodando `./mvnw test` em cada
commit (veja "Como conferir o vermelho → verde" abaixo).

**O par novo desta etapa (T1.3).** A regra de `tamanho` estava escondida num método privado do
controller, que não dá para chamar num teste unitário. Então o commit `test:` escreveu o teste da
regra antes (falhando, porque a classe `ValidadorParametros` ainda não existia) e o commit
`feat:` seguinte criou a classe e fez o controller usá-la. O comportamento da API não mudou.

## Anotações do desenvolvimento

### Quais regras foram testadas primeiro, e por quê (risco)

A ordem seguiu o risco: primeiro o que pode quebrar a API ou estragar o dado que a comparação
de desempenho usa, por último o que é só apresentação.

1. **T1.1, `threads <= 0` no service.** Maior risco e menor esforço: qualquer pessoa digita
   `threads=0` na URL e a API estourava `ArithmeticException` (divisão por zero), virando erro 500.
   É também a base da T1.2.
2. **T1.2 e T1.3, resposta 400.** Dependem da T1.1. O risco é o cliente receber erro interno do
   servidor, sem saber que o erro foi dele.
3. **T2.1 e T2.2, o que o histórico grava.** O objetivo do projeto é comparar sequencial e
   paralelo. Se o sequencial fosse gravado com `threads` diferente de 1, ou com o tipo trocado, a
   comparação futura ficaria errada sem ninguém perceber.
4. **T2.3, gravação ponta a ponta.** Garante que a gravação realmente acontece no fluxo da API e
   que chamada com erro não deixa lixo no banco.
5. **T3.1 e T3.2, ordenação da consulta.** Risco menor: se a ordem falhar, só a leitura fica
   confusa, nenhum dado se perde. Por isso ficou por último.

### O que ficou sem teste

- **Limites de recursos (atualização após revisão).** A API agora limita tamanho a 1000000,
  threads a 32, consulta a 100 registros por página e armazenamento a 10000 registros.
  A taxa e a concorrência são controladas por instância. Há testes para esses controles;
  gateway, autenticação e coordenação entre réplicas continuam fora desta implementação.
- **Valores que não são números** (`tamanho=abc`). O Spring já responde 400 sozinho, e isso não foi
  coberto por teste nosso.
- **O valor de `tempoGastoMs`.** Depende da máquina, então só dá para testar que ele existe, e
  isso não foi feito.
- **PostgreSQL de verdade.** Os testes rodam no H2 em memória. Diferenças entre os dois bancos
  não são cobertas.
- **Ordenação por data.** O `id` é gerado em sequência crescente, mas a consulta ordena pelo
  `id` em ordem crescente (maiores primeiro), não por `criadoEm`, para o teste não depender
  de relógio.
- **O teste "chamada inválida não grava"** já passava antes da implementação (é uma trava
  contra regressão, não um teste vermelho).
- **`IllegalArgumentException` vira 400 em qualquer lugar da API.** Se algum código interno lançar
  essa exceção por outro motivo, ele também vira 400, mesmo sendo um bug do servidor.

### Uso de IA

- **Ferramenta:** Claude (Claude Code, modelo Sonnet 5.5).
- **Prompt principal:** o checklist do PI (print do Notion com story map, tarefas, testes unitário,
  integração e aceitação, histórico red → green, CI e anotações) e o pedido "crie uma branch e me
  explique tudo oq vc fez pra eu entender".
- **O que a IA fez:** propôs as histórias H1 a H3 e as tarefas, escreveu os testes antes do código
  (commits `test:`) e o código mínimo para passar (commits `feat:`), criou o workflow de CI e
  escreveu estas anotações.
- **Limite importante:** na sessão em que isto foi escrito o Claude **não conseguiu rodar
  `./mvnw test`** (o ambiente dele não acessa o Maven Central). O vermelho e o verde de cada
  commit descritos aqui são o **esperado**, não foram observados. Quem assina precisa rodar os
  testes e conferir.

### Como conferir o vermelho → verde

Em cada par de commits, o `test:` deve falhar e o `feat:` seguinte deve passar:

```bash
git log --oneline                 # veja os pares test: / feat:
git checkout <sha-do-commit-test> && ./mvnw test   # esperado: falha
git checkout <sha-do-commit-feat> && ./mvnw test   # esperado: passa
git checkout feat/pi-tdd-validacao-historico       # volta para a ponta da branch
```

Se algum teste ficar de um jeito diferente do esperado, corrija e anote aqui.

### Ajustes após os alertas do CodeRabbit

A pedido do grupo, o CodeRabbit Coding Agent preparou Javadoc para os métodos do PR,
paginação no banco, limites de tamanho/threads, admissão por taxa/concorrência e um orçamento
de registros que preserva o histórico existente. Os valores e a operação estão no README.
Os testes novos foram escritos antes da implementação, mas os commits aguardam validação
humana; esta atualização não comprova o vermelho/verde dos pares históricos anteriores.
