# 🏥 Rota Vital — Processamento Sequencial e Paralelo

Aplicação em Java com Spring Boot desenvolvida para comparar processamento sequencial e
paralelo. O sistema gera requisições de atendimento hospitalar, agrupa os dados por hospital e
calcula estatísticas utilizando uma ou várias threads.

Os testes utilizam cargas de 100 mil e 1 milhão de requisições, processadas com
1, 2, 4 e 8 threads.

---

## 🐳 Como executar com Docker

Só precisa do Docker instalado (não precisa de Java nem Maven na máquina). Sobe a API e o
PostgreSQL do zero, igual em qualquer máquina:

```bash
cp .env.example .env          # 1ª vez: cria o .env com as variáveis (ajuste a senha)
docker compose up -d --build  # compila, roda os testes e sobe API + banco
docker compose ps             # os dois serviços devem aparecer como (healthy)
```

A API fica em `http://localhost:8080` (ou na porta definida em `API_PORT` no `.env`).

```bash
docker compose exec api wget -qO- http://127.0.0.1:8081/actuator/health  # status interno da API e do banco
curl "localhost:8080/estatisticas/sequencial?tamanho=100000"
curl "localhost:8080/estatisticas/paralelo?tamanho=100000&threads=4"
```

| Comando | O que faz |
| --- | --- |
| `docker compose logs -f api` | acompanha o log da API |
| `docker compose down` | para e remove os containers — **os dados continuam** no volume |
| `docker compose down -v` | remove também o volume (apaga o banco) |

### Variáveis (`.env`)

| Variável | Para que serve |
| --- | --- |
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | banco criado pelo Postgres na 1ª subida e usado pela API |
| `API_PORT` | porta da API no seu computador (padrão `8080`) |
| `JAVA_TOOL_OPTIONS` | opções da JVM, ex.: `-XX:MaxRAMPercentage=75` |

O `.env` está no `.gitignore` e no `.dockerignore`: a senha não vai para o repositório nem
para dentro da imagem. O modelo versionado é o `.env.example`.

> O Postgres só lê `DB_NAME`/`DB_USER`/`DB_PASSWORD` quando cria o banco, na 1ª subida.
> Se mudar depois, rode `docker compose down -v` para recriar o volume.

### Decisões do container

**Imagem base: `eclipse-temurin`.** É a distribuição OpenJDK do Eclipse Adoptium: gratuita,
certificada (TCK) e a imagem Java oficial mais usada no Docker Hub. A versão é a **21**, a mesma
do `pom.xml`. O build usa `21-jdk` (precisa do compilador); a execução usa `21-jre-alpine`, que
só tem o necessário para rodar e é bem menor. O banco é o `postgres:16-alpine` (oficial, mesma
versão usada no desenvolvimento local).

**Por que multi-stage.** O `Dockerfile` tem dois estágios:

1. `build`: copia o código e roda `./mvnw package`, que **compila e roda os testes** (com H2 em
   memória, sem precisar do Postgres) e gera o jar. Se um teste falhar, a imagem não é gerada.
2. final: parte de uma imagem limpa só com a JRE e copia **apenas o jar** do 1º estágio.

Assim a imagem que roda não carrega JDK, Maven, código-fonte nem dependências de teste
(~390 MB contra ~950 MB do estágio de build), e menos coisa dentro é menos superfície de
ataque. Ela também roda com um usuário sem privilégio (`app`), não como root. O cache do Maven
(`~/.m2`) é montado como cache do BuildKit, então as dependências só são baixadas no 1º build.

**Onde ficam os dados.** No PostgreSQL, que grava em `/var/lib/postgresql/data` dentro do
container. Essa pasta é o **volume nomeado `rota-vital_db-data`**, gerenciado pelo Docker e
separado do container: sobrevive a `docker compose down`, a rebuilds e a reinícios. Só
`docker compose down -v` apaga. A API não guarda nada em disco (é *stateless*); tudo que ela
persiste (ex.: a tabela `execucao_historico`) vai para o banco.

**Como os serviços se encontram.** O Compose cria uma rede própria (`rota-vital_default`) e
coloca os dois serviços nela. Dentro dessa rede, cada serviço é encontrado **pelo nome** (DNS
interno do Docker): a API conecta em `jdbc:postgresql://db:5432/...`, onde `db` é o nome do
serviço no `compose.yaml`. Por isso:

