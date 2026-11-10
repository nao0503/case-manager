<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { statusLabels, type CaseItem } from './types/caseItem'

// 案件の一覧を入れる箱。中身が変わると、画面が自動で書き換わる
const cases = ref<CaseItem[]>([])

// 画面ができたら、APIから一覧を取ってくる
onMounted(async () => {
  const response = await fetch('/api/cases')
  cases.value = await response.json()
})
</script>

<template>
  <main>
    <h1>案件一覧</h1>
    <p>{{ cases.length }}件</p>
    <table>
      <thead>
        <tr>
          <th>顧客名</th>
          <th>担当者</th>
          <th>作業の種類</th>
          <th>ステータス</th>
          <th>期限</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="caseItem in cases" :key="caseItem.id">
          <td>{{ caseItem.customerName }}</td>
          <td>{{ caseItem.assignee ?? '-' }}</td>
          <td>{{ caseItem.workType }}</td>
          <td>{{ statusLabels[caseItem.status] }}</td>
          <td>{{ caseItem.dueDate }}</td>
        </tr>
      </tbody>
    </table>
  </main>
</template>

<style scoped>
table {
  border-collapse: collapse;
  width: 100%;
}

th,
td {
  border: 1px solid #ccc;
  padding: 6px 10px;
  text-align: left;
}

th {
  background-color: #f3f3f3;
}
</style>
