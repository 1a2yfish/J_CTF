# CTF平台后端接口文档（整合版）


## 一、文档说明
1. 本文档整合CTF平台核心接口，涵盖**管理员系统**、**用户认证**、**题目管理**、**竞赛管理**、**战队管理**五大模块，统一规范接口描述格式。
2. 接口基础路径区分模块：
    - 管理员接口：`/api/admin`
    - 用户认证接口：`/api/users`
    - 题目管理接口：`/api/challenges`
    - 竞赛管理接口：`/api/competitions`（注：管理员接口中竞赛相关接口与本模块合并，统一在此处展示）
3. 权限说明：
    - 管理员接口需携带 `userRole=ADMIN`（请求头/属性），否则返回403。
    - 用户认证接口中，登录/注册接口无需权限，查询用户信息需登录。
    - 题目/竞赛管理接口中，创建/更新/删除等操作需管理员/创建者权限，普通用户仅可查看公开信息（敏感字段隐藏）。
4. 分页默认参数：`page=0`（第一页）、`size=20`，分页结果包含`totalPages`（总页数）、`totalElements`（总条数）、`currentPage`（当前页）。


## 二、通用响应格式
### 1. 管理员/题目/竞赛/战队模块响应格式
#### 成功响应（带数据）
```json
{
  "success": true,
  "message": "操作成功提示",
  "data": {} // 具体业务数据
}
```

#### 成功响应（无数据）
```json
{
  "success": true,
  "message": "操作成功提示"
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "操作失败原因"
}
```