- o banco **não publica porta** no seu computador, só a API o enxerga;
- só a API publica porta (`API_PORT` → `8080` do container);
- `depends_on` com `condition: service_healthy` faz a API só subir depois que o `pg_isready`
  do banco responde;
- a API também tem healthcheck: o Compose chama `127.0.0.1:8081/actuator/health` dentro do
  container, incluindo o teste de conexão com o banco (`"db": {"status": "UP"}`). A porta
  `8081` escuta somente em loopback e não é publicada nem acessível aos outros containers.
  `/actuator/health` na porta pública da API retorna 404 (ou 429 se a cota estiver esgotada).

---

## 🚀 Como executar sem Docker

Requisitos: Java 21, Spring Boot 4.0.8 e Maven Wrapper incluso no projeto. A aplicação espera
um PostgreSQL em `localhost:5432` (veja a seção 2 do [CONTRIBUTING](./CONTRIBUTING.md)); os
testes usam H2 em memória e não precisam de banco.

```bash
# iniciar a aplicação
./mvnw spring-boot:run

# compilar
./mvnw clean compile

# executar os testes
./mvnw test
```

No Windows, utilize `mvnw.cmd` no lugar de `./mvnw`.

Sem Docker, a saúde pode ser consultada na própria máquina em
`http://127.0.0.1:8081/actuator/health`; essa porta não aceita conexões de outras máquinas.

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
│   ├── EstatisticaController.java  # endpoints REST que disparam o processamento
│   ├── ApiExceptionHandler.java   # respostas de erro
│   └── HistoricoController.java   # consulta paginada do histórico
├── model/
│   ├── Requisicao.java             # POJO com os dados usados nos cálculos
│   ├── EstatisticaHospital.java    # acumulador por hospital (total, soma, média)
│   └── ExecucaoHistorico.java      # execução persistida no banco
├── repository/
│   └── ExecucaoHistoricoRepository.java # acesso paginado ao histórico
├── security/
│   └── LimiteRequisicoesFilter.java # controle de taxa e concorrência
├── service/
│   ├── EstatisticaService.java     # cálculo das estatísticas + divisão da carga
│   └── HistoricoService.java       # orçamento e registro de execuções
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

### Erros e histórico

Parâmetros inválidos (`tamanho <= 0` ou `threads <= 0`) respondem **400** com um JSON dizendo o
que errou, por exemplo `{"erro": "threads deve ser maior que zero"}`.

As execuções são gravadas na tabela `execucao_historico` (tipo, tamanho, threads e tempo)
enquanto houver capacidade e a gravação estiver disponível. O campo `historicoRegistrado`
informa se a execução foi salva. Os registros podem ser consultados por IDs maiores primeiro:

```http
GET /historico?pagina=0&limite=20
```

A resposta continua sendo um array JSON. `pagina` começa em zero (máximo 10000) e `limite`
aceita de 1 a 100 registros. Sem parâmetros, retorna somente os 20 primeiros registros.
Durante novas gravações, páginas posteriores podem mudar de posição; não são um retrato fixo.

### Limites de recursos

- **Carga:** de 1 a 1.000.000 requisições; **threads:** de 1 a 32. Valores fora dos limites
  retornam HTTP 400 antes de gerar a massa ou criar threads.
- **Taxa por IP de transporte:** 60 chamadas por janela fixa de um minuto, incluindo chamadas
  que a aplicação rejeita como inválidas. Um IP não consome a cota de outro. O filtro não confia
  em `Forwarded` ou `X-Forwarded-For`; clientes que compartilham um IP/NAT também compartilham a cota.
- **Concorrência:** 2 chamadas globais por padrão, no máximo 1 por IP. O limite por IP é metade
  do global, arredondada para baixo. A configuração global deve ser pelo menos 2 para reservar
  capacidade a outro cliente. Excesso recebe HTTP 429 e `Retry-After`; chamadas recusadas na
  admissão não consomem a cota por minuto. Para excesso de taxa, `Retry-After` informa os
  segundos restantes da janela, arredondados para cima (mínimo 1).
