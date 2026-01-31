# sby-admin

### 🚀 项目简介
**sby-admin** 是一个基于 **Java 21** + **Vue 3** 构建的现代化全栈管理系统脚手架。项目追求代码简洁、环境纯净，利用最新的 JDK 特性（如虚拟线程）提供高性能的后端支撑。

---

### 🛠 技术栈

#### **后端 (sby-server)**
* **核心框架**: `Spring Boot 3.5.10`
* **开发语言**: `Java 21`
* **持久层**: `MyBatis Plus 3.5.10.1`
* **安全认证**: `JWT (JJWT)` + 自定义双 Token 续期机制
* **数据库连接池**: `Alibaba Druid 1.2.23`
* **日志管理**: `SLF4J + Logback`
* **数据库**: `MySQL 8.0+`

#### **前端 (sby-admin-web)**
* **框架**: `Vue 3 (Composition API)`
* **构建工具**: `Vite`
* **UI 组件库**: `Element Plus`

---

### 🔑 认证与安全设计

1. **登录认证**：用户登录成功后，系统返回 `accessToken`（短效）与 `refreshToken`（长效）。
2. **静默刷新**：当 `accessToken` 过期时，拦截器触发 `401` 状态码，前端自动通过 `refreshToken` 接口实现无感换新，用户无需重复登录。
3. **双重校验**：Token 不仅包含 JWT 自定义载荷，还需通过 **Redis** 校验其是否在黑名单中，实现对登录态的实时吊销管理。

---

### 📂 项目结构
```text
sby-admin
├── sby-server             # 后端服务根目录 (Maven 多模块)
│   ├── sby-common         # 公共模块：存放实体类、工具类、常量定义
│   └── sby-service        # 业务模块：存放 Controller、Service 及项目启动入口
└── sby-admin-web          # 管理后台前端项目 (Vue 3 + Vite)
