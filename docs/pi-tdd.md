# PI — regras nascem dos testes

Este documento acompanha o PI: o story map está no [README](../README.md#️-story-map-do-pi) e
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
| T3.1 | o repositório devolve as execuções da mais nova para a mais antiga | integração (banco) |
| T3.2 | `GET /historico` devolve essa lista em JSON | aceitação (com banco) |
