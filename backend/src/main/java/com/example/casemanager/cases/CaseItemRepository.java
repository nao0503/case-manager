package com.example.casemanager.cases;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

// 案件（CaseItem）を保存・取得する係。中身はSpringが自動で作成
public interface CaseItemRepository extends JpaRepository<CaseItem, Long> {

	// ステータスで絞り込む
	List<CaseItem> findByStatus(CaseStatus status, Sort sort);

	// 担当者で絞り込む
	List<CaseItem> findByAssignee(String assignee, Sort sort);

	// ステータスと担当者の両方で絞り込む
	List<CaseItem> findByStatusAndAssignee(CaseStatus status, String assignee, Sort sort);
}