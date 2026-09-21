# Regras de Arquitetura

## Objetivo

Este documento define as regras de organização e arquitetura do projeto.

A arquitetura deve priorizar:

* clareza;
* baixo acoplamento;
* alta coesão;
* regras de negócio explícitas;
* facilidade de testes;
* previsibilidade transacional;
* evolução por funcionalidade;
* simplicidade antes de abstrações desnecessárias.

A arquitetura adotada é baseada em:

**Package by Feature + Vertical Slice + Domain Model leve**

Não é objetivo implementar Clean Architecture ou Hexagonal Architecture de forma purista.

---

# 1. Organização principal

O código deve ser organizado por domínio ou funcionalidade.

```text
com.example.banking
│
├── account/
├── operation/
├── requeststatus/
├── security/
├── config/
└── shared/
```

Evitar organização global por tipo técnico:

```text
controller/
service/
repository/
entity/
dto/
```

Classes relacionadas à mesma funcionalidade devem permanecer próximas.

---

# 2. Estrutura interna de uma feature

Quando necessário, uma feature pode ser dividida em:

```text
feature/
├── api/
├── application/
├── domain/
└── infrastructure/
```

Nem toda feature precisa obrigatoriamente possuir todas essas pastas.

Criar subdivisões somente quando existirem responsabilidades reais que justifiquem a separação.

---

# 3. `api`

Responsável pela entrada e saída da aplicação.

Pode conter:

* controllers;
* request DTOs;
* response DTOs;
* validações de formato;
* adaptação HTTP.

Não deve conter:

* regra de negócio;
* acesso direto ao banco;
* chamadas diretas ao Kafka;
* controle transacional relevante;
* lógica de domínio.

Controllers devem ser pequenos e delegar a execução para a camada `application`.

---

# 4. `application`

Representa os casos de uso da aplicação.

Exemplos conceituais:

```text
CreateAccount
GetAccount
RequestDeposit
RequestWithdrawal
ProcessBankingOperation
GetRequestStatus
```

Cada classe deve representar uma intenção clara do sistema.

Preferir casos de uso específicos a services genéricos e crescentes.

Evitar classes como:

```text
BankingService
AccountService
CommonService
```

quando começam a concentrar funcionalidades sem relação direta.

A camada `application` pode:

* coordenar domínio;
* abrir transações;
* consultar repositories;
* publicar mensagens através de abstrações;
* definir o fluxo de um caso de uso.

Não deve concentrar regras que pertencem naturalmente ao domínio.

---

# 5. `domain`

Contém as regras centrais do negócio.

Pode conter:

* entidades;
* value objects;
* enums;
* invariantes;
* exceções de domínio;
* interfaces de repository quando fizer sentido.

Regras importantes devem permanecer próximas dos objetos que protegem.

Exemplos:

* uma conta não deve permitir saque superior ao saldo;
* uma conta inativa não deve processar determinadas operações;
* uma operação concluída não deve ser processada novamente;
* valores monetários inválidos devem ser recusados.

Evitar entidades puramente anêmicas compostas apenas por getters e setters quando existirem regras próprias daquele objeto.

---

# 6. `infrastructure`

Contém detalhes externos à regra de negócio.

Exemplos:

```text
infrastructure/
├── persistence/
└── messaging/
```

Pode conter:

* Spring Data;
* implementações de repository;
* Kafka producers;
* Kafka consumers;
* adapters externos;
* detalhes específicos de PostgreSQL.

Infraestrutura não deve definir regras de negócio.

---

# 7. Persistência

PostgreSQL é a fonte de verdade dos dados da aplicação.

Flyway deve ser responsável pelo versionamento do schema.

Não utilizar geração automática do Hibernate como mecanismo de evolução do banco em produção.

Preferir:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Repositories devem permanecer específicos ao domínio ou caso de uso.

Evitar repositories genéricos ou abstrações criadas apenas para esconder Spring Data.

---

# 8. JPA

JPA pode ser utilizado diretamente nas entidades de domínio quando isso mantiver o código simples.

Não é obrigatório criar:

