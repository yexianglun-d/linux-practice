# Linux 实战学堂

一个 Spring Boot + Vue 3 的 Linux 手敲实践学习平台原型。打开网站后自动进入全屏终端、创建默认学习会话，并通过终端指令完成学习、提示、判题、重置和进度更新。课程、实验、任务和判题规则全部由代码种子数据控制，不提供页面或接口形式的内容管理入口。

## 目录

- `backend/`：Spring Boot API、JPA 数据模型、WebSocket 终端、判题服务。
- `frontend/`：Vue 3 + TypeScript 终端优先学员端。
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

- `POST /api/lab-sessions/default`
- `GET /api/lab-sessions/{id}`
- `POST /api/lab-sessions/{id}/check`
- `POST /api/lab-sessions/{id}/reset`
- `WS /ws/lab-sessions/{id}/terminal`

## 当前边界

- 默认 `InMemoryVmSandboxClient` 只模拟 VM 分配、终端输出和本地 transcript 判题，便于完整跑通产品闭环。
- 生产接入真实 VM 时，替换 `VmSandboxClient`，在 VM 内执行判题脚本，并把会话/锁状态迁移到 Redis。
- 学习内容维护方式是修改后端种子代码并重新发布；当前版本不包含内容管理页面、内容维护接口、支付、商业化和移动端 App。

## 验证

```bash
cd backend && mvn test
cd frontend && npm run build
```
