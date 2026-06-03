# Linux 实战学堂

一个 Spring Boot + Vue 3 的 Linux 手敲实践学习平台原型。当前实现覆盖课程目录、浏览器终端、实验会话、任务判题、进度记录和轻量后台，真实 VM/KubeVirt 调度被抽象在 `VmSandboxClient` 接口后，默认实现用于本地可运行演示。

## 目录

- `backend/`：Spring Boot API、JPA 数据模型、WebSocket 终端、判题服务。
- `frontend/`：Vue 3 + TypeScript 学员工作台和轻量后台。
- `docker-compose.yml`：可选 PostgreSQL / Redis 本地依赖。

## 本地运行

默认后端使用 H2 内存库，直接启动即可：

```bash
cd backend
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

访问：

- 前端：http://localhost:5173
- 后端：http://localhost:8080
- H2 Console：http://localhost:8080/h2-console

如需 PostgreSQL / Redis：

```bash
docker compose up -d postgres redis
cd backend
SPRING_PROFILES_ACTIVE=postgres mvn spring-boot:run
```

## 已实现接口

- `GET /api/catalog`
- `POST /api/lab-sessions`
- `GET /api/lab-sessions/{id}`
- `POST /api/lab-sessions/{id}/check`
- `POST /api/lab-sessions/{id}/reset`
- `WS /ws/lab-sessions/{id}/terminal`
- `POST /api/admin/courses`
- `POST /api/admin/labs`
- `POST /api/admin/tasks`
- `GET /api/admin/learning-progress`

## 当前边界

- 默认 `InMemoryVmSandboxClient` 只模拟 VM 分配、终端输出和本地 transcript 判题，便于完整跑通产品闭环。
- 生产接入真实 VM 时，替换 `VmSandboxClient`，在 VM 内执行判题脚本，并把会话/锁状态迁移到 Redis。
- 后台为轻量直接发布模式，没有复杂审核流、支付、商业化和移动端。

## 验证

```bash
cd backend && mvn test
cd frontend && npm run build
```