- **Saúde:** `/actuator/health` usa um servidor separado em `127.0.0.1:8081`, sem exposição
  pública. A sonda do Docker continua disponível quando as cotas da API estão esgotadas.
- **Memória do limitador:** até 4096 IPs acompanhados por instância. Somente cotas expiradas
  sem chamadas ativas são removidas. Ao atingir esse teto, novos IPs recebem 429 até haver vaga;
  as cotas dos IPs já acompanhados continuam isoladas. Há no máximo uma limpeza por segundo.
- **Armazenamento:** até 10.000 registros no histórico, preservados sem exclusão automática.
  Com o histórico cheio ou falha de gravação, o cálculo continua respondendo HTTP 200, com
  `historicoRegistrado: false` e um `aviso`. Quando a gravação funciona, o campo vale `true`.
  Para voltar a gravar após atingir o teto, um responsável deve exportar e remover registros
  antigos no banco. A consulta continua disponível; novas gravações não ultrapassam o teto.

As propriedades `rota.limites.requisicoes-por-minuto`, `rota.limites.requisicoes-simultaneas` e
`rota.historico.max-registros` ficam em `application.properties`. O teto do histórico aceita
configuração de 1 a 10000. A contagem e a inserção são serializadas até o commit na mesma instância.

Esses controles foram preparados para **uma instância da API**, sem autenticação de usuários.
`server.forward-headers-strategy=none` mantém a identificação pelo endereço de transporte.
Se houver proxy, a cota será compartilhada pelo IP desse proxy: só habilite encaminhamento de
IP com uma cadeia de proxies confiáveis e sem acesso direto de clientes ao backend.
Não use várias réplicas gravando no mesmo banco sem coordenar o orçamento de armazenamento e
as cotas em um componente compartilhado. Para exposição pública, configure também autenticação
e limites no gateway; o filtro local não protege contra saturação da rede ou ataques distribuídos.

---

## 🗺️ Story map do PI

O story map lê-se da esquerda para a direita (o fluxo de uso) e de cima para baixo (prioridade).
A linha do topo é o *backbone*: as grandes atividades de quem usa a API. Cada linha de baixo é
uma entrega.

| | Gerar massa de dados | Processar | Comparar desempenho | Histórico |
| --- | --- | --- | --- | --- |
| **Já entregue antes deste PI** | gerar N requisições | sequencial e paralelo | tempo gasto em ms | entidade e repositório criados, ainda sem uso |
| **Release 1 (este PI)** | — | **H1** validar `tamanho` e `threads` | — | **H2** registrar cada execução · **H3** consultar o histórico |
| **Release 2 (ideias, não feitas)** | semente fixa | limites ajustáveis por perfil de uso | comparação entre execuções | filtrar o histórico por tipo |

As histórias da Release 1 estão quebradas em tarefas, com o tipo de teste de cada uma, em
[docs/pi-tdd.md](./docs/pi-tdd.md).

---

## 🔄 Pipeline CI/CD, Deploy Automatizado e Rollback (E3)

A esteira de Integração e Entrega Contínua está definida em [`.github/workflows/ci.yml`](./.github/workflows/ci.yml) e é executada no **GitHub Actions** em três estágios encadeados (`needs:`):

1. **`1. Build e Testes` (`testes`)**: roda a cada `push` (em qualquer branch) e em todo `Pull Request` para a `main`. Compila o projeto com Java 21 (Eclipse Temurin) e executa a suíte completa (`./mvnw -B test` — testes unitários, de integração com H2 e de aceitação). Se qualquer teste falhar, a esteira fica vermelha e **bloqueia os estágios seguintes**.
2. **`2. Imagem Docker (GHCR)` (`imagem`)**: depende de `testes`. Valida a construção multi-stage do `Dockerfile` em branches/PRs e, quando o evento é um `push`/merge na `main`, publica a imagem no **GitHub Container Registry** (`ghcr.io/4ndrewss/rota-vital`) com duas tags:
   - `latest` (última versão estável da `main`);
   - `<sha-curto>` (tag imutável do commit para rastreabilidade e rollback).
