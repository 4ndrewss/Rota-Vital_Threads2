# 🤝 Como contribuir

Guia rápido para quem for mexer no projeto. A ideia é todo mundo seguir o mesmo fluxo,
porque ele também é parte da entrega (fluxo de branch, histórico de commits e testes na esteira).

## 1. Pré-requisitos

| Ferramenta | Para quê |
| --- | --- |
| Java 21 | compilar e rodar a aplicação |
| Git | versionamento |
| Docker | rodar a aplicação localmente (API + PostgreSQL) |

O Maven já vem no projeto (`./mvnw`, ou `mvnw.cmd` no Windows), não precisa instalar.

## 2. Rodando localmente

Tudo pelo Docker Compose (API + PostgreSQL):

```bash
cp .env.example .env          # só na 1ª vez
docker compose up -d --build
```

A API fica em `http://localhost:8080`. Detalhes (variáveis, volume, decisões) no
[docs/docker.md](./docs/docker.md).

**Rodando a API pela IDE / `./mvnw`** (para debugar): a aplicação espera um PostgreSQL em
`localhost:5432`. Suba só um banco avulso:

```bash
docker run --name rotavital-db -e POSTGRES_DB=rotavital -e POSTGRES_USER=rotavital -e POSTGRES_PASSWORD=rotavital -p 5432:5432 -d postgres:16
./mvnw spring-boot:run
```

Nas próximas vezes, basta `docker start rotavital-db`.

As credenciais acima são **apenas para desenvolvimento local**. Nenhum segredo real entra no repositório.

## 3. Rodando os testes

```bash
./mvnw test
```

Os testes usam o banco em memória (H2), então **não precisam de Docker nem do PostgreSQL**.
Rode sempre antes de dar push. Se estiver vermelho, não suba.

## 4. Fluxo de trabalho

Usamos **GitHub Flow**: a `main` é a versão estável e todo trabalho acontece em branches curtas.

1. Pegue um card no Trello e se associe a ele.
2. Atualize sua `main` local e crie uma branch a partir dela:
   ```bash
   git checkout main
   git pull
   git checkout -b feat/nome-curto-da-tarefa
   ```
3. Trabalhe em commits pequenos (veja a seção 5).
4. Rode `./mvnw test`.
5. Suba a branch e abra um **Pull Request** para a `main`:
   ```bash
   git push -u origin feat/nome-curto-da-tarefa
   ```
6. No PR, descreva o que mudou e cite o card do Trello.
7. Depois do merge, apague a branch.

**Nunca dê push direto na `main`.** Quando a proteção da `main` e a esteira estiverem configuradas (E3),
o PR só poderá ser mergeado com os checks verdes.

### Nome das branches

| Prefixo | Quando usar | Exemplo |
| --- | --- | --- |
| `feat/` | funcionalidade nova | `feat/historico-execucoes` |
| `fix/` | correção de bug | `fix/validacao-threads` |
| `test/` | só testes | `test/integracao-historico` |
| `docs/` | documentação | `docs/readme-pipeline` |
| `chore/` | configuração, Docker, pipeline | `chore/dockerfile` |

## 5. Commits

Formato: `tipo: descrição curta no imperativo`, com os mesmos tipos das branches
(`feat`, `fix`, `test`, `docs`, `chore`).

### Regra do TDD (importante para a E2)

Para cada regra de negócio, o histórico deve mostrar o teste **antes** do código,
em **commits separados**:

```
test: valida que threads menor ou igual a zero retorna erro   ← teste escrito, falhando (vermelho)
feat: implementa validação do parâmetro threads                ← código que faz passar (verde)
```

Não junte teste e implementação no mesmo commit, senão perdemos a evidência red → green.

## 6. Pull Requests

- Título claro, com o mesmo padrão dos commits.
- Descrição: o que foi feito, como testar e o card do Trello.
- PRs pequenos são revisados mais rápido. Prefira vários PRs curtos a um grande.
- Se houver pipeline, só faça o merge com tudo verde.

## 7. Segredos e configuração

- Nunca commite senhas, tokens ou arquivos `.env`.
- Variáveis novas entram no `.env.example` (sem valores reais).
- No pipeline, segredos ficam no cofre da ferramenta (GitHub Secrets).

## 8. Evidências

Prints e links de execuções do pipeline e de PRs ficam em `docs/evidencias`, com nomes
descritivos (ex.: `pr-card-0-merged.png`, `pipeline-vermelho.png`, `pipeline-verde.png`).

## 9. Uso de IA

Uso de IA é permitido, mas **declarado**: registre no README os principais prompts usados.
A IA rascunha, mas quem assina e valida é o grupo. Antes de commitar, leia e entenda o que foi gerado.