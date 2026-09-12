# Marketing Analytics Platform

Plataforma SaaS de análisis y gestión de marketing digital pensada para pequeñas empresas, emprendimientos y agencias. Permite administrar campañas multi-plataforma, registrar y analizar métricas de rendimiento (CTR, CPC, CPM, ROAS, CPA, tasa de conversión), gestionar leads con scoring automático, planificar contenido, generar copy con IA, recibir recomendaciones de optimización basadas en datos reales y producir reportes exportables.

## Índice

- [Funcionalidades](#funcionalidades)
- [Arquitectura](#arquitectura)
- [Stack tecnológico](#stack-tecnológico)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Requisitos previos](#requisitos-previos)
- [Puesta en marcha con Docker](#puesta-en-marcha-con-docker-recomendado)
- [Puesta en marcha manual](#puesta-en-marcha-manual)
- [Variables de entorno](#variables-de-entorno)
- [Credenciales de demo](#credenciales-de-demo)
- [Endpoints principales](#endpoints-principales)
- [Testing](#testing)
- [Integración de IA](#integración-de-ia)
- [Decisiones de arquitectura](#decisiones-de-arquitectura)

## Funcionalidades

- **Autenticación y usuarios**: registro, login, logout (revocación de token), roles `ADMIN`/`USER`, edición de perfil y cambio de contraseña.
- **Campañas**: CRUD completo, transiciones de estado controladas (`DRAFT → ACTIVE → PAUSED/COMPLETED/CANCELLED`), búsqueda, filtros por plataforma/objetivo/estado, paginación y orden.
- **Métricas**: carga diaria manual, **importación por CSV** (con plantilla descargable) o **sincronización automática** desde una cuenta real conectada (ver Integraciones). Cálculo automático de CTR, CPC, CPM, tasa de conversión, CPA y ROAS, con protección contra división por cero.
- **Integraciones con plataformas reales**: conectá Meta Ads, Google Ads o TikTok Ads vía OAuth (o pegando un token si ya tenés uno), con credenciales cifradas en la base de datos, y sincronizá métricas reales de las campañas que vincules con su ID externo.
- **Dashboard**: KPIs globales, gráficos **personalizables** (elegí la métrica — inversión, ingresos, impresiones, clics, conversiones, CTR, CPC o ROAS — y el tipo de gráfico: línea, barras o área, con la preferencia guardada por widget), rendimiento por plataforma, mejores/peores campañas, distribución de presupuesto, últimos leads y recomendaciones recientes — todo filtrable por período, plataforma, campaña y objetivo.
- **Leads (CRM básico)**: alta/edición/baja, cambio de estado, registro de contactos y **lead scoring determinístico** (según origen, contactos, estado del pipeline y comportamiento en campaña), clasificado en Caliente/Tibio/Frío.
- **Content Planner**: calendario de contenido por plataforma, estado y objetivo, con copy, hashtags y CTA.
- **Generador de contenido con IA**: genera título, copy, CTA y hashtags a partir de producto, audiencia, plataforma, objetivo y tono. Arquitectura desacoplada (`AiContentProvider`) lista para conectar un proveedor real (ver [Integración de IA](#integración-de-ia)).
- **Campaign Optimizer**: motor de recomendaciones que analiza el ROAS real de campañas y plataformas y sugiere subir/bajar presupuesto, pausar campañas o renovar creatividades — siempre con la métrica y el motivo que la originan.
- **Reportes**: generación de reportes por período/campañas/plataformas con resumen de KPIs, desglose por campaña y recomendaciones asociadas; exportables a CSV.
- **Objetivos (Goals)**: metas de conversiones/ingresos/ROAS/leads/CTR con seguimiento de progreso.
- **Ads y Presupuestos**: creatividades y asignaciones de presupuesto por período dentro de cada campaña.
- **Auditoría**: registro de actividad reciente (quién hizo qué) visible en el dashboard.

## Arquitectura

### Backend (arquitectura en capas)

```
controller  →  service  →  repository  →  entity (JPA)
                 ↑
               dto / mapper (evitan exponer entidades directamente)
```

- Los controllers son delgados: validan la entrada (Bean Validation) y delegan en los services.
- La lógica de negocio (transiciones de estado, cálculo de métricas, scoring, motor de recomendaciones) vive en `service`.
- `GlobalExceptionHandler` centraliza el manejo de errores y devuelve respuestas HTTP consistentes.
- JWT stateless con filtro propio (`JwtAuthFilter`) + lista de revocación en memoria para logout.
- Flyway gestiona el esquema (`ddl-auto=validate` en todo entorno, nunca `create`).

### Frontend (por capas también)

```
api/        → llamadas HTTP tipadas (axios)
context/    → estado global (auth, toasts)
hooks/      → lógica reutilizable (debounce, plataformas)
components/ → ui (design system) · layout · charts · domain (formularios de negocio)
pages/      → una página por ruta, componen lo anterior
```

## Stack tecnológico

**Backend**: Java 21 · Spring Boot 3 · Spring Web · Spring Data JPA (Hibernate) · Spring Security + JWT (jjwt) · Bean Validation · PostgreSQL · Flyway · springdoc-openapi (Swagger) · Lombok · JUnit 5 / Mockito / AssertJ.

**Frontend**: React 19 · TypeScript · Vite · Tailwind CSS v4 · React Router · React Hook Form + Zod · Axios · Recharts · Lucide Icons.

**Infraestructura**: Docker, Docker Compose, PostgreSQL en contenedor.

## Estructura del proyecto

```
marketing-analytics-platform/
├── backend/
│   ├── src/main/java/com/marketinganalytics/platform/
│   │   ├── config/        (seguridad, OpenAPI, seed de datos demo)
│   │   ├── controller/
│   │   ├── service/ (+ impl/ + ai/)
│   │   ├── repository/ (+ spec/ para filtros dinámicos)
│   │   ├── entity/ (+ enums/)
│   │   ├── dto/
│   │   ├── mapper/
│   │   ├── exception/
│   │   └── security/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/   (Flyway V1__init_schema.sql, V2__seed_platforms.sql)
│   ├── src/test/java/...
│   └── Dockerfile
├── frontend/
│   ├── src/
│   │   ├── api/ · context/ · hooks/ · lib/ · types/ · utils/
│   │   ├── components/ (ui/ · layout/ · charts/ · domain/)
│   │   └── pages/ (auth/ · dashboard/ · campaigns/ · leads/ · content/ · analytics/ · settings/)
│   └── Dockerfile
├── docker-compose.yml
└── .env.example
```

## Requisitos previos

- **Con Docker (recomendado)**: Docker Desktop 24+ con Docker Compose.
- **Manual**: Java 21 (JDK), Node.js 20+, PostgreSQL 16 (o Docker solo para la base de datos).

## Puesta en marcha con Docker (recomendado)

```bash
git clone <url-del-repositorio>
cd marketing-analytics-platform
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:8081
- Backend / Swagger: http://localhost:8080/swagger-ui.html
- PostgreSQL: `localhost:5432` (credenciales según `.env`)

Al iniciar por primera vez, el backend corre las migraciones de Flyway y siembra datos de demo (usuario, campañas, métricas, leads, contenido y recomendaciones) automáticamente. Para desactivar el seed, definí `SEED_DEMO_DATA=false` en `.env`.

## Puesta en marcha manual

### 1. Base de datos

```bash
docker run -d --name map-postgres \
  -e POSTGRES_DB=marketing_analytics \
  -e POSTGRES_USER=marketing_user \
  -e POSTGRES_PASSWORD=marketing_pass \
  -p 5432:5432 postgres:16-alpine
```

(o instalá PostgreSQL 16 localmente con esas mismas credenciales/base).

### 2. Backend

```bash
cd backend
cp .env.example .env   # y exportá esas variables, o configurá tu IDE con ellas
./mvnw spring-boot:run
```

El backend queda disponible en `http://localhost:8080`. Documentación interactiva en `http://localhost:8080/swagger-ui.html`.

### 3. Frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

El frontend queda disponible en `http://localhost:5173`.

## Variables de entorno

### Backend

| Variable | Descripción | Default |
|---|---|---|
| `DB_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/marketing_analytics` |
| `DB_USERNAME` / `DB_PASSWORD` | Credenciales de base de datos | `marketing_user` / `marketing_pass` |
| `SERVER_PORT` | Puerto HTTP del backend | `8080` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (separados por coma) | `http://localhost:5173` |
| `JWT_SECRET` | Clave HMAC para firmar JWT (mínimo 32 caracteres) | *(clave de desarrollo, cambiar en producción)* |
| `JWT_EXPIRATION_MINUTES` | Minutos de validez del token | `1440` |
| `SEED_DEMO_DATA` | Si sembrar datos de demo al iniciar | `true` |
| `DEMO_USER_EMAIL` / `DEMO_USER_PASSWORD` | Credenciales del usuario demo generado | ver abajo |
| `APP_ENCRYPTION_KEY` | Passphrase para cifrar (AES-256-GCM) los tokens de integraciones antes de guardarlos | *(clave de desarrollo, cambiar en producción)* |
| `FRONTEND_URL` | URL del frontend, usada para redirigir tras el callback de OAuth | `http://localhost:5173` |
| `META_CLIENT_ID` / `META_CLIENT_SECRET` / `META_REDIRECT_URI` | Credenciales de la app de Meta for Developers (producto "Marketing API") | vacío (integración deshabilitada hasta configurarlas) |
| `GOOGLE_ADS_CLIENT_ID` / `GOOGLE_ADS_CLIENT_SECRET` / `GOOGLE_ADS_DEVELOPER_TOKEN` / `GOOGLE_ADS_LOGIN_CUSTOMER_ID` / `GOOGLE_ADS_REDIRECT_URI` | Credenciales OAuth + developer token de Google Ads API | vacío |
| `TIKTOK_APP_ID` / `TIKTOK_APP_SECRET` / `TIKTOK_REDIRECT_URI` | Credenciales de la app de TikTok for Business | vacío |

Ninguna clave o secreto está hardcodeado: todos se leen vía variables de entorno con `${VAR:default}` en `application.yml`.

### Frontend

| Variable | Descripción | Default |
|---|---|---|
| `VITE_API_URL` | URL base de la API | `http://localhost:8080/api` |

## Credenciales de demo

```
Email:    demo@marketinganalytics.com
Password: Demo1234!
```

El usuario demo (rol `ADMIN`) se crea junto con 7 campañas (una por plataforma, en distintos estados), 14 días de métricas por campaña, leads, contenido planificado, ideas generadas por IA, objetivos y recomendaciones ya calculadas a partir de esos datos.

## Endpoints principales

Documentación completa e interactiva en Swagger (`/swagger-ui.html`). Resumen:

```
POST   /api/auth/register            POST   /api/auth/login          POST /api/auth/logout
GET    /api/users/me                 PUT    /api/users/me            PUT  /api/users/me/password

GET    /api/campaigns                POST   /api/campaigns           GET/PUT/DELETE /api/campaigns/{id}
POST   /api/campaigns/{id}/activate  /pause  /complete  /cancel
GET    /api/campaigns/{id}/metrics   POST   /api/campaigns/{id}/metrics
POST   /api/campaigns/{id}/metrics/import   (CSV)   GET /api/metrics/import/template
GET    /api/campaigns/{id}/ads       POST   /api/campaigns/{id}/ads
GET    /api/campaigns/{id}/budgets   POST   /api/campaigns/{id}/budgets

GET    /api/platforms
GET    /api/dashboard

GET    /api/integrations                          GET  /api/integrations/{platformId}/authorize-url
POST   /api/integrations/{platformId}/connect-manual   POST /api/integrations/{platformId}/sync
DELETE /api/integrations/{platformId}             GET  /api/integrations/callback (OAuth redirect target)

GET    /api/leads                    POST   /api/leads               GET/PUT/DELETE /api/leads/{id}
PATCH  /api/leads/{id}/status        POST   /api/leads/{id}/contact

GET    /api/content                  POST   /api/content             GET/PUT/DELETE /api/content/{id}
POST   /api/content-ideas/generate   GET    /api/content-ideas       PUT/DELETE /api/content-ideas/{id}

POST   /api/recommendations/generate GET    /api/recommendations     PATCH /api/recommendations/{id}/status

POST   /api/reports                  GET    /api/reports             GET /api/reports/{id}
GET    /api/reports/{id}/export      (CSV)

GET    /api/goals                    POST   /api/goals               DELETE /api/goals/{id}
GET    /api/activity
```

## Testing

```bash
cd backend
./mvnw test
```

Incluye tests unitarios (cálculo de métricas: CTR/CPC/CPM/CPA/conversion rate/ROAS con casos límite de división por cero; lead scoring; motor del optimizer; transiciones de estado de campaña; parseo de CSV; cifrado de credenciales; construcción y parseo de la respuesta real de la API de Meta Ads contra un servidor mockeado) y tests de integración de extremo a extremo sobre una base H2 en modo PostgreSQL con las migraciones de Flyway reales (registro/login/logout, autorización, CRUD de campañas, cálculo de ROAS vía API, dashboard, generación de recomendaciones, leads, generación de contenido con IA, reportes con exportación CSV, y el ciclo de vida de una conexión de integración incluyendo que el token quede cifrado en la base).

## Integración de IA

El generador de contenido usa la interfaz `AiContentProvider` (`backend/.../service/ai/AiContentProvider.java`). La implementación incluida (`MockAiContentProvider`) genera copy determinístico por plantillas según tono/objetivo, sin depender de una API externa ni de claves — así el producto funciona completo hoy mismo.

Para conectar un proveedor real (OpenAI, Anthropic, etc.):

1. Creá una clase que implemente `AiContentProvider`.
2. Anotala con `@Service` y `@Primary` (para que reemplace al mock).
3. Leé la API key desde una variable de entorno (nunca hardcodeada).

No hay que tocar controllers, DTOs ni el resto de `ContentIdeaService`: la abstracción ya aísla ese cambio.

## Integraciones con plataformas reales (Meta / Google / TikTok Ads)

La arquitectura de conexión está completa y funcional (`Configuración › Integraciones` en el frontend): OAuth, almacenamiento cifrado de credenciales (AES-256-GCM, ver `CredentialCipher`), y clientes reales contra las APIs de Meta Marketing API, Google Ads API y TikTok Business API (`backend/.../service/integration/`). Sin credenciales propias todavía podés:

- **Operar ya mismo**: cargar métricas a mano o importando un CSV (`Métricas de campaña › Importar CSV`, con plantilla descargable) — no depende de ninguna integración externa.
- **Conectar con un token manual**: si ya tenés un access token vigente de alguna cuenta (por ejemplo generado desde el Graph API Explorer de Meta), pegalo en `Integraciones › Token manual` junto con el ID de esa cuenta — no requiere registrar una app OAuth.

Para habilitar la conexión completa por OAuth con una plataforma:

1. Registrá una app de desarrollador en esa plataforma (Meta for Developers, Google Cloud Console + acceso a Google Ads API, o TikTok for Business) y agregá `<META|GOOGLE_ADS|TIKTOK>_REDIRECT_URI` (`http://localhost:8080/api/integrations/callback` en desarrollo) como redirect URI autorizada.
2. Completá las variables de entorno correspondientes (ver [Variables de entorno](#variables-de-entorno)) y reiniciá el backend.
3. En `Configuración › Integraciones`, hacé clic en "Conectar con OAuth" para esa plataforma.
4. Editá cada campaña que quieras sincronizar y completá su **ID de campaña externa** (el ID que esa campaña tiene en el Ads Manager de la plataforma).
5. Desde `Integraciones`, usá "Sincronizar" eligiendo un rango de fechas — solo se actualizan las campañas con ID externo cargado.

Para sumar una plataforma nueva: implementá `PlatformSyncProvider`, registrala como `@Service`, y sumá su slug al mapa en `PlatformConnectionServiceImpl` — no hay que tocar el resto del sistema.

## Decisiones de arquitectura

- **`ddl-auto=validate` + Flyway**: el esquema vive en migraciones versionadas, no en autogeneración de Hibernate; los tests de integración corren las mismas migraciones sobre H2 (modo PostgreSQL) para detectar desajustes entidad-esquema antes de llegar a producción.
- **DTOs + mappers manuales**: con ~13 entidades no se justificó sumar MapStruct como dependencia; los mappers son simples y explícitos.
- **`DailyMetric` como tabla de agregación**: evita recalcular sumas sobre `campaign_metrics` en cada carga del dashboard a medida que crece el histórico.
- **Logout con lista de revocación en memoria**: la única forma de "invalidar" un JWT stateless es recordarlo hasta que expire; para un despliegue multi-instancia se reemplazaría por Redis sin cambiar la interfaz del servicio.
- **Recomendaciones deterministas**: el optimizer nunca genera sugerencias aleatorias — cada una cita la métrica y el valor real que la originó (ver `CampaignOptimizerEngine`, testeado de forma aislada).
- **Credenciales de integraciones cifradas, nunca en texto plano**: los tokens de OAuth se cifran (AES-256-GCM) antes de persistirse; un dump de la base nunca expone un token utilizable.
- **Gráficos del dashboard sobre datos crudos, no pre-agregados por métrica**: el backend expone totales diarios (`DailyBreakdownPoint`) en vez de series ya calculadas, para que el frontend pueda ofrecer cualquier métrica derivada sin otro round-trip al servidor.
