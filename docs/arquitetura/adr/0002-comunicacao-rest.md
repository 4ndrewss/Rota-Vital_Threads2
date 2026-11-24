# ADR 0002: Comunicação por REST síncrono

Status: proposto

## Contexto

Reservar bolsas e emitir guias envolvem serviços distintos e podem falhar parcialmente, sem transação única entre bancos. O squad quer implementar e testar a saga com o menor custo de infraestrutura.

## Decisão

Usar REST síncrono entre os serviços, com a Requisição orquestrando a saga e acionando a compensação (liberar a reserva no Estoque) quando a emissão da guia falhar.

## Consequências

- **Melhora:** é simples de implementar, testar e implantar.
- **Piora:** acopla os serviços no tempo. Se o Estoque ou a Expedição estiver fora do ar, o atendimento não avança, e não há reprocessamento automático.
- **Alternativa descartada:** fila de mensagens, nesta fase, por exigir mais infraestrutura e operação. Fica como evolução futura.
