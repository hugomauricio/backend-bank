# Backend Bank

Proyecto compuesto por dos microservicios Spring Boot y una infraestructura local con Docker Compose para PostgreSQL y RabbitMQ.

## Estructura del proyecto

```text
backend-bank/
├── .gitignore
├── README.md
├── docker-compose.yml
├── db/
│   └── BaseDatos.sql
├── account-service/
│   ├── Dockerfile
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew
│   ├── gradlew.bat
│   ├── gradle/
│   ├── src/
│   └── build/
└── customer-service/
    ├── Dockerfile
    ├── build.gradle
    ├── settings.gradle
    ├── gradlew
    ├── gradlew.bat
    ├── gradle/
    ├── src/
    └── build/