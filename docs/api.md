# 📡 API

## Endpoints

```http
GET /estatisticas/sequencial?tamanho=100000
GET /estatisticas/paralelo?tamanho=100000&threads=4
GET /historico?pagina=0&limite=20
```

Os endpoints de estatística geram a massa de dados, processam e cronometram **só o
processamento** (`System.nanoTime()`):

```json
{
  "tempoGastoMs": 42,
  "totalHospitais": 10,
  "resultado": {
    "HOSP-01": { "totalRequisicoes": 10012, "somaTempoAtendimento": 25318744, "media": 2528 }
  },
  "historicoRegistrado": true
}
```

O paralelo devolve também `threads`. Tempos de atendimento são sempre em **milissegundos**.

## Erros e histórico

- Parâmetros inválidos respondem **400** com o motivo, ex.: `{"erro": "threads deve ser maior que zero"}`.
- Cada execução é gravada em `execucao_historico` (tipo, tamanho, threads e tempo).
  `historicoRegistrado` diz se foi salva.
- `/historico` devolve um array JSON, IDs maiores primeiro. `pagina` começa em 0 (máx. 10000) e
  `limite` vai de 1 a 100 (padrão 20). Com gravações novas, as páginas podem se deslocar.

## Limites de recursos

| Limite | Valor |
| --- | --- |
| `tamanho` / `threads` | 1 a 1.000.000 / 1 a 32 (fora disso: 400, antes de gerar dados) |
| Taxa por IP | 60 chamadas por minuto (janela fixa); excesso: 429 com `Retry-After` |
| Concorrência | 2 chamadas globais, no máximo 1 por IP; excesso: 429 |
| IPs acompanhados | até 4096 por instância; acima disso, novos IPs recebem 429 até haver vaga |
| Histórico | até 10.000 registros, sem exclusão automática |

- O IP é o de transporte (`server.forward-headers-strategy=none`): `X-Forwarded-For` é ignorado e
  clientes atrás do mesmo NAT/proxy dividem a cota.
- Com o histórico cheio ou falha ao gravar, o cálculo continua com 200, `historicoRegistrado: false`
  e um `aviso`. Para voltar a gravar, exporte e remova registros antigos no banco.
- Configuração em `application.properties`: `rota.limites.requisicoes-por-minuto`,
  `rota.limites.requisicoes-simultaneas` (mínimo 2) e `rota.historico.max-registros` (1 a 10000).
- Os controles valem para **uma instância**, sem autenticação. Para várias réplicas ou exposição
  pública, use cotas compartilhadas e autenticação/limites no gateway.
