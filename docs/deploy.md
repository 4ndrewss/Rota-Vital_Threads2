# 🔄 CI/CD, deploy e rollback (E3)

A esteira está em [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) e roda no GitHub
Actions em três estágios encadeados (`needs:`). Se um falha, os seguintes não começam.

1. **`1. Build e Testes`**: em todo push e em todo PR para a `main`. Roda `./mvnw -B test`
   (Java 21 Temurin) com a suíte completa.
2. **`2. Imagem Docker (GHCR)`**: valida o build do `Dockerfile`; em push na `main`, publica
   `ghcr.io/4ndrewss/rota-vital` com as tags `latest` e `<sha-curto>`.
3. **`3. Deploy Automatizado (Render)`**: só na `main`. Chama o Deploy Hook do Render com
   `ref=<SHA completo>`, e o Render reconstrói exatamente o commit aprovado. O Auto-Deploy do
   Render fica desligado, então só sobe o que passou na esteira.

As execuções da `main` são serializadas; execuções manuais usam outro grupo de concorrência.

**API pública:** <https://rota-vital-api.onrender.com> (ex.: `/historico?pagina=0&limite=20`).


| URL ou Caminho | O que faz |
|---|---|
| https://rota-vital-api.onrender.com | Página principal. Não exibe nada (retorna erro 404), pois é uma aplicação apenas backend, sem front. |
| https://rota-vital-api.onrender.com/historico | Retorna o histórico completo das execuções salvas, sem limite de exibição. |
| https://rota-vital-api.onrender.com/historico?pagina=0&limite=20 | Histórico paginado: lista as execuções salvas no banco, 20 por página (`pagina` = número da página, `limite` = itens por página). |
| https://rota-vital-api.onrender.com/estatisticas/sequencial?tamanho=100000 | Cálculo sequencial: processa 100000 elementos (`tamanho`) em uma única thread e grava o resultado no Postgres do Render. |
| https://rota-vital-api.onrender.com/estatisticas/paralelo?tamanho=100000&threads=4 | Cálculo paralelo: processa 100000 elementos usando 4 threads (`threads`) e grava o resultado no Postgres do Render. |


## Segredos

| Onde | O quê |
| --- | --- |
| GitHub → *Settings → Secrets and variables → Actions* | `RENDER_DEPLOY_HOOK_URL` (URL do hook com `?key=...`) |
| GitHub (automático) | `GITHUB_TOKEN` com `packages: write` para publicar no GHCR |
| Render → *Environment Variables* | `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT=8080`, `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75` |

## Rollback

1. **Painel do Render (sem build):** serviço `rota-vital-api` → *Events* → deploy estável →
   **Rollback to this deploy**.
2. **Commit específico no Render:** faça deploy informando o SHA completo de um commit estável.
3. **Git:** reverta pelo fluxo normal; após o merge, a esteira publica a versão revertida.

   ```bash
   git checkout main && git pull
   git checkout -b fix/rollback-versao-anterior
   git revert <SHA_DO_COMMIT_COM_PROBLEMA> --no-edit
   git push -u origin fix/rollback-versao-anterior
   ```

## Evidências

- Esteira vermelha barrando imagem e deploy ([run](https://github.com/4ndrewss/Rota-Vital_Threads2/actions/runs/39083877310)): [print](./evidencias/e3-pipeline-vermelho.png)
- Esteira verde na `main` publicando no GHCR e no Render ([run](https://github.com/4ndrewss/Rota-Vital_Threads2/actions/runs/39092940462)): [print](./evidencias/e3-pipeline-verde.png)
- PR com checks e revisão ([PR #6](https://github.com/4ndrewss/Rota-Vital_Threads2/pull/6)): [print](./evidencias/e3-pr-pipeline.png)
- Proteção da `main` com `1. Build e Testes` obrigatório: [regra](./evidencias/e3-branch-protection.png) · [PR bloqueado](./evidencias/e3-pr-branch-protegida.png)
- API pública gravando no PostgreSQL em nuvem: [print](./evidencias/e3-api-publica.png)
