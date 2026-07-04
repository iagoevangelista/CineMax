# CineMax — Microservicios

## Requisitos

| Herramienta | Versión | Instalación |
|---|---|---|
| Docker Desktop | 24+ | https://www.docker.com/products/docker-desktop/ |
| Java | 17+ | `winget install EclipseAdoptium.Temurin.17.JDK` |
| Maven | 3.9+ | `winget install Apache.Maven` |

---

## Fase 0 — Bases de datos (Docker)

```bash
cd microservices
docker compose up -d
```

| Servicio | Puerto | Usuario | Password | Bases de datos |
|---|---|---|---|---|
| PostgreSQL | 5432 | `cine` | `cine123` | `auth_db` |
| MySQL | 3307 | `root` | `cine123` | `sucursales_db`, `dulceria_db` |
| MongoDB | 27017 | `cine` | `cine123` | `cartelera_db` |
| SQL Server | 1433 | `sa` | `123456` | `sales_db`, `usuarios_db` |

Para detener: `docker compose down`  
Para detener + borrar datos: `docker compose down -v`

---

## Fase 1 — Infraestructura

Orden de arranque (obligatorio):

```
1. service-discovery  → :8761
2. config-server      → :8888
3. api-gateway        → :8080
```

### Compilar todo
```bash
mvn compile -pl common-library,service-discovery,config-server,api-gateway
```

### service-discovery
```bash
cd service-discovery
mvn spring-boot:run
```
Dashboard: http://localhost:8761

### config-server
```bash
cd config-server
mvn spring-boot:run
```
Lee configs de `config-repo/`.  
Verificar: http://localhost:8888/auth-service/default

### api-gateway
```bash
cd api-gateway
mvn spring-boot:run
```
Punto de entrada único para el frontend: http://localhost:8080

---

## Estructura actual

```
microservices/
├── docker-compose.yml       # 4 bases de datos
├── pom.xml                  # Padre (solo módulos de Fase 1)
├── config-repo/             # Configs externas (YML)
├── init-scripts/            # Seed data para cada BD
│   ├── postgres/
│   ├── mysql/
│   ├── mongodb/
│   └── sqlserver/
├── common-library/          # [F1] CloudinaryService + ApiResponse
├── service-discovery/       # [F1] Eureka Server :8761
├── config-server/           # [F1] Config Server :8888
└── api-gateway/             # [F1] Spring Cloud Gateway :8080
```

---

## Próximas fases (empezar cuando toque)

| Fase | Servicio | BD |
|---|---|---|
| 2 | auth-service | PostgreSQL |
| 3 | cartelera-service | MongoDB |
| 4 | sucursales-service | MySQL |
| 5 | dulceria-service | MySQL |
| 6 | facturacion-service | SQL Server |
| 7 | usuarios-service | SQL Server |
| 8 | notification-service | — |
