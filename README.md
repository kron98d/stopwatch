# Stopwatch Service

Приложение - секундомер, записывает каждую секунду текущее время в PostgreSQL базу данных и предоставляет API для получения этих записей.

## 🚀 Возможности
- Асинхронная запись времени каждую секунду
- Буферизация и повторная запись при сбое БД
- REST API с пагинацией
- Swagger UI
- Тесты с использованием Testcontainers

## 🐳 Запуск через Docker Compose

docker-compose up -d


## ▶️ Запуск приложения

mvn spring-boot:run


## 🌐 Swagger UI
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 🧪 Тесты

mvn test


## 🗃️ Конфигурация application.yml

spring:
datasource:
url: jdbc:postgresql://localhost:5432/stopwatch
username: user
password: pass
jpa:
hibernate:
ddl-auto: update
show-sql: true
database-platform: org.hibernate.dialect.PostgreSQLDialect

logging:
level:
com.example.stopwatch: DEBUG

stopwatch:
producer:
interval-ms: 1000
shutdown:
timeout-seconds: 5


---

Разработано с применением DDD, многопоточности, отказоустойчивости и гибкой архитектуры.