```text
DomainEntity
JpaEntity
EntityMapper
PersistenceModel
```

para cada entidade.

Separar modelos somente quando houver benefício concreto.

Evitar uso indiscriminado de:

* relacionamentos bidirecionais;
* `CascadeType.ALL`;
* `FetchType.EAGER`;
* `@Data`;
* setters públicos;
* carregamento implícito difícil de controlar.

Queries importantes devem ter comportamento previsível.

---

# 9. Transações

Transações devem representar unidades reais de consistência.

No processamento de movimentações bancárias, devem ocorrer atomicamente:

* validação;
* alteração do saldo;
* criação da movimentação;
* atualização do status da requisição.

Não distribuir uma operação atômica entre múltiplas transações sem necessidade.

O limite transacional deve preferencialmente estar no caso de uso responsável pelo processamento.

---

# 10. Concorrência

Saldo bancário é dado concorrente.

A aplicação não deve depender apenas da ordenação fornecida pelo Kafka para garantir consistência.

O banco também deve proteger as invariantes necessárias.

Utilizar estratégia explícita de concorrência quando houver atualização simultânea de saldo, como:

* pessimistic locking;
* optimistic locking;
* operações SQL atômicas.

A escolha deve ser consciente e testada.

---

# 11. Kafka

Kafka pertence à infraestrutura.

Consumers devem ser pequenos.

Um `@KafkaListener` deve:

1. receber o evento;
2. validar o contrato técnico quando necessário;
3. delegar para um caso de uso.

Não colocar regras financeiras diretamente no consumer.

Producers devem publicar contratos próprios de evento.

Nunca publicar entidades JPA diretamente.

---

# 12. Eventos

Eventos devem representar contratos explícitos.

Devem conter apenas os dados necessários para o consumidor realizar a operação.

Eventos devem ser independentes da estrutura interna das entidades persistidas.

Alterações em entidades não devem alterar silenciosamente contratos Kafka existentes.

Quando possível, eventos devem ser imutáveis.

---

# 13. Ordenação Kafka

Operações da mesma conta devem utilizar `accountId` como chave da mensagem.

Isso permite que operações da mesma conta permaneçam na mesma partição e preservem sua ordem relativa.

A ordenação Kafka é uma proteção adicional, não substitui garantias de consistência no banco.

---

# 14. Idempotência

Consumers Kafka devem ser idempotentes.

Uma mensagem repetida não pode:

* duplicar depósito;
* duplicar saque;
* duplicar movimentação;
* alterar novamente uma solicitação já finalizada.

A idempotência deve ser protegida tanto pela aplicação quanto, quando apropriado, por constraints no banco.

Não assumir entrega exatamente uma vez.

---

# 15. Publicação confiável de eventos

Persistência no PostgreSQL e publicação no Kafka são operações independentes.

Evitar arquiteturas que assumam que:

```text
save()
kafka.send()
```

formam automaticamente uma única transação confiável.

Quando a garantia de publicação for relevante, utilizar padrão Transactional Outbox ou mecanismo equivalente.

---

# 16. DTOs

DTOs pertencem à fronteira da aplicação.

Preferir `record` para estruturas imutáveis simples.

DTOs HTTP não devem ser reutilizados como:

* entidades JPA;
* eventos Kafka;
* modelos internos de domínio.

Cada contrato deve possuir responsabilidade clara.

---

# 17. Validação

Existem dois níveis de validação.

### Validação estrutural

Responsabilidade da API.

Exemplos:

* campo obrigatório;
* formato;
* valor positivo;
* tamanho;
* sintaxe.

Pode utilizar Bean Validation.

### Validação de negócio

Responsabilidade do domínio ou do caso de uso.

Exemplos:

* saldo insuficiente;
* conta inativa;
* acesso não autorizado à conta;
* operação já concluída.

Não confiar apenas em validações da camada HTTP.

---

# 18. Segurança

Autenticação e autorização devem permanecer separadas das regras financeiras.

A aplicação deve distinguir corretamente:

```text
401 Unauthorized
403 Forbidden
```

A autorização para acessar uma conta deve ser validada no servidor.

