# sby-admin

### 🚀 项目简介
**sby-admin** 是一个基于 **Java 21** + **Vue 3** 构建的现代化全栈管理系统脚手架。项目追求代码简洁、环境纯净，利用最新的 JDK 特性提供高性能的后端支撑。

### 🛠 技术栈
#### **后端 (sby-server)**
* **核心框架**: `Spring Boot 3.3.x+` (基于 Jakarta EE 规范)
* **开发语言**: `Java 21` (全面启用 **虚拟线程** 优化，高并发利器)
* **持久层**: `MyBatis Plus 3.5.10.1` (采用插件拆分架构，按需引入 jsqlparser)
* **数据库连接池**: `Alibaba Druid 1.2.23` (集成内置 SQL 监控面板)
* **日志管理**: `SLF4J + Logback` (支持彩色控制台输出、按天滚动压缩归档)
* **数据库**: `MySQL 8.0+`

#### **前端 (sby-admin-web)**
* **框架**: `Vue 3 (Composition API)`
* **构建工具**: `Vite`
* **UI 组件库**: `Element Plus`

### 📂 项目结构
```text
sby-admin
├── sby-server              # 后端服务根目录 (Maven 多模块)
│   ├── sby-common          # 公共模块：存放实体类、工具类、常量定义
│   └── sby-service         # 业务模块：存放 Controller、Service 及项目启动入口
└── sby-admin-web           # 管理后台前端项目 (Vue 3 + Vite)
