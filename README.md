# Spring Bank Challenge

## Introdução
Esse projeto faz parte de um desafio de uma aplicação de microsservicos para criar um sistema de banco. 
Algumas das regras são sua construção em java, uso de microsserviços, utilização de processamento assíncrono, 
tratamento de erros, uso de filas e boas práticas de código.

## Tecnologias

- Java 11 com springboot versão 2.3.3
- PostgreSQL
- RabbitMQ como mensageria
- Feign para integração REST
- Flyway para controle de migrations
- Logstash para controle de log
- Testes unitários e integrados spring-test, mockito e junit 5
- Docker e Docker-compose

## Setup

### Instalar dependências

```bash
$ mvn clean install
```

### Subir o docker-compose com as dependências requeridas

```bash
$ docker-compose up -d
```

### Rodar testes

```bash
$ mvn test
```

### Rodar aplicação (referência da pasta raíz)

```bash
$ java -jar customer/*.jar 
$ java -jar account/*.jar
$ java -jar service/*.jar
```

## Estrutura de pastas e Arquitetura

### Nível principal:
- common   - módulo com utilitários, configurações e classes de DTO comuns à todos os módulos que o importam
- customer - microsservico responsável pelo domínio do cliente 
- account  - microsservico responsável pelo domínio da conta
- service  - microsservico responsável pelo domínio de serviços (cartão e cheque especial)

### Estrutura interna:
`config` - arquivos de configurações gerais da aplicação

`controller` - camada responsável pela integração rest de entrada

`external` - camada responsável por toda comunicação chamada externamente (integração de apis rest, publicação em filas , comunicação com banco de dados etc)

`listener` - camada responsável pela integração de mensageria de entrada

`model` - Entidades, objetos de valor, enums e classes auxiliares de criações

`service` - Camada responsável por realizar a regra de negócio


### Construção das tabelas
- Customer MS

![Customer MS](https://github.com/rodrigorpo/spring-bank/blob/master/images/customer_ms.png)

- Account MS

![Account MS](https://github.com/rodrigorpo/spring-bank/blob/master/images/account_ms.png)

- Service MS

![Service MS](https://github.com/rodrigorpo/spring-bank/blob/master/images/service_ms.png)


### Desenho da arquitetura

![Arquitetura](https://github.com/rodrigorpo/spring-bank/blob/master/images/architecture-example.jpg)


## Considerações finais
- A collection está em anexo - Insomnia
- Rode os três módulos simultâneamente para conseguir testar a aplicação
- Os microsserviços customer, account e service estão rodando, respectivamente, nas portas 8080, 8081 e 8082.