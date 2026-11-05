package com.example.casemanager.cases;

// 案件のステータス（DBにはこの英語のコードで保存する）
public enum CaseStatus {
	NOT_STARTED, // 未着手
	WAITING_DOCS, // 資料待ち
	IN_PROGRESS, // 作業中
	WAITING_REVIEW, // 確認待ち
	DONE // 完了
}
