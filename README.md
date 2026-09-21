# Banking Demo

Demo bancário em Java 17 e Spring Boot com autenticação JWT stateless, uma conta por usuário e processamento assíncrono de depósitos e saques via Kafka.

## Funcionalidades do demo

- Cadastro de usuário, login JWT e criação de uma única conta por usuário.
- Solicitação de depósito e saque com validação de saldo e regras de negócio.
- Registro da requisição no **entrypoint** antes do processamento assíncrono.
- Publicação de eventos de movimentação no Kafka.
- Consumo dos eventos para efetivar a movimentação, atualizar o saldo e registrar o histórico.
- Consulta do estado de cada solicitação de movimentação.
- Migrações versionadas do banco com Flyway.
- Testes de integração com PostgreSQL e Kafka reais, inicializados por Testcontainers.

Todos os identificadores gerados pela aplicação são UUIDv7. O vínculo da conta é extraído do token; nenhum endpoint aceita `holderId` ou `userId` no corpo.

## Configuração

As configurações da aplicação ficam em [application.yml](src/main/resources/application.yml) e são agrupadas em `AppProperties`: nome, JWT, tópico Kafka e metadados OpenAPI. O segredo JWT pode ser definido sem alterar arquivos pelo ambiente:

```bash
export APP_JWT_SECRET='uma-chave-longa-e-aleatoria-para-o-ambiente'
```

Os demais valores podem ser sobrescritos pelas propriedades Spring equivalentes, por exemplo `APP_KAFKA_OPERATIONS_TOPIC` e `APP_KAFKA_CONSUMER_GROUP_ID`.

## Stack e dependências

| Área | Tecnologias |
| --- | --- |
| Linguagem e framework | Java 17, Spring Boot, Gradle |
| API | Spring Web MVC, Spring Validation, springdoc-openapi |
| Persistência | Spring Data JDBC, PostgreSQL, Flyway |
| Mensageria | Spring for Apache Kafka |
| Desenvolvimento local | Spring Boot DevTools e suporte a Docker Compose |
| Testes | JUnit 5, Spring Boot Test e Testcontainers (PostgreSQL e Kafka) |
| Produtividade | Lombok |

## Pré-requisitos

- JDK 17.
- Docker em execução para subir os serviços locais e executar os testes de integração.
- `make` (opcional; os comandos Gradle também podem ser usados diretamente).

## Como executar

```bash
# Mostra os comandos disponíveis
make help

# Executa a aplicação em desenvolvimento
make run

# Executa os testes, incluindo os que usam Testcontainers
make test
```

O Spring Boot detecta o arquivo [compose.yaml](compose.yaml) durante a execução de desenvolvimento e gerencia PostgreSQL e Kafka locais. Para a suíte de integração, a configuração de teste sobe containers isolados de PostgreSQL e Kafka automaticamente; não é necessário apontar para serviços locais.

Também é possível usar o Gradle Wrapper diretamente:

```bash
./gradlew bootRun
./gradlew test
```

## Endpoints e documentação

