.DEFAULT_GOAL := help

.PHONY: help infra-up infra-down infra-logs infra-status run test build verify clean

help: # Exibe esta ajuda.
	@awk 'BEGIN {FS = ":.*#"} /^[a-zA-Z_-]+:.*#/ {printf "  \033[36m%-16s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

infra-up: # Inicia PostgreSQL e Kafka e espera pelos healthchecks.
	docker compose up -d --wait

infra-down: # Para a infraestrutura local.
	docker compose down

infra-logs: # Acompanha os logs de PostgreSQL e Kafka.
	docker compose logs -f postgres kafka

infra-status: # Exibe o estado dos serviços locais.
	docker compose ps

run: # Inicia a aplicação; Spring Compose conecta a infra e Flyway aplica migrations.
	./gradlew bootRun

test: # Executa a suíte de testes, incluindo os testes de integração.
	./gradlew test

build: # Compila, testa e empacota a aplicação.
	./gradlew build

verify: # Compila e executa as verificações sem gerar o pacote final.
	./gradlew check

clean: # Remove os artefatos gerados pelo Gradle.
	./gradlew clean
