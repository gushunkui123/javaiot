# 仓库指南

## 项目结构与模块组织
- `agileboot-admin`：Spring Boot 启动模块和 Web 控制器；这是主要的可运行后端应用。
- `agileboot-domain`：核心业务逻辑、领域模型、命令、工厂以及面向数据库的服务。
- `agileboot-infrastructure`：框架集成，例如安全、缓存、MyBatis Plus、调度、过滤器和运行时配置。
- `agileboot-common`：共享常量、枚举、异常、注解和工具类。
- `agileboot-api`：为外部或面向应用的接口预留的 API / 定制化扩展层。
- `sql/` 存放数据库初始化脚本，`docs/` 存放项目文档，`docker/` 包含与容器相关的资源。

## 项目技术栈
- 语言与构建：Java 21、Maven Wrapper（`mvnw.cmd`）、多模块 Maven 聚合工程。
- 核心框架：Spring Boot 3.2.5、Spring MVC、Spring AOP、Spring Task。
- 安全认证：Spring Security、JWT（`jjwt 0.12.5`）、BCrypt、方法级权限控制。
- 数据访问：MyBatis Spring Boot 3.0.3、MyBatis Plus 3.5.5、Dynamic Datasource 4.3.0、PageHelper 2.1.0。
- 数据存储与连接：MySQL 8.3.0、Druid 1.2.21、Redis（`spring-boot-starter-data-redis`）；测试 / 本地场景包含 H2 与 embedded-redis。
- 接口与运行：`agileboot-admin` 基于 Spring Boot Web；`agileboot-api` 使用 Undertow 作为容器；接口文档使用 Springdoc OpenAPI 2.3.0。
- 常用组件：Lombok 1.18.30、Hutool 5.8.25、Guava 31.0.1-jre、Jackson、Apache POI 4.1.2、Velocity 2.3、Kaptcha 2.3.2、ip2region 2.6.5、OSHI 6.4.0。
- 测试栈：JUnit 5、Spring Boot Test、Mockito 4.11.0。

## 构建、测试与开发命令
- `.\mvnw.cmd clean install`：构建所有模块并打包项目。
- `.\mvnw.cmd test -DskipTests=false`：在仓库根目录运行测试套件。父级 `pom.xml` 默认设置了 `skipTests=true`，因此需要显式传入该参数。
- `.\mvnw.cmd -pl agileboot-domain test -DskipTests=false`：只运行单个模块的测试。
- `.\mvnw.cmd -pl agileboot-admin spring-boot:run -Dspring-boot.run.profiles=dev`：使用 `dev` 配置启动后端。
- 本地启动前先导入 `sql/` 中最新的脚本，然后更新 `agileboot-admin/src/main/resources/application-dev.yml`。

## 编码风格与命名约定
- 使用 Java 21、UTF-8，并遵循仓库中的 Google 风格文件：`GoogleStyle.xml`。
- 使用 4 空格缩进，包名使用小写，类名使用 `PascalCase`，字段和方法使用 `camelCase`。
- 遵循现有后缀约定：`*Controller`、`*Model`、`*Factory`、`*Service`、`*Command`、`*Entity`、`*Test`。
- 业务规则保留在 `agileboot-domain`；传输层 / Web 相关逻辑保留在 `agileboot-admin`。

## 测试指南
- 测试使用 JUnit 5、Spring Boot Test 和 Mockito。
- 测试代码放在 `src/test/java` 下；测试资源放在 `src/test/resources` 下。
- 使用与生产类型对应的 `*Test.java` 命名，例如 `UserModelTest`。
- 当修改持久化、权限或特定 profile 的行为时，需要补充集成测试覆盖。现有数据库集成测试位于 `agileboot-domain/src/test/java/com/agileboot/integrationTest/db`。

## 提交与 Pull Request 指南
- 当前 Git 历史较少（`init`），因此优先使用带作用域的简短祈使句提交标题，例如 `domain: validate menu parent rules`。
- 保持提交聚焦；将重构与行为变更分开。
- PR 需要包含清晰摘要、受影响模块、配置或 SQL 变更、关联 issue 以及测试依据。若接口或鉴权行为发生变化，请附上请求 / 响应示例。

## 安全与配置提示
- 不要在 `application-*.yml` 中提交真实密钥。
- 仓库文档和自动化脚本示例统一使用 PowerShell 命令。
- 修改表结构或种子数据时，请同步更新 `sql/`，并在 PR 中说明迁移影响。