### 2. 用户认证模块响应格式
#### 成功响应
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {} // User/OrdinaryUser对象或null
}
```

#### 失败响应
```json
{
  "code": 500, // 状态码随业务场景变化（如400参数错误）
  "msg": "具体错误信息",
  "data": null
}
```


## 三、系统统计模块（管理员）
### 1. 获取仪表盘统计数据
- **接口路径**：`/api/admin/dashboard`
- **请求方法**：GET
- **描述**：获取系统总用户数、战队数、竞赛数等统计数据。
- **参数**：无
- **响应数据**：
  ```json
  {
    "success": true,
    "message": "获取成功",
    "data": {
      "totalUser": 100,
      "totalTeam": 20,
      "totalCompetition": 5
      // 其他统计字段由业务层定义
    }
  }
  ```
- **状态码**：200（成功）、403（权限不足）、500（服务器错误）


## 四、用户管理模块
### （一）管理员用户管理接口
#### 1. 获取用户列表
- **接口路径**：`/api/admin/users`
- **请求方法**：GET
- **描述**：分页查询用户列表，支持关键字搜索和排序。
- **查询参数**：
  | 参数名 | 类型 | 必填 | 默认值 | 说明 |
  |--------|------|------|--------|------|
  | page   | int  | 否   | 0      | 页码（从0开始） |
  | size   | int  | 否   | 20     | 每页条数 |
  | sort   | string | 否 | registerTime | 排序字段（如registerTime、userName） |
  | keyword | string | 否 | - | 匹配用户名、邮箱等 |
- **响应数据**：
  ```json
  {
    "success": true,
    "message": "获取成功",
    "data": {
      "users": [
        {
          "userID": 1,
          "userName": "test",
          "email": "test@ctf.com",
          "status": "ENABLED",
          "registerTime": "2025-11-20 10:00:00"
        }
      ],
      "totalPages": 5,
      "totalElements": 100,
      "currentPage": 0
    }
  }
  ```
- **状态码**：200、403、500

#### 2. 获取用户详情
- **接口路径**：`/api/admin/users/{userId}`
- **请求方法**：GET
- **描述**：根据用户ID查询详细信息。
- **路径参数**：`userId`（int，必填，用户ID）
- **响应数据**：包含用户ID、用户名、邮箱、手机号等完整信息（格式同上）。
- **状态码**：200、400（用户不存在）、403、500

#### 3. 更新用户信息
- **接口路径**：`/api/admin/users/{userId}`
- **请求方法**：PUT
- **描述**：更新用户信息（用户名、邮箱、状态等）。
- **参数**：
    - 路径参数：`userId`（必填）
    - 请求体（JSON）：
      ```json
      {
        "userName": "newName",
        "email": "new@ctf.com",
        "phone": "13900139000",
        "status": "ENABLED"
      }
      ```
- **响应数据**：更新后的用户完整信息。
- **状态码**：200、400（参数错误/用户不存在）、403、500

#### 4. 禁用/启用用户
- **禁用接口**：`/api/admin/users/{userId}/disable`（POST）
- **启用接口**：`/api/admin/users/{userId}/enable`（POST）
- **描述**：修改用户状态（禁用/启用）。
- **路径参数**：`userId`（必填）
- **响应数据**：`{"success": true, "message": "禁用/启用用户成功"}`
- **状态码**：200、400（操作失败）、403、500

#### 5. 删除用户
- **接口路径**：`/api/admin/users/{userId}`
- **请求方法**：DELETE
- **描述**：删除指定用户。
- **路径参数**：`userId`（必填）
- **响应数据**：`{"success": true, "message": "删除用户成功"}`
- **状态码**：200、400（删除失败）、403、500


### （二）用户认证接口（通用用户）
#### 1. 通用用户注册
- **接口路径**：`/api/users/register`
- **请求方法**：POST
- **描述**：通过完整User对象注册。
- **请求体（JSON）**：
  ```json
  {
    "account": "ctf_user01",
    "password": "123456Ab",
    "userName": "CTF用户01",
    "userEmail": "user01@ctf.com",
    "phoneNumber": "13800138000"
  }
  ```
- **响应数据**：注册后的用户信息（密码脱敏）。
- **状态码**：200（成功）、500（失败，如账号已存在）

#### 2. 普通用户注册
- **接口路径**：`/api/users/register/ordinary`
- **请求方法**：POST
- **描述**：通过表单参数注册普通用户。
- **表单参数**：
  | 参数名 | 类型 | 必填 | 说明 |
  |--------|------|------|------|
  | userPassword | string | 是 | 用户密码 |
  | phoneNumber | string | 是 | 手机号 |
  | userEmail | string | 是 | 邮箱 |
  | userName | string | 是 | 用户名 |
  | gender | string | 是 | 性别（男/女/未知） |
  | schoolWorkunit | string | 是 | 学校/工作单位 |
- **响应数据**：注册后的普通用户信息。
- **状态码**：200、500（失败，如邮箱格式错误）

#### 3. 用户登录
- **接口路径**：`/api/users/login`
- **请求方法**：POST
- **描述**：通过账号和密码登录。
- **表单参数**：
  | 参数名 | 类型 | 必填 | 说明 |
  |--------|------|------|------|
  | account | string | 是 | 账号/邮箱/手机号 |
  | password | string | 是 | 密码 |
- **响应数据**：登录用户信息（成功）；`{"code":500,"msg":"账号或密码错误","data":null}`（失败）。
- **状态码**：200（成功）、500（失败）

#### 4. 查询用户信息
- **接口路径1**：`/api/users/{id}`（GET，按ID查询）
- **接口路径2**：`/api/users/account/{account}`（GET，按账号查询）
- **描述**：查询用户完整信息（需登录）。
- **路径参数**：`id`（int）或`account`（string），必填。
- **响应数据**：用户信息（成功）；`{"code":500,"msg":"用户不存在","data":null}`（失败）。
- **状态码**：200、500


## 五、战队管理模块（管理员）
### 1. 获取战队列表
- **接口路径**：`/api/admin/teams`
- **请求方法**：GET
- **描述**：分页查询战队列表，支持按竞赛ID、审核状态筛选。
- **查询参数**：
  | 参数名 | 类型 | 必填 | 默认值 | 说明 |
  |--------|------|------|--------|------|
  | page   | int  | 否   | 0      | 页码 |
  | size   | int  | 否   | 20     | 每页条数 |
  | competitionId | int | 否 | - | 筛选指定竞赛的战队 |
  | auditState | string | 否 | - | 审核状态（PENDING/APPROVED/REJECTED） |
- **响应数据**：
  ```json
  {
    "success": true,
    "message": "获取成功",
    "data": {
      "teams": [
        {
          "teamId": 1,
          "teamName": "CTF战队",
          "leaderId": 1,
          "competitionId": 1,
          "auditState": "APPROVED",
          "creationTime": "2025-11-20 10:00:00"
        }
      ],
      "totalPages": 3,
      "totalElements": 50,
      "currentPage": 0
    }
  }
  ```
- **状态码**：200、403、500

### 2. 获取战队详情
- **接口路径**：`/api/admin/teams/{teamId}`
- **请求方法**：GET
- **描述**：查询战队详细信息（含队员列表）。
- **路径参数**：`teamId`（必填）
- **响应数据**：战队完整信息。
- **状态码**：200、400（战队不存在）、403、500

### 3. 审核战队
- **接口路径**：`/api/admin/teams/{teamId}/audit`
- **请求方法**：POST
- **描述**：更新战队审核状态。
- **参数**：
    - 路径参数：`teamId`（必填）
    - 请求体（JSON）：
      ```json
      {
        "auditState": "APPROVED", // 或REJECTED
        "auditRemark": "审核通过" // 可选
      }
      ```
- **响应数据**：审核后的战队完整信息。
- **状态码**：200、400（参数错误/战队不存在）、403、500

### 4. 获取竞赛战队统计
- **接口路径**：`/api/admin/competitions/{competitionId}/team-stats`
- **请求方法**：GET
- **描述**：查询指定竞赛的战队统计（总数、通过数等）。
- **路径参数**：`competitionId`（必填）
- **响应数据**：
  ```json
  {
    "success": true,
    "message": "获取成功",
    "data": {
      "totalTeam": 20,
      "approvedTeam": 18,
      "pendingTeam": 2
    }
  }
  ```
- **状态码**：200、403、500


## 六、竞赛管理模块
### 1. 创建竞赛
- **接口路径**：`/api/competitions`
- **请求方法**：POST
- **描述**：登录用户创建竞赛（创建者获管理权限）。
- **请求体（JSON）**：
  ```json
  {
    "competitionName": "2025年春季CTF大赛",
    "description": "面向高校的竞赛",
    "startTime": "2025-12-01 09:00:00",
    "endTime": "2025-12-01 18:00:00",
    "type": "PUBLIC", // 公开/私有
    "maxTeamSize": 5,
    "registrationDeadline": "2025-11-30 23:59:59"
  }
  ```
- **响应数据**：创建后的竞赛信息（初始状态为`DRAFT`）。
- **状态码**：200、401（未登录）、400（参数错误）、500

### 2. 更新竞赛
- **接口路径**：`/api/competitions/{competitionId}`
- **请求方法**：PUT
- **描述**：竞赛创建者更新信息。
- **参数**：
    - 路径参数：`competitionId`（必填）
    - 请求体（JSON）：同创建竞赛（可修改字段）。
- **响应数据**：更新后的竞赛信息。
- **状态码**：200、401（未登录）、403（无权限）、400（参数错误）、500

### 3. 发布/取消竞赛
- **发布接口**：`/api/competitions/{competitionId}/publish`（POST）
- **取消接口**：`/api/competitions/{competitionId}/cancel`（POST）
- **描述**：创建者发布（状态改为`PUBLISHED`）或取消（状态改为`CANCELLED`）竞赛。
- **路径参数**：`competitionId`（必填）
- **响应数据**：操作后的竞赛信息（含状态更新）。
- **状态码**：200、401、403、400（非草稿状态/非发布状态）、500

### 4. 删除竞赛
- **接口路径**：`/api/competitions/{competitionId}`
- **请求方法**：DELETE
- **描述**：创建者删除竞赛（已开始的竞赛不允许删除）。
- **路径参数**：`competitionId`（必填）
- **响应数据**：`{"success": true, "message": "删除竞赛成功"}`
- **状态码**：200、401、403、400（删除失败/竞赛已开始）、500

### 5. 获取竞赛详情
- **接口路径**：`/api/competitions/{competitionId}`
- **请求方法**：GET
- **描述**：查询竞赛完整信息（公开竞赛无需登录，私有竞赛仅限创建者和参赛者）。
- **路径参数**：`competitionId`（必填）
- **响应数据**：竞赛完整信息（含规则、时间等）。
- **状态码**：200、404（不存在）、500

### 6. 获取竞赛列表
- **接口路径**：`/api/competitions`
- **请求方法**：GET
- **描述**：分页查询竞赛列表，支持按状态、创建者筛选。
- **查询参数**：
  | 参数名 | 类型 | 必填 | 默认值 | 说明 |
  |--------|------|------|--------|------|
  | page   | int  | 否   | 0      | 页码 |
  | size   | int  | 否   | 20     | 每页条数 |
  | sort   | string | 否 | publishTime | 排序字段 |
  | type   | string | 否 | - | 类型：ongoing/upcoming/finished/my（需登录） |
  | keyword | string | 否 | - | 匹配名称/描述 |
- **响应数据**：竞赛列表及分页信息。
- **状态码**：200、401（筛选`my`时未登录）、500

### 7. 竞赛统计（管理员/创建者）
- **接口路径**：`/api/competitions/{competitionId}/stats`
- **请求方法**：GET
- **描述**：查询竞赛统计（参赛人数、提交数等）。
- **路径参数**：`competitionId`（必填）
- **响应数据**：
  ```json
  {
    "success": true,
    "message": "获取成功",
    "data": {
      "totalParticipant": 200,
      "totalTeam": 50,
      "submitCount": 1000,
      "solveCount": 500
    }
  }
  ```
- **状态码**：200、403、500

### 8. 检查参赛权限
- **接口路径**：`/api/competitions/{competitionId}/can-join`
- **请求方法**：GET
- **描述**：检查当前用户是否可参加竞赛（报名未截止等）。
- **路径参数**：`competitionId`（必填）
- **响应数据**：`{"success": true, "message": "获取成功", "data": {"canJoin": true}}`
- **状态码**：200、401（未登录）、500


## 七、题目管理模块
### 1. 创建题目
- **接口路径**：`/api/challenges`
- **请求方法**：POST
- **描述**：管理员/竞赛创建者创建题目。
- **请求体（JSON）**：
  ```json
  {
    "competitionId": 1,
    "title": "Web安全入门题",
    "category": "Web",
    "difficulty": "Easy",
    "description": "找到Flag",
    "flag": "CTF{web_123}",
    "score": 100,
    "hintCount": 2
  }
  ```
- **响应数据**：创建后的完整题目信息。
- **状态码**：200、401（未登录）、400（参数错误）、500

### 2. 更新/删除题目
- **更新接口**：`/api/challenges/{challengeId}`（PUT）
- **删除接口**：`/api/challenges/{challengeId}`（DELETE）
- **描述**：管理员/创建者更新或删除题目。
- **参数**：
    - 路径参数：`challengeId`（必填）
    - 更新请求体：同创建题目（可修改字段）。
- **响应数据**：更新后的题目信息（更新）；`{"success": true, "message": "删除成功"}`（删除）。
- **状态码**：200、401、400、500

### 3. 获取题目详情
- **接口路径**：`/api/challenges/{challengeId}`
- **请求方法**：GET
- **描述**：查询题目信息（普通用户隐藏`flag`）。
- **路径参数**：`challengeId`（必填）
- **响应数据**：
    - 管理员：含`flag`的完整信息。
    - 普通用户：`flag`字段为`null`。
- **状态码**：200、404（不存在）、500

### 4. 获取题目列表
- **接口路径**：`/api/challenges`
- **请求方法**：GET
- **描述**：分页查询题目，支持按竞赛、类别、难度筛选。
- **查询参数**：
  | 参数名 | 类型 | 必填 | 默认值 | 说明 |
  |--------|------|------|--------|------|
  | page   | int  | 否   | 0      | 页码 |
  | size   | int  | 否   | 20     | 每页条数 |
  | competitionId | int | 否 | - | 所属竞赛ID |
  | category | string | 否 | - | 类别（Web/PWN等） |
  | difficulty | string | 否 | - | 难度（Easy/Medium/Hard） |
  | keyword | string | 否 | - | 匹配名称/描述 |
- **响应数据**：题目列表及分页信息。
- **状态码**：200、500

### 5. Flag提交
- **接口路径**：`/api/challenges/{challengeId}/submit`
- **请求方法**：POST
- **描述**：用户提交Flag（需登录）。
- **参数**：
    - 路径参数：`challengeId`（必填）
    - 请求体（JSON）：`{"flag": "提交的Flag"}`
- **响应数据**：
    - 正确：`{"success": true, "message": "提交成功", "data": {"isCorrect": true, "message": "正确"}}`
    - 错误：`{"success": true, "message": "提交成功", "data": {"isCorrect": false, "message": "错误"}}`
- **状态码**：200、401（未登录）、400（Flag为空）、500

### 6. 题目提示管理
- **获取提示**：`/api/challenges/{challengeId}/hints`（GET）
- **添加提示**：`/api/challenges/{challengeId}/hints`（POST，请求体`{"content": "提示内容"}`）
- **删除提示**：`/api/challenges/hints/{hintId}`（DELETE）
- **权限**：添加/删除需管理员/创建者权限。
- **响应数据**：提示列表（获取）；创建的提示信息（添加）；删除成功提示（删除）。
- **状态码**：200、401、400、500

### 7. 提交记录与统计
- **提交记录**：`/api/challenges/{challengeId}/submissions`（GET，管理员/创建者，分页查询）。
- **竞赛题目统计**：`/api/challenges/competitions/{competitionId}/stats`（GET，总题数、难度分布等）。
- **竞赛题目类别**：`/api/challenges/competitions/{competitionId}/categories`（GET，返回类别列表）。
- **检查是否解决**：`/api/challenges/{challengeId}/solved`（GET，登录用户，返回`{"solved": true/false}`）。


## 补充说明
1. 敏感信息安全：密码传输需HTTPS，存储需哈希加密（如BCrypt），响应中禁止返回明文密码；普通用户查询题目时隐藏`flag`。
2. 权限校验：业务层需实现细粒度权限控制（如创建者校验、管理员权限校验）。
3. IP溯源：Flag提交记录需记录用户IP（通过`X-Forwarded-For`或`request.getRemoteAddr()`获取）。
4. 状态流转：竞赛状态（DRAFT→PUBLISHED→ONGOING→ENDED/CANCELLED）、战队审核状态（PENDING→APPROVED/REJECTED）需严格按业务逻辑流转。