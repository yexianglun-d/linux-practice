# Linux 实战学堂

一个 Spring Boot + Vue 3 的 Linux 命令学习平台原型。打开网站后先进入命令练习台：一次练一个命令，输入后即时判断拼写、参数和目标匹配度，并记录正确率、连续正确和章节进度。终端保留为章节完成后的实战验证入口，不再作为新手首屏。

学习内容、实验、任务和判定规则全部由后端代码种子维护；改内容即改代码再发布，不提供页面或接口形式的内容管理入口。

## 目录

- `backend/`：Spring Boot API、JPA 数据模型、WebSocket 终端、判题服务。
- `frontend/`：Vue 3 + TypeScript 命令练习台和实战终端入口。
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

- `GET /api/command-exercises/default`
- `POST /api/command-exercises/{id}/attempt`
- `POST /api/lab-sessions/default`
- `GET /api/lab-sessions/{id}`
- `POST /api/lab-sessions/{id}/check`
- `POST /api/lab-sessions/{id}/reset`
- `WS /ws/lab-sessions/{id}/terminal`

## 当前边界

- 命令练习内容先覆盖 `pwd`、`ls -la`、`cd /var/log`、`touch file`、`chmod 644 file`、`systemctl status nginx`、`grep "error" app.log`。
- 默认 `InMemoryVmSandboxClient` 只模拟 VM 分配、终端输出和本地 transcript 判题，便于跑通实战验证闭环。
- 生产接入真实 VM 时，替换 `VmSandboxClient`，在 VM 内执行判题脚本，并把会话/锁状态迁移到 Redis。
- 当前版本不包含支付、商业化和移动端 App。

## 验证

```bash
cd backend && mvn test
cd frontend && npm run build
```