Nunca confiar em `accountId`, `userId` ou informações de propriedade enviadas pelo cliente.

---

# 19. `shared`

O pacote `shared` deve permanecer pequeno.

Pode conter elementos genuinamente compartilhados, como:

```text
shared/
└── exception/
```

Evitar transformar `shared`, `common` ou `utils` em depósitos de código sem domínio definido.

Se uma classe pertence claramente a uma feature, ela deve permanecer nessa feature.

---

# 20. Configuração

Configurações globais do framework podem permanecer em:

```text
config/
```

Exemplos:

* Kafka;
* Spring Security;
* Jackson;
* observabilidade.

Não colocar regras de negócio em classes `@Configuration`.

---

# 21. Dependências entre features

Evitar dependências circulares.

Preferir:

```text
api
 ↓
application
 ↓
domain
```

Infraestrutura implementa detalhes necessários pelo restante da aplicação.

Features devem acessar outras features através de contratos claros, não através de detalhes internos.

---

# 22. Testes

A estrutura dos testes deve acompanhar a estrutura das features.

Priorizar três níveis.

### Testes de domínio

Para regras puras e rápidas.

### Testes de caso de uso

Para coordenação e comportamento da aplicação.

### Testes de integração

Para verificar:

* PostgreSQL;
* JPA;
* migrations;
* Kafka;
* transações;
* concorrência;
* idempotência;
* autenticação e autorização.

Testes de integração devem utilizar PostgreSQL e Kafka reais através de Testcontainers.

Não utilizar H2 como substituto do PostgreSQL nos testes de integração.

---

# 23. Docker Compose

Docker Compose deve ser utilizado para facilitar o ambiente de desenvolvimento local.

Testes automatizados devem utilizar Testcontainers.

Separação esperada:

```text
Docker Compose
    → desenvolvimento

Testcontainers
    → testes automatizados
```

---

# 24. Exceções

Exceções devem representar situações específicas.

Evitar:

```text
RuntimeException
BusinessException
GenericException
```

como solução universal.

Preferir exceções semanticamente claras quando houver comportamento específico associado.

---

# 25. Nomenclatura

Nomes devem representar comportamento e intenção.

Preferir:

```text
RequestWithdrawal
ProcessBankingOperation
GetAccount
BankingOperationPublisher
BankingOperationConsumer
```

Evitar nomes vagos:

```text
Manager
Helper
Utils
Processor
Handler
ServiceImpl
Common
```

quando não explicarem claramente a responsabilidade.

---

# 26. Abstrações

Não criar abstração antecipadamente.

Uma interface deve existir quando houver motivo arquitetural concreto, como:

* isolamento de infraestrutura;
* múltiplas implementações;
* necessidade real de substituição;
* melhoria relevante de testabilidade.

Não criar interfaces automaticamente para toda classe `Service`.

---

# 27. Complexidade

A arquitetura deve crescer conforme a necessidade do sistema.

Antes de adicionar:

* nova camada;
* mapper;
* interface;
* factory;
* adapter;
* DTO intermediário;
* framework adicional;

avaliar se existe problema concreto sendo resolvido.

Preferir código explícito e simples a abstrações especulativas.

---

# 28. Regra geral

Cada funcionalidade deve ser compreensível navegando principalmente dentro de sua própria feature.

Ao implementar uma alteração, o desenvolvedor não deveria precisar percorrer diversas pastas globais para localizar:

```text
controller
DTO
service
repository
entity
consumer
producer
```

relacionados à mesma funcionalidade.

A arquitetura deve aproximar código que muda pelo mesmo motivo.

---

# Estrutura de referência

```text
com.example.banking
│
├── account/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│
├── operation/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│       ├── persistence/
│       └── messaging/
│
├── requeststatus/
│   ├── api/
│   └── application/
│
├── security/
├── config/
└── shared/
```

Esta estrutura é uma referência, não uma obrigação rígida.

A regra principal é:

> Organizar por funcionalidade, manter responsabilidades explícitas e adicionar complexidade somente quando ela resolver um problema real.

