# Contenedores Backend

Backend del sistema de gestión de contenedores marítimos en depósito.
Java 21 + Spring Boot 3.5 + PostgreSQL 16 + Flyway + Spring Security (JWT).

## Requisitos

- Java 21+
- Maven 3.9+ (`brew install maven`)
- Docker Desktop (para Postgres local)

## Cómo correr

```bash
# 1. Levantar Postgres (puerto 5433 en el host, para no chocar con un Postgres local)
docker compose up -d

# 2. Arrancar la app con el perfil local
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

- API: http://localhost:8080/api
- Swagger: http://localhost:8080/swagger-ui.html

En IntelliJ: Run → Edit Configurations → Active profiles: `local` (o VM options `-Dspring.profiles.active=local`).

## Usuario inicial

Se crea con la migración `V2__seed_usuario_inicial.sql`:

- Email: `admin@empresa.cl`
- Contraseña: `Admin123!` — **solo desarrollo local**, cambiar tras el primer uso real.
- En el repo solo vive el hash BCrypt, nunca la contraseña.

Para generar el hash de una contraseña nueva:

```bash
PASSWORD_SEED='NuevaClave' mvn test -Dtest=GeneradorHashBcryptTest
# copiar HASH_BCRYPT=... a V2__seed_usuario_inicial.sql
```

## Tests

```bash
mvn test
```

## Variables sensibles (producción)

Definidas en `application-prod.yml`, nunca hardcodeadas:

| Variable | Descripción |
|---|---|
| `DB_URL` | JDBC URL de Postgres |
| `DB_USERNAME` / `DB_PASSWORD` | credenciales de BD |
| `JWT_SECRET` | clave HMAC-SHA256 (mín. 32 bytes) |

## Migraciones Flyway

```
V1__init.sql                  # tablas usuario, ubicacion, contenedor, movimiento + índices
V2__seed_usuario_inicial.sql  # usuario inicial con hash BCrypt
V3__seed_ubicaciones_demo.sql # ubicaciones demo para desarrollo
```

## Decisiones de diseño relevantes

- Cada operación que cambia el estado de un contenedor (ingreso, reubicación,
  despacho) registra automáticamente su `Movimiento` con el usuario autenticado
  como `registradoPor`, dentro del servicio y transaccionalmente.
- Validación ISO 6346 completa: regex + dígito verificador (`ValidadorContenedor`),
  expuesta como anotación Bean Validation `@NumeroContenedorValido`.
- El campo `rol` ya existe en el modelo, pero en este MVP no hay autorización
  diferenciada por rol; se agregará sin migrar esquema cuando exista el caso de uso.
- Errores centralizados vía `@ControllerAdvice` con formato `{codigo, mensaje, campo}`.
