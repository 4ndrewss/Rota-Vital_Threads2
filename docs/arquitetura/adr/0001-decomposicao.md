# ADR 0001: Decomposição do Rota Vital

Status: proposto

## Contexto

A Seção A definiu quatro contextos (Estoque, Requisição, Expedição e Estatística), e a Seção E prevê de 0,5 req/s (piloto) a 150 req/s (estadual, no pico). O squad é pequeno e precisa de limites claros sem multiplicar serviços.

## Decisão

Projetar três microsserviços (Estoque, Requisição e Expedição), cada um com seu banco. A Estatística fica como módulo da Requisição.

## Consequências

- **Melhora:** permite evolução e escala isoladas por contexto.
- **Piora:** aumenta o número de integrações e o custo de operação.
- **Alternativa descartada:** um quarto serviço só para a Estatística, nesta fase.
