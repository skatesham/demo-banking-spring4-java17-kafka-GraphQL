# Requisitos — Banking Demo

## Objetivo

Disponibilizar um demo de banco com contas, autenticação e movimentações de depósito e saque. A API recebe a intenção do cliente, registra a requisição e delega o processamento da movimentação a um consumidor Kafka.

## Escopo funcional

### Usuário e conta

- Cadastrar usuário com e-mail único e senha protegida por hash BCrypt.
- Fazer login stateless: a resposta devolve JWT Bearer e o `accountId` do usuário, quando existir.
- Criar uma conta para o cliente autenticado somente quando ele ainda não possuir uma.
- Consultar os dados da própria conta, incluindo saldo disponível.
- Cada usuário possui no máximo uma conta; a restrição é garantida por `UNIQUE (account.user_id)`.
- IDs criados pela aplicação usam UUIDv7.

### Autenticação e autorização

- Autenticar o cliente antes do acesso a recursos protegidos.
- Garantir que o cliente só consulte e movimente contas às quais tenha acesso.
- Retornar `401 Unauthorized` para credenciais inválidas ou ausentes e `403 Forbidden` quando não houver permissão.

### Depósito

- Receber conta de destino e valor positivo.
- Registrar a requisição com um identificador único antes de publicar o evento.
- Ao consumir o evento, creditar o valor na conta e gravar o histórico da movimentação.

### Saque

- Receber conta de origem e valor positivo.
- Registrar a requisição e publicar o evento de saque.
- Ao consumir o evento, validar conta ativa e saldo suficiente antes de debitar o valor.
- Recusar operações que resultem em saldo negativo.

## Fluxo assíncrono

1. O **entrypoint** HTTP valida o formato, autentica o cliente e registra a requisição com status `PENDING`.
2. O entrypoint publica no Kafka um evento contendo o ID da requisição, a conta, o tipo da operação e o valor.
3. O consumidor Kafka processa o evento em transação: valida as regras, atualiza o saldo, registra a movimentação e atualiza o status para `COMPLETED` ou `REJECTED`.
4. O cliente consulta o status pelo ID da requisição ou consulta o saldo/histórico da conta.

O processamento deve ser idempotente: uma nova entrega de um evento já concluído não pode duplicar crédito ou débito.

## Contrato mínimo da API

| Método | Rota | Finalidade |
| --- | --- | --- |
| `POST` | `/auth/register` | Cria usuário e devolve JWT, sem criar conta automaticamente. |
| `POST` | `/auth/login` | Autentica e devolve JWT, `accountId` e `canCreateAccount`. |
| `POST` | `/accounts` | Cria a única conta do usuário no token; não recebe holderId. |
| `GET` | `/accounts/{accountId}` | Consulta a conta e seu saldo. |
| `POST` | `/accounts/{accountId}/deposits` | Solicita um depósito. |
| `POST` | `/accounts/{accountId}/withdrawals` | Solicita um saque. |
| `GET` | `/requests/{requestId}` | Consulta o status de uma solicitação. |

Para depósito e saque, a resposta inicial deve ser `202 Accepted`, com `requestId` e status `PENDING`.

## Persistência

- PostgreSQL como banco relacional.
- Flyway para criar e versionar o schema.
- Entidades mínimas: `app_user`, `account`, `banking_request` e `transaction`.
- Saldo e criação de movimentação devem ser persistidos na mesma transação do consumidor.

## Mensageria

- Kafka como canal entre o entrypoint e o processador.
- Tópico de operações bancárias, por exemplo `banking.operations`.
- A chave da mensagem deve preservar a ordenação por conta (por exemplo, `accountId`).
- Eventos inválidos ou que excederem as tentativas devem ter tratamento observável, como tópico de erro/DLT.

## Testes e execução local

- Testes de integração devem usar Testcontainers para iniciar PostgreSQL e Kafka reais, sem dependência de instalações locais.
- A configuração de teste deve expor os containers ao contexto Spring por `@ServiceConnection`.
- O suporte do Spring Boot a Docker Compose deve ser usado no desenvolvimento local; o `compose.yaml` fornece o PostgreSQL.
- Cobrir, no mínimo: autenticação, autorização, depósito processado, saque processado, saldo insuficiente e idempotência de evento.
