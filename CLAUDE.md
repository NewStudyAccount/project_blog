# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A blog/CMS system with a Spring Boot 3.5 backend (Java 21, Maven) and Vue 3 frontend (TypeScript, Vite). Backend runs on port 38080; frontend dev server proxies API calls to `http://localhost:38080/project`.

## Build & Run Commands

### Backend (from `myproject/`)
```bash
mvn clean package                          # Build all modules
mvn spring-boot:run -pl myproject-server   # Run backend (port 38080)
mvn clean package -pl myproject-server     # Build runnable JAR only
```

### Frontend (from `myproject-frontend/`)
```bash
npm install        # Install dependencies
npm run dev        # Dev server (Vite)
npm run build      # Type-check (vue-tsc) + production build
npm run preview    # Preview production build
```

## Backend Architecture (myproject/)

Multi-module Maven project under `com.example` package. Entry point: `myproject-server/ServerApplication.java`.

**Module dependency chain:** `server` depends on `blog`, `system`, `generator`, `oss`, `framework`, `common`.

- **myproject-common** — Shared utilities, `Response<T>` wrapper (code/msg/data), `PageQuery`, exception classes (`BizException`), Redis helper, Snowflake ID generator, `SecurityUtils`, AOP logging (`@ApiOperationLog`)
- **myproject-framework** — Spring Security config, JWT filter (`JwtAuthenticationFilter`), `TokenService`, CORS config, MyBatis-Plus config, global exception handler, thread pool config
- **myproject-system** — User/Role/Menu CRUD. `AuthController` handles login/logout. Menu tree drives frontend dynamic routing. RBAC: `SysUser` ↔ `SysUserRole` ↔ `SysRole` ↔ `SysRoleMenu` ↔ `SysMenu`
- **myproject-blog** — Article/Tag/Category/ArticleContent CRUD. Markdown rendering via Flexmark
- **myproject-oss** — S3-compatible object storage (AWS SDK). Factory pattern for OSS clients
- **myproject-generator** — Code generation from database tables using Apache Velocity templates

**Conventions:**
- Layered architecture per module: `controller` → `service` → `mapper` (MyBatis-Plus). POJOs in `domain/pojo`, request DTOs in `domain/req`, response DTOs in `domain/vo`
- Mappers are auto-discovered via `@MapperScan(basePackages = {"com.example.**.mapper"})`
- Logical delete field: `is_deleted` (1 = deleted)
- Snowflake IDs for primary keys
- `Response.code` is a String — `"200"` for success, `"8888"` for generic failure

**Security:** JWT Bearer tokens. Login at `POST /project/auth/login`. Token sent via `Authorization: Bearer <token>` header. Security whitelist defined in `SecurityConfig.WHITE_LIST`.

**API Docs:** Knife4j at `http://localhost:38080/doc.html` (dev profile).

## Frontend Architecture (myproject-frontend/)

Vue 3 + TypeScript + Vite. Component library: Element Plus. Icons: `unplugin-icons` with `@iconify-json/ep`.

**Key directories:**
- `src/api/` — Axios API modules (one per domain: `auth.ts`, `blog/`, `oss/`, `sys*.ts`)
- `src/stores/` — Pinia stores: `user` (auth state), `permission` (dynamic routes)
- `src/router/` — Route definitions + navigation guard (`guard.ts`)
- `src/components/layout/` — `MainLayout`, `SideMenu`, `HeaderNav`
- `src/views/` — Page components organized by domain (`blog/`, `system/`, `oss/`, `generator/`)
- `src/utils/http.ts` — Axios instance with interceptors (token injection, duplicate submission prevention, file upload handling)

**Dynamic routing:** Frontend fetches menu tree from `GET /project/sysMenu/getMenuTree` after login. `permission` store converts menu items to Vue Router routes, matching `menu.component` to `src/views/**/*.vue` via `import.meta.glob`. Menu types: `M` (directory with children) and leaf pages.

**Auth flow:** Login → store token in localStorage → fetch user info → load menu tree → generate dynamic routes → add to router.

**API base URL:** Configured in `.env.development` (`http://localhost:38080/project`) and `.env.production` (`/project`). All API modules use the shared `http` Axios instance from `src/utils/http.ts`.

## Database

MySQL 8.0 with MyBatis-Plus. Connection configured in `myproject-server/src/main/resources/application-dev.yml`. SQL logging via P6Spy. Profiles: `dev`, `test`.

## OpenSpec

The `openspec/` directory contains design specs and change tracking for features (archived in `openspec/changes/archive/`). Active specs are in `openspec/specs/`. Config in `openspec/config.yaml` documents the full tech stack.
