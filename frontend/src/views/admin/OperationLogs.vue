<template>
  <div class="operation-logs">
    <div class="page-header">
      <h1>操作日志</h1>
      <div class="breadcrumb">管理后台 / 操作日志</div>
    </div>
    
    <div class="content-card">
      <div class="coming-soon">
        <el-icon size="80" color="#13c2c2"><Document /></el-icon>
        <h2>操作日志功能</h2>
        <p>此功能正在开发中，敬请期待...</p>
        <el-button type="primary" @click="$router.push('/admin')">返回仪表盘</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.operation-logs {
  padding: 24px;
}

.page-header {
  background: white;
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-header h1 {
  font-size: 20px;
  font-weight: 500;
  color: #262626;
  margin: 0;
}

.breadcrumb {
  margin-top: 8px;
  color: #8c8c8c;
  font-size: 14px;
}

.content-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.coming-soon {
  text-align: center;
  padding: 80px 20px;
  color: #606266;
}

.coming-soon h2 {
  margin: 20px 0 10px 0;
  color: #303133;
}

.coming-soon p {
  margin-bottom: 30px;
}
</style>