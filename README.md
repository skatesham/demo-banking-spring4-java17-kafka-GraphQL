# Banking Demo

Demo bancário em Java 17 e Spring Boot que cria contas e processa depósitos e saques de forma assíncrona com Kafka.

## Funcionalidades do demo

- Cadastro e consulta de contas bancárias.
- Solicitação de depósito e saque com validação de saldo e regras de negócio.
- Registro da requisição no **entrypoint** antes do processamento assíncrono.
- Publicação de eventos de movimentação no Kafka.
- Consumo dos eventos para efetivar a movimentação, atualizar o saldo e registrar o histórico.
- Consulta do estado de cada solicitação de movimentação.
- Migrações versionadas do banco com Flyway.
- Testes de integração com PostgreSQL e Kafka reais, inicializados por Testcontainers.

> O demo não inclui autenticação/autorização nem histórico de transações por endpoint. Esses itens permanecem como evolução prevista em [docs/requirements.md](docs/requirements.md).

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
| `POST` | `/accounts` | `201 Created` | Cria uma conta para o `holderId` informado. |
| `GET` | `/accounts/{accountId}` | `200 OK` | Consulta a conta, incluindo o saldo atual. |
| `POST` | `/accounts/{accountId}/deposits` | `202 Accepted` | Registra e publica uma solicitação de depósito. |
| `POST` | `/accounts/{accountId}/withdrawals` | `202 Accepted` | Registra e publica uma solicitação de saque. |
| `GET` | `/requests/{requestId}` | `200 OK` | Consulta o estado da solicitação: `PENDING`, `COMPLETED` ou `REJECTED`. |
| `POST` | `/graphql` | `200 OK` | Executa consultas GraphQL, como o dashboard composto de uma conta. |

As rotas REST acima constam no Swagger/OpenAPI (`/swagger-ui/index.html` e `/v3/api-docs`). A rota `/graphql` possui schema próprio e não é exibida no Swagger; consulte o exemplo abaixo ou envie consultas para esse endpoint.

### Consulta GraphQL para a tela de conta

O GraphQL é usado como uma API de leitura composta: uma tela pode buscar a conta, as dez movimentações mais recentes e as dez últimas solicitações em uma única consulta. Os comandos permanecem em REST para preservar o contrato assíncrono `202 Accepted`.
Valores monetários são retornados como `String`, evitando perda de precisão por ponto flutuante no cliente.

Com a aplicação em execução, envie a consulta para `POST /graphql`:

```graphql
query AccountDashboard($accountId: ID!) {
  accountOverview(accountId: $accountId) {
    account { id holderId status balance }
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
  -d "{\"query\":\"query(\$accountId: ID!) { accountOverview(accountId: \$accountId) { account { balance } recentTransactions { amount } } }\",\"variables\":{\"accountId\":\"${ACCOUNT_ID}\"}}"
```

Exemplo de fluxo completo:

```bash
# Crie a conta; guarde o id retornado em accountId.
curl -X POST http://localhost:8080/accounts \
  -H 'Content-Type: application/json' \
  -d '{"holderId":"11111111-1111-1111-1111-111111111111"}'

# Solicite um depósito; guarde o requestId retornado.
curl -X POST http://localhost:8080/accounts/{accountId}/deposits \
  -H 'Content-Type: application/json' \
  -d '{"amount":125.50}'

# Consulte o resultado assíncrono.
curl http://localhost:8080/requests/{requestId}
curl http://localhost:8080/accounts/{accountId}
```

## Arquitetura

```text
Cliente -> API / entrypoint -> registra requisição -> Kafka
                                                -> consumidor -> valida e processa
                                                               -> PostgreSQL
```

O entrypoint responde ao cliente com o identificador da solicitação e publica um evento. O consumidor aplica a operação de maneira idempotente e persiste o resultado.

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
