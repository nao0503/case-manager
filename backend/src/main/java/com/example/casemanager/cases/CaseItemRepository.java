package com.example.casemanager.cases;

import org.springframework.data.jpa.repository.JpaRepository;

// 案件（CaseItem）を保存・取得する係。中身はSpringが自動で作成
public interface CaseItemRepository extends JpaRepository<CaseItem, Long> {

}
