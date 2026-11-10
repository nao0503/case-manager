package com.example.casemanager.cases;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// 起動したときに、DBが空なら架空のサンプル案件を入れる係（テストのときは動かさない）
@Component
@Profile("!test")
public class SampleDataLoader implements CommandLineRunner {

	private final CaseItemRepository repository;

	public SampleDataLoader(CaseItemRepository repository) {
		this.repository = repository;
	}

	@Override
	public void run(String... args) {
		// すでに案件があるときは何もしない（起動のたびに増えないように）
		if (repository.count() > 0) {
			return;
		}

		// 期限は「今日から何日後か」で決める。いつ起動しても、期限切れ・今週・今月・来月以降が混ざる
		LocalDate today = LocalDate.now();
		add("サンプル商事", "決算申告", today.minusDays(5), "担当A", CaseStatus.IN_PROGRESS);
		add("テスト工業", "予定申告", today.minusDays(1), "担当B", CaseStatus.WAITING_DOCS);
		add("架空建設", "年末調整", today.minusDays(3), "担当A", CaseStatus.DONE);
		add("みほん食品", "決算申告", today.plusDays(2), "担当C", CaseStatus.WAITING_REVIEW);
		add("ためし運輸", "支払調書", today.plusDays(4), "担当B", CaseStatus.NOT_STARTED);
		add("サンプル商事", "償却資産", today.plusDays(12), "担当A", CaseStatus.NOT_STARTED);
		add("テスト工業", "納期特例", today.plusDays(15), "担当C", CaseStatus.DONE);
		add("れい電機", "決算申告", today.plusDays(35), "担当B", CaseStatus.NOT_STARTED);
		add("架空建設", "予定申告", today.plusDays(50), null, CaseStatus.NOT_STARTED);
		add("みほん食品", "年末調整", today.plusDays(70), "担当C", CaseStatus.WAITING_DOCS);
	}

	// 案件を1件作って保存する
	private void add(String customerName, String workType, LocalDate dueDate, String assignee, CaseStatus status) {
		CaseItem caseItem = new CaseItem();
		caseItem.setCustomerName(customerName);
		caseItem.setWorkType(workType);
		caseItem.setDueDate(dueDate);
		caseItem.setAssignee(assignee);
		caseItem.setStatus(status);
		repository.save(caseItem);
	}
}