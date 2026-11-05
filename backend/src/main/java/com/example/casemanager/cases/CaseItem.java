package com.example.casemanager.cases;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity // このテーブルはDBのテーブルと対応
@Table(name = "cases") // テーブル名はcases
public class CaseItem {
	
	@Id // 主キー
	@GeneratedValue(strategy = GenerationType.IDENTITY) // DBが自動採番
	private Long id;
	
	@Column(nullable = false, length = 100) // 必須、100文字まで
	private String customerName;
	
	@Column(nullable = false, length = 30) // 必須、30文字まで
	private String workType;
	
	@Column(nullable = false) // 必須
	private LocalDate dueDate; // 日付だけ（時刻なし）
	
	@Column(length = 50) // 任意、50文字まで
	private String assignee;
	
	@Enumerated(EnumType.STRING) // enumを"IN_PROGRESS"のような文字で保存
	@Column(nullable = false, length = 20) // 必須、20文字
	private CaseStatus status;
	
	@Column(length = 500) // 500文字
	private String memo;
	
	@Column(nullable = false, updatable = false) // 作成日時は、後から変更不可
	private LocalDateTime createdAt;
	
	@Column(nullable = false) // 必須
	private LocalDateTime updatedAt;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getWorkType() {
		return workType;
	}

	public void setWorkType(String workType) {
		this.workType = workType;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public CaseStatus getStatus() {
		return status;
	}

	public void setStatus(CaseStatus status) {
		this.status = status;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	@PrePersist // 初めて保存する直前に、自動で呼ばれる
	void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = createdAt;
	}

	@PreUpdate // 更新する直前に、自動で呼ばれる
	void onUpdate(){
		updatedAt = LocalDateTime.now();
	}
}
