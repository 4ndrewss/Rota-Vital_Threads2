# 🐳 Executar com Docker

Só precisa do Docker (não precisa de Java nem Maven). Sobe a API e o PostgreSQL do zero:

```bash
cp .env.example .env          # 1ª vez: cria o .env (ajuste a senha)
docker compose up -d --build  # compila, roda os testes e sobe API + banco
docker compose ps             # os dois serviços devem aparecer como (healthy)
```

A API fica em `http://localhost:8080` (ou na porta de `API_PORT`).

| Comando | O que faz |
| --- | --- |
| `docker compose exec api wget -qO- http://127.0.0.1:8081/actuator/health` | saúde da API e do banco |
| `docker compose logs -f api` | acompanha o log da API |
| `docker compose down` | remove os containers — **os dados continuam** no volume |
| `docker compose down -v` | remove também o volume (apaga o banco) |

## Variáveis (`.env`)

| Variável | Para que serve |
| --- | --- |
| `DB_NAME`, `DB_USER`, `DB_PASSWORD` | banco criado na 1ª subida e usado pela API |
| `API_PORT` | porta da API no seu computador (padrão `8080`) |
| `JAVA_TOOL_OPTIONS` | opções da JVM, ex.: `-XX:MaxRAMPercentage=75` |

O `.env` está no `.gitignore` e no `.dockerignore`; o modelo versionado é o `.env.example`.
O Postgres só lê `DB_*` ao criar o banco: se mudar depois, rode `docker compose down -v`.

## Decisões do container

- **Imagem base:** `eclipse-temurin` 21 (mesma versão do `pom.xml`): `21-jdk` no build e
  `21-jre-alpine` na execução. Banco: `postgres:16-alpine`.
- **Multi-stage:** o estágio `build` roda `./mvnw package` (compila e roda os testes com H2; se
  um teste falha, a imagem não é gerada). O final copia só o jar para uma imagem com JRE
  (~390 MB contra ~950 MB), sem JDK, Maven nem código-fonte, rodando como usuário `app` (não root).
  O `~/.m2` é cache do BuildKit, então as dependências só baixam no 1º build.
- **Dados:** ficam no volume nomeado `rota-vital_db-data` (`/var/lib/postgresql/data`), que
  sobrevive a `down`, rebuilds e reinícios. A API é *stateless*.
- **Rede:** os serviços se encontram pelo nome na rede do Compose (`jdbc:postgresql://db:5432/...`).
  Só a API publica porta; o banco não. A API só sobe depois que o `pg_isready` do banco responde
  (`depends_on: service_healthy`).
- **Healthcheck da API:** `127.0.0.1:8081/actuator/health` dentro do container, incluindo o banco.
  A porta `8081` escuta só em loopback; na porta pública, `/actuator/health` retorna 404.
