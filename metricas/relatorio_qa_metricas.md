# Relatório de Conclusão de QA & Métricas — Rota Vital (Threads)

Este documento sumariza a homologação completa de QA, execução dos testes ponta a ponta, coleta de métricas de desempenho e geração da planilha Excel solicitada pelo grupo no Trello.

---

## 1. Status de Homologação (QA)

| Verificação | Status | Observações |
| :--- | :---: | :--- |
| **Instalação do JDK 21** | ✅ Sucesso | Eclipse Adoptium OpenJDK 21 LTS configurado no ambiente. |
| **Build & Testes Unitários** | ✅ 7/7 Aprovados | Todos os testes em [`EstatisticaServiceTest`](file:///C:/Users/cgcms/.gemini/antigravity/scratch/Rota-Vital_Threads2/src/test/java/io/github/_ndrewss/rota_vital/service/EstatisticaServiceTest.java) passaram sem erros ou race conditions. |
| **Endpoint Sequencial** | ✅ 200 OK | `GET /estatisticas/sequencial?tamanho={tamanho}` validado. |
| **Endpoint Paralelo** | ✅ 200 OK | `GET /estatisticas/paralelo?tamanho={tamanho}&threads={N}` validado para 2, 4 e 8 threads. |
| **Consistência dos Dados** | ✅ Idênticos | Resultados de soma, total e média de atendimento por hospital batem exatamente entre sequencial e paralelo. |

---

## 2. Tabela de Tempos Médios & Speedup

Foram executadas **5 rodadas consecutivas** para cada cenário para eliminar ruídos de inicialização da JVM (Warmup) e variações do sistema operacional:

### Tabela Consolidada (Média em milissegundos)

| Volume de Registros | Sequencial (1 thread) | 2 Threads | 4 Threads | 8 Threads |
| :--- | :---: | :---: | :---: | :---: |
| **100.000 requisições** | 7.80 ms | 10.40 ms | 8.00 ms | **4.00 ms** |
| **1.000.000 requisições** | 249.80 ms | 148.80 ms | 88.40 ms | **63.40 ms** |

### Speedup ($S = T_{sequencial} / T_{paralelo}$) e Eficiência ($E = S / P$)

| Volume | 1 Thread | 2 Threads | 4 Threads | 8 Threads |
| :--- | :---: | :---: | :---: | :---: |
| **100k Speedup** | 1.00x (100%) | 0.75x (37.5%) | 0.98x (24.5%) | **1.95x (24.4%)** |
| **1M Speedup** | 1.00x (100%) | **1.68x (84.0%)** | **2.83x (70.8%)** | **3.94x (49.3%)** |

> [!NOTE]
> Em **100k requisições**, o laço sequencial é tão veloz (< 10 ms) que o custo de gerenciamento das threads no `ExecutorService` e troca de contexto neutraliza o ganho até 4 threads. Já em **1 milhão de requisições**, a carga de trabalho de CPU é suficiente para que as threads paralelas escalem com vigor, alcançando quase **4x de aceleração com 8 threads**.

---

## 3. Arquivos Entregues

1. **Planilha Excel Completa (.xlsx)**:
   - Caminho local: [`metricas_desempenho_rota_vital.xlsx`](file:///C:/Users/cgcms/.gemini/antigravity/scratch/Rota-Vital_Threads2/metricas_desempenho_rota_vital.xlsx)
   - Contém 3 abas estilizadas:
     - `Resumo & Speedup`: Tabela de tempos, fórmulas nativas de speedup e eficiência, e **2 gráficos nativos do Excel** (Gráfico de Barras de Tempo x Threads e Gráfico de Linha de Speedup).
     - `Rodadas Detalhadas (QA)`: Todas as 5 rodadas de medição, médias, máximos e mínimos.
     - `Justificativa & Análise`: Textos prontos para os Cards 18 e 19.

2. **Dashboard Interativo em HTML**:
   - Disponível em: [`dashboard_metricas.html`](file:///C:/Users/cgcms/.gemini/antigravity/brain/20fca962-5037-4af5-b53c-0ad3d29e0c2d/dashboard_metricas.html)

---

## 4. Cards do Trello que Podem Ser Concluídos

| Card | Título | Ação Realizada |
| :---: | :--- | :--- |
| **#14** | *Rodar testes: 100k e 1M registros × sequencial/2/4/8 threads* | 5 rodadas executadas e registradas. |
| **#15** | *Montar tabela de tempos* | Tabela estruturada com linhas por tamanho e colunas por threads. |
| **#16** | *Montar gráfico (Excel/Sheets resolve)* | Gráficos nativos gerados e incorporados na planilha. |
| **#17** | *Calcular speedup* | Fórmulas de speedup ($T_1/T_p$) e eficiência inseridas na planilha. |
| **#18** | *Escrever justificativa* | Redigida explicação completa sobre Big-O, gargalo CPU-bound e partição comutativa. |
| **#19** | *Escrever análise* | Redigida análise de 15-20 linhas abordando Lei de Amdahl, analogia da Mesa DJ e arquitetura futura. |