Com a aplicação em execução, a especificação OpenAPI e a interface interativa das rotas REST estão disponíveis em:

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [GraphiQL](http://localhost:8080/graphiql) para explorar e executar as consultas GraphQL.

Acessar [http://localhost:8080](http://localhost:8080) redireciona para o Swagger UI.

| Método | Rota | Resposta | Finalidade |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | `201 Created` | Cria um usuário e devolve JWT; não cria conta automaticamente. |
| `POST` | `/auth/login` | `200 OK` | Aceita e-mail e senha; devolve JWT, `accountId` (ou `null`) e `canCreateAccount`. |
| `POST` | `/accounts` | `201 Created` | Cria a única conta do usuário autenticado. Corpo vazio. |
| `GET` | `/accounts/{accountId}` | `200 OK` | Consulta a conta, somente se pertencer ao token. |
| `POST` | `/accounts/{accountId}/deposits` | `202 Accepted` | Solicita depósito na própria conta. |
| `POST` | `/accounts/{accountId}/withdrawals` | `202 Accepted` | Solicita saque na própria conta. |
| `GET` | `/requests/{requestId}` | `200 OK` | Consulta solicitação da própria conta. |
| `POST` | `/graphql` | `200 OK` | Executa consultas GraphQL, como o dashboard composto de uma conta. |

As rotas REST acima constam no Swagger/OpenAPI (`/swagger-ui/index.html` e `/v3/api-docs`). A rota `/graphql` possui schema próprio e não é exibida no Swagger; consulte o exemplo abaixo ou envie consultas para esse endpoint.

### Fluxo no Swagger

1. Execute `POST /auth/register` ou `POST /auth/login`. Ambos têm um exemplo de JSON editável diretamente no Swagger.
2. Copie `accessToken`, clique em **Authorize** e cole somente o token (ou `Bearer <token>`).
3. Se a resposta trouxer `accountId: null` e `canCreateAccount: true`, execute `POST /accounts` sem corpo uma única vez.
4. Use o `accountId` retornado nas chamadas de consulta, depósito e saque. A API retorna `403` para uma conta ou solicitação de outro usuário, `401` para token ausente/inválido e `409` se tentar criar outra conta.

Os schemas OpenAPI descrevem cada campo: `accessToken` é o Bearer JWT, `accountId` é a conta opcional no login, `canCreateAccount` indica a próxima ação possível, e `requestId` identifica uma operação assíncrona. Senhas nunca retornam na resposta nem são persistidas em texto puro.

### Consulta GraphQL para a tela de conta

O GraphQL é usado como uma API de leitura composta: uma tela pode buscar a conta, as dez movimentações mais recentes e as dez últimas solicitações em uma única consulta. Os comandos permanecem em REST para preservar o contrato assíncrono `202 Accepted`.
Valores monetários são retornados como `String`, evitando perda de precisão por ponto flutuante no cliente.

Com a aplicação em execução, envie a consulta para `POST /graphql`:

```graphql
query AccountDashboard($accountId: ID!) {
  accountOverview(accountId: $accountId) {
    account { id status balance }
    recentTransactions { id operationType amount balanceAfter createdAt }
    recentRequests { id operationType amount status processedAt rejectionReason }
  }
}
```

Variáveis:

```json
{ "accountId": "the-account-id-returned-by-POST-accounts" }
```

Substitua o valor inteiro pelo UUID retornado ao criar a conta; não envie literalmente `{accountId}`.

O endpoint aceita o envelope usual do GraphQL, por exemplo:

```bash
ACCOUNT_ID='cole-aqui-o-UUID-retornado-por-POST-accounts'

curl -X POST http://localhost:8080/graphql \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d "{\"query\":\"query(\$accountId: ID!) { accountOverview(accountId: \$accountId) { account { balance } recentTransactions { amount } } }\",\"variables\":{\"accountId\":\"${ACCOUNT_ID}\"}}"
```

Exemplo de fluxo completo:

```bash
# Cadastre o usuário, copie accessToken e informe-o no cabeçalho das chamadas seguintes.
curl -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"ana@example.com","password":"senha-segura-123"}'

# Crie a única conta desse usuário; guarde o id retornado em accountId.
curl -X POST http://localhost:8080/accounts \
  -H 'Authorization: Bearer {accessToken}'

# Solicite um depósito; guarde o requestId retornado.
curl -X POST http://localhost:8080/accounts/{accountId}/deposits \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {accessToken}' \
  -d '{"amount":125.50}'

# Consulte o resultado assíncrono.
curl -H 'Authorization: Bearer {accessToken}' http://localhost:8080/requests/{requestId}
curl -H 'Authorization: Bearer {accessToken}' http://localhost:8080/accounts/{accountId}
```

## Arquitetura

```mermaid
flowchart LR
    Client[Cliente / Swagger UI]
    API[Spring Boot API]
    Auth[Autenticação stateless<br/>JWT + BCrypt]
    Account[Contas e autorização<br/>dono da conta]
    Request[Registra solicitação<br/>PENDING]
    Kafka[(Kafka<br/>banking.operations)]
    Consumer[Consumidor de operações<br/>transação e idempotência]
    Database[(PostgreSQL<br/>Flyway)]
    GraphQL[GraphQL<br/>dashboard de leitura]

    Client -->|cadastro / login| Auth
    Auth -->|usuários e hash de senha| Database
    Client -->|Bearer JWT| API
    API --> Auth
    API --> Account
    Account -->|conta vinculada ao usuário| Database
    API -->|GET /graphql| GraphQL
    GraphQL -->|consulta autorizada| Database
    API -->|depósito / saque| Request
    Request -->|persiste PENDING| Database
    Request -->|publica evento| Kafka
    Kafka -->|consome evento| Consumer
    Consumer -->|atualiza saldo, transação e status| Database
    Consumer -.->|COMPLETED ou REJECTED| Database
```

### Como o fluxo funciona

1. O cliente cria usuário ou faz login. A API valida a senha BCrypt e devolve um JWT; o token contém o identificador do usuário, não o `accountId`.
2. Nas rotas protegidas, o filtro JWT autentica o usuário. Antes de consultar ou movimentar uma conta, a API confere se aquela conta pertence ao usuário do token.
3. Depósito e saque são assíncronos: a API persiste uma `banking_request` como `PENDING`, responde `202 Accepted` com `requestId` e publica o evento no Kafka.
4. O consumidor lê o evento, executa as regras na mesma transação do PostgreSQL, grava a movimentação e muda a solicitação para `COMPLETED` ou `REJECTED`.
5. Entregas repetidas são seguras: uma solicitação já finalizada não altera o saldo novamente.

| Integração | Responsabilidade |
| --- | --- |
| Swagger/OpenAPI | Explorar a API, enviar credenciais e informar o Bearer token. |
| PostgreSQL + Flyway | Persistir usuários, conta, solicitações e movimentações com schema versionado. |
| Kafka | Desacoplar o recebimento HTTP do processamento financeiro. A chave é o `accountId`, preservando a ordem por conta. |
| GraphQL | Buscar o dashboard da própria conta e históricos recentes em uma única consulta. |

## Logs para demonstração

O nível padrão (`INFO`) registra os marcos necessários para acompanhar uma operação pelos campos `requestId` e `accountId`: criação da conta, solicitação da operação, publicação e consumo do evento, conclusão, rejeição e reentrega idempotente. Falhas de publicação são registradas como `ERROR` com a exceção.

Para acompanhar o fluxo localmente, execute `make run` e observe a saída da aplicação. A suíte de integração também gera esses logs enquanto valida depósito, saque sem saldo e idempotência.

## Estrutura

```text
src/main/                         código da aplicação
src/test/                         testes unitários e de integração com Testcontainers
docs/requirements.md              requisitos funcionais e técnicos do demo
compose.yaml                      PostgreSQL e Kafka para desenvolvimento local
Makefile                          atalhos de desenvolvimento
```
