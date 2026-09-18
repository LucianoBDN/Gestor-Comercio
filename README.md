# Gestor de Comercio


## Stack

Java 21 · JavaFX · Maven · PostgreSQL · Hibernate/JPA (Jakarta Persistence)

## Requisitos previos

- JDK 21
- Maven
- PostgreSQL corriendo localmente

## Configuración

### 1. Base de datos

Crear la base y el usuario de la aplicación:

```bash
sudo -u postgres psql -c "CREATE DATABASE gestor_comercio;"
sudo -u postgres psql -c "CREATE USER app_user WITH PASSWORD 'tu_clave';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE gestor_comercio TO app_user;"
sudo -u postgres psql -d gestor_comercio -c "GRANT ALL ON SCHEMA public TO app_user;"
```

Las tablas no se crean a mano: Hibernate las genera automáticamente al arrancar la aplicación (`hibernate.hbm2ddl.auto=update`).

### 2. Credenciales locales

Las credenciales de la base **no** están en el código versionado. Copiar la plantilla y completarla con los datos reales:

```bash
cp db.properties.example db.properties
```

Editar `db.properties` con el usuario y contraseña que creaste en el paso anterior:

```properties
db.user=app_user
db.password=tu_clave
```

Este archivo está en `.gitignore` — cada persona que corra el proyecto crea el suyo.

## Ejecutar la aplicación

```bash
mvn clean javafx:run
```

Si la conexión a la base falla (por ejemplo, PostgreSQL no está corriendo), la aplicación muestra un aviso de error en vez de colgarse.

## Correr los tests

```bash
mvn test
```

Requiere PostgreSQL corriendo con la base y las credenciales configuradas como se indica arriba.

## Estado del proyecto

- **Sprint 1** (en curso): estructura base del proyecto, conexión a PostgreSQL, navegación inicial entre pantallas.
