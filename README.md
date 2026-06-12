# 助农商城管理系统

基于 Spring Boot 3 + Vue 3 的全栈电商管理系统，本科毕业设计项目。

## 项目简介

助农商城管理系统是一个面向农产品销售的B2C电商平台，包含前端用户商城和管理后台两大部分。系统采用前后端分离架构，支持商品管理、订单管理、用户管理、数据可视化等核心功能。

## 技术栈

### 后端
- **Spring Boot 3.4** - Java企业级应用框架
- **MyBatis-Plus 3.5** - ORM框架
- **Spring Security + JWT** - 认证授权
- **MySQL 8.0** - 关系型数据库
- **七牛云 OSS** - 对象存储（图片上传）
- **Knife4j** - API文档

### 管理后台前端
- **Vue 3.5** + **TypeScript 5.6**
- **Element Plus 2.11** - UI组件库
- **Vite 7.1** - 构建工具
- **Pinia 3.0** - 状态管理
- **ECharts 6.0** - 数据可视化
- **Axios** - HTTP客户端

### 用户商城前端
- **Vue 3** + **Vite**
- **Element Plus**
- **Pinia**

## 项目结构

```
助农商城/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/          # Java源代码
│   ├── src/main/resources/     # 配置文件
│   │   ├── application.yml          # 主配置（环境切换）
│   │   ├── application-example.yml  # 示例配置（安全，可提交）
│   │   └── application-dev.yml      # 本地开发配置（敏感，勿提交）
│   └── pom.xml
│
├── art-lnb-master/             # 管理后台前端（Vue3）
│   ├── src/                    # 源代码
│   ├── .env.example            # 环境变量示例
│   ├── .env.development.example
│   └── package.json
│
├── zhunong-shop/               # 用户商城前端（Vue3）
│   ├── src/
│   ├── .env.example
│   └── package.json
│
├── .gitignore                  # Git忽略规则
└── README.md                   # 项目说明
```

## 快速开始

### 前置要求

- JDK 17+
- Node.js 20+
- MySQL 8.0+
- Maven 3.9+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/zhunong-mall.git
cd zhunong-mall
```

### 2. 配置后端

```bash
cd backend

# 复制示例配置为本地配置
cp src/main/resources/application-example.yml src/main/resources/application-dev.yml

# 编辑 application-dev.yml，填写你的真实配置
# - 数据库账号密码
# - 七牛云 AccessKey / SecretKey
# - JWT 密钥
```

**application-dev.yml 关键配置项：**

```yaml
spring:
  datasource:
    username: your_db_username      # 数据库用户名
    password: your_db_password      # 数据库密码

qiniu:
  access-key: your_access_key       # 七牛云AccessKey
  secret-key: your_secret_key       # 七牛云SecretKey
  bucket: your_bucket_name          # 存储空间名
  domain: https://your-domain.com   # CDN域名

jwt:
  secret: your_strong_secret_key    # JWT签名密钥（至少32位随机字符串）
```

### 3. 初始化数据库

```bash
# 登录MySQL，创建数据库
create database zhunong-shangcheng character set utf8mb4 collate utf8mb4_unicode_ci;

# 执行 schema.sql 初始化表结构和测试数据
mysql -u root -p zhunong-shangcheng < backend/src/main/resources/schema.sql
```

### 4. 启动后端

```bash
# 方式一：IDEA中直接运行 Application.java
# 方式二：命令行
mvn spring-boot:run

# 后端服务默认运行在 http://localhost:8080/api
# API文档：http://localhost:8080/api/doc.html
```

### 5. 配置并启动管理后台

```bash
cd ../art-lnb-master

# 安装依赖
npm install

# 复制环境变量示例
cp .env.development.example .env.development

# 启动开发服务器
npm run dev

# 管理后台默认运行在 http://localhost:3007
```

### 6. 配置并启动用户商城（可选）

```bash
cd ../zhunong-shop
npm install
npm run dev
```

### 7. 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 管理后台登录 |
| 测试用户 | user1 | user123 | 前端商城测试 |

## 核心功能

### 管理后台
- [x] 用户管理（列表、搜索、状态管理）
- [x] 商品管理（CRUD、分类、上下架）
- [x] 订单管理（列表、状态更新）
- [x] 轮播图管理（上传、排序、启用禁用）
- [x] 活动管理
- [x] 商品评价管理
- [x] 数据可视化（用户收藏统计、趋势分析）
- [x] 管理员管理

### 用户商城
- [x] 用户注册/登录
- [x] 商品浏览与搜索
- [x] 购物车管理
- [x] 订单创建与查看
- [x] 收货地址管理
- [x] 个人中心

## 安全说明

本项目已对所有敏感配置进行抽离处理：

- **数据库密码** - 通过 `application-dev.yml` 本地配置，不提交到Git
- **七牛云密钥** - 同上，通过环境变量或本地配置文件注入
- **JWT密钥** - 同上，生产环境务必使用强随机字符串
- **前端密钥** - 通过 `.env` 文件本地配置

**提交到GitHub的代码中不包含任何真实密钥。**

## 部署说明

### 后端部署

```bash
cd backend
mvn clean package -DskipTests
java -jar target/zhunong-mall-1.0.0.jar
```

生产环境建议：
- 使用 `application-prod.yml` 配置
- 配置Nginx反向代理
- 使用HTTPS
- 配置数据库连接池

### 前端部署

```bash
cd art-lnb-master
npm run build
# 将 dist/ 目录部署到Nginx或静态托管服务
```

## 开发团队

- 作者：[你的名字]
- 学校：广西民族大学人工智能学院
- 项目性质：本科毕业设计

## 开源协议

MIT License
