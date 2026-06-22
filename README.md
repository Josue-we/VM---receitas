# Pipeline de CI/CD - Receitas (GCS 2026/A)

Este pacote contém os arquivos pra montar o pipeline completo (A a H do enunciado)
usando só 4 containers Docker + 1 processo nativo na VM. Segue o passo a passo.

## 0. Onde colocar cada arquivo

Copie estes arquivos para a RAIZ do seu projeto `receitas` (sobrescrevendo os
equivalentes que já existem):

- `docker-compose.yml`
- `Dockerfile`
- `pom.xml`
- `scripts/init-multi-db.sh`
- `.gitea/workflows/pipeline.yml`
- `src/main/resources/application.properties`
- `src/main/resources/application-homolog.properties`
- `src/main/resources/application-prod.properties`
- `src/main/resources/db/migration/V1__criar_tabelas.sql`

Apague a linha `spring.jpa.hibernate.ddl-auto=update` do seu properties antigo
se ainda existir alguma cópia por aí - ela foi substituída por `validate` +
Flyway.

Adicione ao seu `.gitignore` (ele já existe no projeto, só acrescente a linha):

```
.env
```

## 1. Trocar a senha do Gmail vazada

A senha de app que estava no `application.properties` antigo já ficou exposta.
Revogue ela em myaccount.google.com -> Segurança -> Senhas de app, gere uma
nova, e guarde só localmente (nunca commitada) num arquivo `.env` na raiz do
projeto:

```
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-nova-senha-de-app
```

O `docker-compose.yml` já lê esse `.env` automaticamente.

## 2. Instalar Docker na VM (se ainda não tiver)

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo usermod -aG docker $USER
# depois disso, feche e abra a sessão de novo pra valer o grupo docker
```

## 3. Subir o Gitea e o banco

```bash
cd receitas
docker compose up -d gitea db
```

Acesse `http://IP_DA_VM:3000`, conclua a instalação inicial e crie sua conta
de administrador.

## 4. Criar o repositório e gerar o token do runner

1. No Gitea, crie um repositório novo (ex: `receitas`).
2. Vá em **Site Administration -> Actions -> Runners -> Create new runner**
   e copie o token gerado (token de uso único).

## 5. Instalar o act_runner direto na VM (modo host, sem Docker-in-Docker)

```bash
# baixe o binário (confira a última versão em gitea.com/gitea/act_runner/releases)
wget https://gitea.com/gitea/act_runner/releases/download/v0.3.0/act_runner-0.3.0-linux-amd64
chmod +x act_runner-0.3.0-linux-amd64
sudo mv act_runner-0.3.0-linux-amd64 /usr/local/bin/act_runner

act_runner register \
  --instance http://localhost:3000 \
  --token SEU_TOKEN_AQUI \
  --name vm-runner \
  --labels host:host \
  --no-interactive

act_runner daemon
```

Rodando assim, o runner executa os jobs como processos normais na própria VM
(usando o JDK que você já tem instalado, via `./mvnw`) - não precisa de
Docker dentro do Docker. Pra deixar isso permanente, crie um serviço systemd
chamando `act_runner daemon` no boot (posso te ajudar com esse arquivo se
quiser).

## 6. Conectar seu repositório local ao Gitea

```bash
git remote add gitea http://IP_DA_VM:3000/SEU_USUARIO/receitas.git
git push gitea main
```

O pipeline (`.gitea/workflows/pipeline.yml`) dispara sozinho: roda os 20
testes, o checkstyle, builda e sobe o ambiente de Homologação.

## 7. Promover para Produção

Quando a versão estiver validada em Homologação, crie e envie uma tag - esse
é o "clique"/passo manual que o trabalho pede antes de ir pra produção:

```bash
git tag v1.0.0
git push gitea v1.0.0
```

## 8. Conferir se está tudo de pé

```bash
docker compose ps
curl http://localhost:8081   # Homologação
curl http://localhost:8082   # Produção
```

## Sobre RAM (você tem 2GB)

`app-homolog` e `app-prod` não sobem automaticamente quando a VM reinicia
(de propósito). Pra liberar memória quando não estiver demonstrando:

```bash
docker compose stop app-homolog app-prod
```

E pra subir de volta na hora de apresentar:

```bash
docker compose start app-homolog app-prod
```
