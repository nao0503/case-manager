<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { dueGroupLabels, getDueGroup, toDateString, type DueGroup } from './utils/dueGroups'
import { statusLabels, workTypes, type CaseForm, type CaseItem } from './types/caseItem'

// 案件の一覧を入れる箱。中身が変わると、画面が自動で書き換わる
const cases = ref<CaseItem[]>([])

// 絞り込みの条件（空なら「絞り込まない」）
const filterStatus = ref('')
const filterAssignee = ref('')

// 一覧をAPIから読み込む（最初の表示と、保存の後に使う）
async function loadCases() {
  const params = new URLSearchParams()
  if (filterStatus.value) params.set('status', filterStatus.value)
  if (filterAssignee.value) params.set('assignee', filterAssignee.value)

  const query = params.toString()
  const response = await fetch(query ? `/api/cases?${query}` : '/api/cases')
  cases.value = await response.json()
}

// 区切りの表示順
const groupOrder: DueGroup[] = ['overdue', 'thisWeek', 'thisMonth', 'later']

// 案件を4つの区切りに分けたもの（cases が変わると、自動で計算し直される）
const groupedCases = computed(() => {
  const today = new Date()
  return groupOrder.map((group) => ({
    group: group,
    label: dueGroupLabels[group],
    items: visibleCases.value.filter((caseItem) => getDueGroup(caseItem.dueDate, today) === group),
  }))
})

// 画面に見せる案件（期限が過ぎた完了案件は、「完了」で絞り込んだときだけ見せる）
const visibleCases = computed(() => {
  const todayString = toDateString(new Date())
  return cases.value.filter((caseItem) => {
    const isOverdueDone = caseItem.status === 'DONE' && caseItem.dueDate < todayString
    return !isOverdueDone || filterStatus.value === 'DONE'
  })
})

// 「解除」ボタン：絞り込みをやめて、全件を出す
function clearFilter() {
  filterStatus.value = ''
  filterAssignee.value = ''
  loadCases()
}

onMounted(loadCases)

// パネルが開いているか
const isPanelOpen = ref(false)

// パネルの入力欄とつながる箱
const form = ref<CaseForm>(emptyForm())

// APIから返ってきたエラー（項目名 → メッセージ）
const errors = ref<Record<string, string>>({})

// 編集中の案件のid（null なら新規登録）
const editingId = ref<number | null>(null)

// 空の入力欄（新規登録のとき）
function emptyForm(): CaseForm {
  return {
    customerName: '',
    workType: '',
    dueDate: '',
    assignee: '',
    status: 'NOT_STARTED',
    memo: '',
  }
}

// 「新規登録」ボタン：空の入力欄でパネルを開く
function openNew() {
  editingId.value = null
  form.value = emptyForm()
  errors.value = {}
  isPanelOpen.value = true
}

// 一覧の行を押したとき：その案件の内容でパネルを開く
function openEdit(caseItem: CaseItem) {
  editingId.value = caseItem.id
  form.value = {
    customerName: caseItem.customerName,
    workType: caseItem.workType,
    dueDate: caseItem.dueDate,
    assignee: caseItem.assignee ?? '',
    status: caseItem.status,
    memo: caseItem.memo ?? '',
  }
  errors.value = {}
  isPanelOpen.value = true
}

// 「キャンセル」ボタン：パネルを閉じる
function closePanel() {
  isPanelOpen.value = false
}

// 「保存」ボタン：入力内容をPOSTで送る
async function save() {
  const body = {
    ...form.value,
    dueDate: form.value.dueDate || null,
    assignee: form.value.assignee || null,
    memo: form.value.memo || null,
  }

  const isEdit = editingId.value !== null
  const url = isEdit ? `/api/cases/${editingId.value}` : '/api/cases'
  const method = isEdit ? 'PUT' : 'POST'

  const response = await fetch(url, {
    method: method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })

  if (response.ok) {
    isPanelOpen.value = false
    await loadCases()
  } else if (response.status === 400) {
    const data = await response.json()
    errors.value = data.errors
  } else if (response.status === 404) {
    alert('この案件はすでに削除されています')
    isPanelOpen.value = false
    await loadCases()
  } else {
    console.log('保存できませんでした', response.status)
  }
}

