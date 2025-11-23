# CTF平台测试指南

## 快速开始

### 1. 数据库初始化

首先执行数据库初始化脚本：

```bash
mysql -u root -p < Init.sql
```

### 2. 插入测试数据

执行测试数据脚本：

```bash
mysql -u root -p competition_system < test-data.sql
```

### 3. 启动后端服务

```bash
# 在项目根目录
./gradlew bootRun
# 或
gradle bootRun
```

后端服务将运行在 `http://localhost:8081`

### 4. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端服务将运行在 `http://localhost:5173`

## 测试账号

### 管理员账号
- **用户名**: `admin`
- **密码**: `123456`
- **角色**: ADMIN

### 普通用户账号
- **用户名**: `testuser1` - `testuser5`
- **密码**: `123456`
- **角色**: ORDINARY

## 测试数据说明

### 竞赛数据
1. **2024春季CTF挑战赛** (进行中)
   - 5道题目（Web、Crypto、Reverse、Pwn等）
   - 3个已审核通过的团队

2. **网络安全实战演练** (即将开始)
   - 2道题目（Web安全相关）
   - 2个团队（1个已审核，1个待审核）

3. **CTF新手训练营** (已结束)
   - 2道简单题目（Misc类型）

### 题目数据
- 总共9道题目，涵盖Web、Crypto、Reverse、Pwn、Misc等类别
- 难度从Easy到Hard不等
- 每道题目都有对应的Flag和提示

### 团队数据
- 5个团队，分布在不同的竞赛中
- 部分团队已有成员
- 包含待审核、已通过、已拒绝等不同状态的申请

## 功能测试清单

### 用户认证
- [x] 用户注册
- [x] 用户登录
- [x] 退出登录
- [x] 查看个人信息
- [x] 更新个人信息

### 竞赛功能
- [x] 查看竞赛列表
- [x] 查看竞赛详情
- [x] 查看题目列表
- [x] 提交Flag
- [x] 查看排行榜
- [x] 创建/加入团队

### 团队功能
- [x] 创建团队
- [x] 申请加入团队
- [x] 查看团队信息
- [x] 管理团队成员
- [x] 离开团队

### 管理员功能
- [x] 查看系统统计
- [x] 管理用户
- [x] 管理竞赛
- [x] 管理团队
- [x] 审核功能

## API测试

### 使用Postman或curl测试

#### 1. 用户登录
```bash
curl -X POST "http://localhost:8081/api/users/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "account=testuser1&password=123456" \
  -c cookies.txt
```

#### 2. 获取竞赛列表
```bash
curl -X GET "http://localhost:8081/api/competitions?page=0&size=20" \
  -b cookies.txt
```

#### 3. 获取题目列表
```bash
curl -X GET "http://localhost:8081/api/challenges?competitionId=1" \
  -b cookies.txt
```

#### 4. 提交Flag
```bash
curl -X POST "http://localhost:8081/api/challenges/1/submit" \
  -H "Content-Type: application/json" \
  -d '{"flag":"CTF{web_security_001}"}' \
  -b cookies.txt
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否运行
- 确认数据库用户名和密码正确
- 确认数据库`competition_system`已创建

### 2. 前后端通信失败
- 确认后端服务运行在8081端口
- 检查Vite代理配置
- 查看浏览器控制台的网络请求

### 3. Session问题
- 确保axios配置了`withCredentials: true`
- 检查后端CORS配置
- 清除浏览器Cookie后重新登录

### 4. 密码验证失败
- 测试数据中的密码哈希值可能需要根据实际BCrypt配置调整
- 如果登录失败，可以在后端代码中查看密码加密逻辑

## 注意事项

1. **密码加密**: 测试数据中的密码哈希值`$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`对应密码`123456`，如果后端使用不同的BCrypt配置，可能需要重新生成。

2. **时间字段**: 测试数据中的时间使用`NOW()`和相对时间，确保数据的时间逻辑正确。

3. **外键约束**: 插入数据时注意外键约束，确保引用的数据已存在。

4. **唯一约束**: 用户名、邮箱、手机号等字段有唯一约束，重复插入会失败。

## 下一步

1. 根据实际需求调整测试数据
2. 添加更多测试场景
3. 进行压力测试
4. 完善错误处理
5. 优化性能

