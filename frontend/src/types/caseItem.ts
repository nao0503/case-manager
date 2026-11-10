// 案件のステータス（APIと同じ英語のコード）
export type CaseStatus = 'NOT_STARTED' | 'WAITING_DOCS' | 'IN_PROGRESS' | 'WAITING_REVIEW' | 'DONE'

// APIから返ってくる案件の形（Javaの CaseItem に合わせる）
export interface CaseItem {
  id: number
  customerName: string
  workType: string
  dueDate: string
  assignee: string | null
  status: CaseStatus
  memo: string | null
  createdAt: string
  updatedAt: string
}

// ステータスの表示名（画面に出すときに使う）
export const statusLabels: Record<CaseStatus, string> = {
  NOT_STARTED: '未着手',
  WAITING_DOCS: '資料待ち',
  IN_PROGRESS: '作業中',
  WAITING_REVIEW: '確認待ち',
  DONE: '完了',
}