// 「削除」ボタン：確認してからDELETEを送る
async function remove() {
  if (editingId.value === null) return

  const ok = confirm(
    `「${form.value.customerName}」の案件を削除します。もとに戻せません。よろしいですか？`,
  )

  if (!ok) return
  const response = await fetch(`/api/cases/${editingId.value}`, { method: 'DELETE' })

  if (response.status === 404) {
    alert('この案件はすでに削除されています')
  } else if (!response.ok) {
    console.log('削除できませんでした', response.status)
    return
  }

  isPanelOpen.value = false
  await loadCases()
}
</script>

<template>
  <main>
    <div class="header">
      <h1>案件一覧</h1>
      <button type="button" @click="openNew">新規登録</button>
    </div>

    <div class="filters">
      <label>
        ステータス
        <select v-model="filterStatus" @change="loadCases">
          <option value="">すべて</option>
          <option v-for="(label, code) in statusLabels" :key="code" :value="code">
            {{ label }}
          </option>
        </select>
      </label>

      <label>
        担当者
        <input v-model="filterAssignee" type="text" placeholder="例：担当A" @change="loadCases" />
      </label>
      <button type="button" @click="clearFilter">解除</button>
    </div>

    <div class="layout">
      <section class="list">
        <p>{{ visibleCases.length }}件</p>
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
          <tbody v-for="g in groupedCases" :key="g.group">
            <tr class="group-row">
              <th colspan="5">{{ g.label }}（{{ g.items.length }}件）</th>
            </tr>
            <tr
              v-for="caseItem in g.items"
              :key="caseItem.id"
              :class="{
                selected: caseItem.id === editingId && isPanelOpen,
                overdue: g.group === 'overdue' && caseItem.status !== 'DONE',
              }"
              @click="openEdit(caseItem)"
            >
              <td>{{ caseItem.customerName }}</td>
              <td>{{ caseItem.assignee ?? '-' }}</td>
              <td>{{ caseItem.workType }}</td>
              <td>{{ statusLabels[caseItem.status] }}</td>
              <td>{{ caseItem.dueDate }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <aside v-if="isPanelOpen" class="panel">
        <h2>{{ editingId === null ? '新規登録' : '編集' }}</h2>

        <label>
          顧客名
          <input v-model="form.customerName" type="text" />
          <span v-if="errors.customerName" class="error">{{ errors.customerName }}</span>
        </label>

        <label>
          作業の種類
          <select v-model="form.workType">
            <option value="" disabled>選択してください</option>
            <option v-for="workType in workTypes" :key="workType" :value="workType">
              {{ workType }}
            </option>
          </select>
          <span v-if="errors.workType" class="error">{{ errors.workType }}</span>
        </label>

        <label>
          期限
          <input v-model="form.dueDate" type="date" />
          <span v-if="errors.dueDate" class="error">{{ errors.dueDate }}</span>
        </label>

        <label>
          担当者
          <input v-model="form.assignee" type="text" />
          <span v-if="errors.assignee" class="error">{{ errors.assignee }}</span>
        </label>

        <label>
          ステータス
          <select v-model="form.status">
            <option v-for="(label, code) in statusLabels" :key="code" :value="code">
              {{ label }}
            </option>
          </select>
          <span v-if="errors.status" class="error">{{ errors.status }}</span>
        </label>

        <label>
          メモ
          <textarea v-model="form.memo" rows="3"></textarea>
          <span v-if="errors.memo" class="error">{{ errors.memo }}</span>
        </label>

        <div class="buttons">
          <button type="button" @click="save">保存</button>
          <button type="button" @click="closePanel">キャンセル</button>
          <button v-if="editingId !== null" type="button" class="danger" @click="remove">
            削除
          </button>
        </div>
      </aside>
    </div>
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

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.layout {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.list {
  flex: 1;
}

.panel {
  width: 320px;
  border: 1px solid #ccc;
  padding: 12px 16px;
}

.panel label {
  display: block;
  margin-bottom: 10px;
}

.panel input,
.panel select,
.panel textarea {
  display: block;
  width: 100%;
  margin-top: 4px;
}

.error {
  display: block;
  color: #c00;
  font-size: 0.9em;
  margin-top: 2px;
}

tbody tr {
  cursor: pointer;
}

tbody tr:hover {
  background: #f3f3f3;
}

tbody tr.selected {
  background: #e3f0ff;
}

.buttons {
  display: flex;
  gap: 8px;
}

.danger {
  margin-left: auto;
  color: #c00;
}

.filters {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 12px;
}

tbody tr.group-row {
  cursor: default;
}

tbody tr.group-row th {
  background: #e8eef7;
}

tbody tr.overdue td {
  color: #c00;
  font-weight: bold;
}
</style>