3. **`3. Deploy Automatizado (Render)` (`deploy`)**: depende de `imagem` e só executa em merges/pushes na `main`. Consome o segredo `RENDER_DEPLOY_HOOK_URL` armazenado no cofre do **GitHub Actions Secrets** e dispara via `POST` a atualização automática do serviço na nuvem (**Render**). O serviço está conectado ao repositório GitHub e recebe `ref=<SHA completo da execução>` no hook para reconstruir o commit aprovado usando o `Dockerfile`. O **Auto-Deploy** nativo do Git fica desativado no provedor para garantir que a publicação passe pela esteira verde.

As execuções da `main` são serializadas. Execuções manuais usam um grupo de concorrência
separado, preservando os pushes que aguardam publicação.

### 🌐 Ambiente Público (Produção — Render)

A aplicação está publicada e acessível publicamente em:

🔗 **URL Pública:** [`https://rota-vital-api.onrender.com`](https://rota-vital-api.onrender.com)

Exemplos de chamadas diretas ao ambiente em nuvem:

```bash
# Processamento sequencial (100 mil registros)
curl "https://rota-vital-api.onrender.com/estatisticas/sequencial?tamanho=100000"

# Processamento paralelo (100 mil registros com 4 threads)
curl "https://rota-vital-api.onrender.com/estatisticas/paralelo?tamanho=100000&threads=4"

# Consulta paginada ao histórico persistido no PostgreSQL em nuvem
curl "https://rota-vital-api.onrender.com/historico?pagina=0&limite=20"
```

### Cofre de segredos e variáveis de ambiente

- **No GitHub (`Settings → Secrets and variables → Actions`)**:
  - `RENDER_DEPLOY_HOOK_URL`: URL original do webhook privado do Render, incluindo `?key=...`. O workflow acrescenta `&ref=<SHA completo>` a cada execução.
  - `GITHUB_TOKEN`: token automático de escopo restrito (`packages: write`) para publicar a imagem no GHCR.
- **No Render (`Environment Variables` do Web Service)**:
  - `DB_URL`, `DB_USER`, `DB_PASSWORD`: credenciais internas do banco PostgreSQL gerenciado no Render.
  - `PORT=8080` e `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`.

### ⏪ Como fazer Rollback

Caso uma versão publicada na `main` apresente regressão em produção, há **três formas documentadas de rollback**:

1. **Rollback imediato pelo painel do Render (1 clique, sem novo build)**:
   - Acesse o serviço `rota-vital-api` no painel do Render → menu **Events** (ou **Deploys**).
   - Localize o deploy anterior estável e clique em **Rollback to this deploy**. O Render restaura instantaneamente o container da versão anterior.
2. **Rollback por commit específico no Render**:
   - Use o recurso de deploy de um commit específico do Render e informe o SHA completo de um commit estável. O serviço reconstrói essa versão a partir do repositório GitHub usando o `Dockerfile`.
3. **Rollback via Git (GitHub Flow)**:
   ```bash
   git checkout main && git pull
   git checkout -b fix/rollback-versao-anterior
   git revert <SHA_DO_COMMIT_COM_PROBLEMA> --no-edit
   git push -u origin fix/rollback-versao-anterior
   ```
   Abra o Pull Request para a `main`. Após os checks verdes e o merge, a própria esteira reconstrói e publica a versão revertida automaticamente.

### 📸 Evidências da Esteira (E3)

As evidências exigidas para a etapa **E3 — Deploy Automatizado** estão arquivadas em [`docs/evidencias/`](./docs/evidencias/):

- **Execução vermelha barrando imagem e deploy** ([Run `#39083877310`](https://github.com/4ndrewss/Rota-Vital_Threads2/actions/runs/39083877310)): [`docs/evidencias/e3-pipeline-vermelho.png`](./docs/evidencias/e3-pipeline-vermelho.png)
- **Execução verde na `main` que publicou no GHCR e no Render** ([Run `#39092940462`](https://github.com/4ndrewss/Rota-Vital_Threads2/actions/runs/39092940462)): [`docs/evidencias/e3-pipeline-verde.png`](./docs/evidencias/e3-pipeline-verde.png)
- **Pull Request com checks da esteira e revisão** ([PR `#6`](https://github.com/4ndrewss/Rota-Vital_Threads2/pull/6)): [`docs/evidencias/e3-pr-pipeline.png`](./docs/evidencias/e3-pr-pipeline.png)
- **Proteção da branch `main` (Branch Protection Rule + Status Check `1. Build e Testes` obrigatório)**: [`docs/evidencias/e3-branch-protection.png`](./docs/evidencias/e3-branch-protection.png) e [`docs/evidencias/e3-pr-branch-protegida.png`](./docs/evidencias/e3-pr-branch-protegida.png)
- **URL pública no ar com persistência no PostgreSQL em nuvem** ([`https://rota-vital-api.onrender.com/historico`](https://rota-vital-api.onrender.com/historico)): [`docs/evidencias/e3-api-publica.png`](./docs/evidencias/e3-api-publica.png)

---

## 🧭 Tabela de Decisões do Projeto

| Decisão | Escolha adotada | Alternativas consideradas | Por quê |
| --- | --- | --- | --- |
| **Imagem base** | `eclipse-temurin:21-jdk` (build) + `eclipse-temurin:21-jre-alpine` (runtime) e `postgres:16-alpine` | `openjdk`, `amazoncorretto`, imagem única com JDK completo | Multi-stage reduz a imagem final (~390 MB vs ~950 MB), remove JDK/Maven/código-fonte do runtime e roda com usuário não-root (`app`). |
| **Persistência e dados** | PostgreSQL 16 em volume nomeado (`db-data`) no Compose e PostgreSQL gerenciado no Render | Banco apenas em memória (H2 em produção) ou bind mount local | Volume nomeado preserva o histórico (`execucao_historico`) entre reinícios/rebuilds dos containers sem depender de caminho fixo do host. |
| **Estratégia de testes (TDD)** | JUnit 5 + MockMvc + H2 em memória (unitários, integração com repositório e aceitação HTTP) | Testcontainers em todos os testes ou apenas testes manuais | H2 em memória permite rodar toda a suíte em segundos no build do Docker e no GitHub Actions sem exigir daemon externo de banco. |
| **Fluxo de branch** | **GitHub Flow** (`main` protegida + branches curtas `feat/`, `fix/`, `test/`, `chore/` + Pull Requests) | Git Flow (com `develop`/`release`) ou Trunk-Based com push direto na `main` | Simples para equipe acadêmica, exige revisão e check verde antes do merge e mantém a `main` sempre pronta para produção. |
| **CI/CD e Deploy** | **GitHub Actions** (3 estágios: `testes` → `imagem GHCR` → `deploy`) + **Render** (Web Service Docker + PostgreSQL) | Railway, Fly.io, AWS ECS ou deploy manual via CLI | Integração nativa com o repositório, publicação de imagens versionadas por SHA no GHCR e deploy gratuito acionado apenas com esteira 100% verde via Deploy Hook. |
| **Gestão de segredos** | `.env` ignorado no Git (`.env.example` versionado) + **GitHub Actions Secrets** + **Render Environment Variables** | Variáveis fixas no `application.properties` ou `compose.yaml` | Garante que nenhuma senha real ou webhook de deploy vaze no histórico do repositório ou nas camadas da imagem Docker. |

---

## 🤖 Uso de IA

Declaração exigida pelo [CONTRIBUTING](./CONTRIBUTING.md#9-uso-de-ia). Os testes, o código, o
workflow de CI e a documentação inicial do PI foram rascunhados pelo Claude (Claude Code) e precisam ser
lidos e validados pelo grupo antes do merge. O prompt usado e os limites do que foi verificado
estão em [docs/pi-tdd.md](./docs/pi-tdd.md#uso-de-ia). Os ajustes de limites de recursos,
paginação e Javadoc foram preparados pelo CodeRabbit Coding Agent a pedido do grupo e
também aguardam validação humana. A evolução da esteira para CI/CD completo com publicação no GHCR,
deploy automatizado no Render, guia de rollback e tabela de decisões (E3) foi estruturada com apoio
do Gemini (Antigravity) a partir do prompt: *"Estruturar o pipeline completo de CI/CD no GitHub Actions
(build e testes -> imagem Docker no GHCR com tags latest e SHA -> deploy via Deploy Hook no Render com
segredos no GitHub Secrets) e documentar decisões e rollback no README"*, revisado e validado pela equipe.
As correções de concorrência e de deploy por commit foram preparadas pelo CodeRabbit Coding Agent
a pedido do grupo: *"separar as execuções manuais e enviar o commit exato pelo parâmetro ref"*.

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
