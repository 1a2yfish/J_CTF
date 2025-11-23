# 组件分类说明

参考 [GZCTF](https://github.com/GZTimeWalker/GZCTF) 的组件组织方式，将组件按功能模块分类：

## 📁 目录结构

```
components/
├── layout/           # 布局组件
│   ├── DefaultLayout.vue    # 默认布局（包含侧边栏和头部）
│   ├── Header.vue           # 顶部导航栏
│   └── Sidebar.vue           # 侧边栏导航
│
├── pages/            # 页面视图组件（原 views/）
│   ├── DashboardView.vue           # 仪表盘页面
│   ├── CompetitionsView.vue         # 竞赛列表页面
│   ├── CompetitionDetailView.vue    # 竞赛详情页面
│   ├── TeamsView.vue                # 团队管理页面
│   └── ProfileView.vue              # 个人中心页面
│
├── auth/             # 认证相关组件
│   ├── LoginView.vue         # 登录页面
│   └── RegisterView.vue      # 注册页面
│
├── competition/      # 竞赛相关组件
│   └── CompetitionCard.vue   # 竞赛卡片组件
│
├── challenge/        # 题目相关组件
│   ├── ProblemCard.vue        # 题目卡片组件
│   └── FlagSubmission.vue   # Flag提交组件
│
├── team/             # 团队相关组件
│   └── TeamCard.vue          # 团队卡片组件
│
├── common/           # 通用组件（跨模块使用）
│   └── StatCard.vue          # 统计卡片组件
│
└── modals/           # 对话框组件
    ├── CreateCompetitionModal.vue   # 创建竞赛对话框
    └── JoinTeamModal.vue            # 加入团队对话框
```

## 🎯 组件分类原则

### 1. **按功能模块分类**
- **competition/** - 竞赛相关组件（竞赛卡片、竞赛列表等）
- **challenge/** - 题目相关组件（题目卡片、Flag提交等）
- **team/** - 团队相关组件（团队卡片、团队成员等）
- **auth/** - 认证相关组件（登录、注册页面）

### 2. **页面与组件分离**
- **pages/** - 存放页面视图组件（完整的页面）
- **components/** - 存放可复用的功能组件（卡片、表单等）

### 3. **通用组件独立**
- **common/** - 存放跨模块使用的通用组件（统计卡片、空状态等）

### 4. **布局组件独立**
- **layout/** - 存放整体布局相关组件（侧边栏、头部等）

### 5. **对话框组件独立**
- **modals/** - 存放所有对话框/弹窗组件

## 📝 使用示例

### 导入组件

```javascript
// 方式1：从具体路径导入
import CompetitionCard from '@/components/competition/CompetitionCard.vue'
import ProblemCard from '@/components/challenge/ProblemCard.vue'
import TeamCard from '@/components/team/TeamCard.vue'

// 方式2：从统一导出文件导入（推荐）
import { CompetitionCard, ProblemCard, TeamCard } from '@/components'
```

### 在页面中使用

```vue
<template>
  <div>
    <CompetitionCard 
      v-for="comp in competitions" 
      :key="comp.id"
      :competition="comp"
    />
  </div>
</template>

<script setup>
import { CompetitionCard } from '@/components/competition'
// 或
import CompetitionCard from '@/components/competition/CompetitionCard.vue'
</script>
```

## 🔄 迁移说明

从旧的 `views/` 和 `common/` 结构迁移到新的分类结构：

| 旧路径 | 新路径 |
|--------|--------|
| `views/*.vue` | `pages/*.vue` |
| `common/CompetitionCard.vue` | `competition/CompetitionCard.vue` |
| `common/ProblemCard.vue` | `challenge/ProblemCard.vue` |
| `common/FlagSubmission.vue` | `challenge/FlagSubmission.vue` |
| `common/TeamCard.vue` | `team/TeamCard.vue` |
| `common/StatCard.vue` | `common/StatCard.vue` (保持不变) |

## 📚 参考

- [GZCTF GitHub](https://github.com/GZTimeWalker/GZCTF) - 参考其组件组织方式
- [Vue 3 组件最佳实践](https://vuejs.org/guide/components/registration.html)
