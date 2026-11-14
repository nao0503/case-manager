// 期限の区切り
export type DueGroup = 'overdue' | 'thisWeek' | 'thisMonth' | 'later'

// 画面に出す名前
export const dueGroupLabels: Record<DueGroup, string> = {
  overdue: '期限切れ',
  thisWeek: '今週',
  thisMonth: '今月',
  later: '来月以降',
}

// Date → '2026-11-14' の形の文字にする（パソコンの日付のまま）
export function toDateString(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 期限が（'2026-11-20'）が today  から見てどの区切りか
export function getDueGroup(dueDate: string, today: Date): DueGroup {
  const todayString = toDateString(today)

  // 今週の日曜日（getDay()は日曜0・月曜1…土曜6）
  const sunday = new Date(today)
  sunday.setDate(today.getDate() + ((7 - today.getDay()) % 7))
  const endOfWeek = toDateString(sunday)

  // 今月の末日（来月0日 = 今月の最終日）
  const endOfMonth = toDateString(new Date(today.getFullYear(), today.getMonth() + 1, 0))

  if (dueDate < todayString) return 'overdue'
  if (dueDate <= endOfWeek) return 'thisWeek'
  if (dueDate <= endOfMonth) return 'thisMonth'
  return 'later'
}
