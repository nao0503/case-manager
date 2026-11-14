package com.example.casemanager.cases;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// 案件APIのテスト。curlで手で確かめていたことを、プログラムで確かめる
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CaseItemControllerTest {

	// curlの代わりに、リクエストを送る道具
	@Autowired
	private MockMvc mockMvc;

	// テストの準備で、DBに直接案件を入れるために使う
	@Autowired
	private CaseItemRepository repository;

	// テスト用の案件を1件保存して返す（取得・更新・削除のテスト準備）
	private CaseItem saveSample() {
		CaseItem caseItem = new CaseItem();
		caseItem.setCustomerName("サンプル商事");
		caseItem.setWorkType("決算申告");
		caseItem.setDueDate(LocalDate.of(2026, 11, 30));
		caseItem.setStatus(CaseStatus.NOT_STARTED);
		return repository.save(caseItem);
	}

	@Test
	void 正しい内容なら登録できて201が返る() throws Exception {
		String json = """
				{
				  "customerName": "サンプル商事",
				  "workType": "決算申告",
				  "dueDate": "2026-11-30",
				  "status": "NOT_STARTED"
				}
				""";

		mockMvc.perform(post("/api/cases")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.customerName").value("サンプル商事"))
				.andExpect(jsonPath("$.createdAt").exists());
	}

	@Test
	void 顧客名が空で作業の種類が不正なら400と項目ごとのメッセージが返る() throws Exception {
		String json = """
				{
				  "customerName": "",
				  "workType": "おやつ",
				  "dueDate": "2026-11-30",
				  "status": "NOT_STARTED"
				}
				""";

		mockMvc.perform(post("/api/cases")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.customerName").value("顧客名は必須です"))
				.andExpect(jsonPath("$.errors.workType").value("作業の種類が正しくありません"));
	}

	@Test
	void 存在するidなら1件取得できる() throws Exception {
		CaseItem saved = saveSample();

		mockMvc.perform(get("/api/cases/{id}", saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerName").value("サンプル商事"));
	}

	@Test
	void 存在しないidなら404が返る() throws Exception {
		mockMvc.perform(get("/api/cases/{id}", 999999))
				.andExpect(status().isNotFound());
	}

	@Test
	void 更新すると期限とステータスが変わる() throws Exception {
		CaseItem saved = saveSample();
		String json = """
				{
				"customerName": "サンプル商事",
				"workType": "決算申告",
				"dueDate": "2026-12-15",
				"status": "IN_PROGRESS"
				}
				""";
		
		mockMvc.perform(put("/api/cases/{id}", saved.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.dueDate").value("2026-12-15"))
		.andExpect(jsonPath("$.status").value("IN_PROGRESS"));
	}
	
	private CaseItem saveCase(String customerName, LocalDate dueDate) {
		CaseItem caseItem = new CaseItem();
		caseItem.setCustomerName(customerName);
		caseItem.setWorkType("決算申告");
		caseItem.setDueDate(dueDate);
		caseItem.setStatus(CaseStatus.NOT_STARTED);
		return repository.save(caseItem);
	}
	
	// 顧客名・担当者・ステータス・期限を指定して保存する
	private CaseItem saveCase(String customerName, String assignee, CaseStatus status, LocalDate dueDate) {
		CaseItem caseItem = new CaseItem();
		caseItem.setCustomerName(customerName);
		caseItem.setWorkType("決算申告");
		caseItem.setAssignee(assignee);
		caseItem.setStatus(status);
		caseItem.setDueDate(dueDate);
		return repository.save(caseItem);
	}
	
	// 絞り込みテスト用に、5件の案件を保存する（先に全件削除する）
	private void saveFilterSamples() {
		repository.deleteAll();
		saveCase("サンプル商事", "担当A", CaseStatus.IN_PROGRESS, LocalDate.of(2026, 11,10));
		saveCase("架空建設", "担当A", CaseStatus.DONE, LocalDate.of(2026, 11,15));
		saveCase("テスト工業", "担当B", CaseStatus.IN_PROGRESS, LocalDate.of(2026, 11,20));
		saveCase("空想工務店", "担当B", CaseStatus.NOT_STARTED, LocalDate.of(2026, 11,25));
		saveCase("仮設商店", "担当B", CaseStatus.IN_PROGRESS, LocalDate.of(2026, 11,30));
	}
	
	@Test
	void 期限がばらばらでも一覧は期限の早い順に返る() throws Exception {
		// サンプルデータを全て削除する
		repository.deleteAll();
		// 期限がバラバラのサンプルデータを保存
		saveCase("サンプル商事", LocalDate.of(2026, 12, 20));
		saveCase("テスト工業", LocalDate.of(2026, 11, 15));
		saveCase("架空建設", LocalDate.of(2026, 12, 1));
		// 一覧を取得する
		mockMvc.perform(get("/api/cases"))
		// ステータスが200、件数が3件、顧客名の順番を確認
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()").value(3))
		.andExpect(jsonPath("$[0].customerName").value("テスト工業"))
		.andExpect(jsonPath("$[1].customerName").value("架空建設"))
		.andExpect(jsonPath("$[2].customerName").value("サンプル商事"));
	}
	
	@Test
	void ステータスを指定すると_そのステータスの案件だけが期限順に返る() throws Exception {
		saveFilterSamples();
		mockMvc.perform(get("/api/cases?status=IN_PROGRESS"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()").value(3))
		.andExpect(jsonPath("$[0].customerName").value("サンプル商事"))
		.andExpect(jsonPath("$[1].customerName").value("テスト工業"))
		.andExpect(jsonPath("$[2].customerName").value("仮設商店"));
	}
	
	@Test
	void 担当者を指定すると_その担当者の案件だけが期限順に返る() throws Exception {
		saveFilterSamples();
		mockMvc.perform(get("/api/cases?assignee=担当A"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()").value(2))
		.andExpect(jsonPath("$[0].customerName").value("サンプル商事"))
		.andExpect(jsonPath("$[1].customerName").value("架空建設"));
	}
	
	@Test
	void ステータスと担当者の両方を指定すると_両方に一致する案件だけが返る() throws Exception {
		saveFilterSamples();
		mockMvc.perform(get("/api/cases?status=IN_PROGRESS&assignee=担当B"))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.length()").value(2))
		.andExpect(jsonPath("$[0].customerName").value("テスト工業"))
		.andExpect(jsonPath("$[1].customerName").value("仮設商店"));
	}
	
	@Test
	void 存在しないステータスを指定すると400が返る() throws Exception {
		mockMvc.perform(get("/api/cases?status=OYATSU"))
		.andExpect(status().isBadRequest());
	}
	
	@Test
	void 削除すると204が返り_その後取得すると404になる() throws Exception {
		CaseItem saved = saveSample();
		
		mockMvc.perform(delete("/api/cases/{id}", saved.getId()))
		.andExpect(status().isNoContent());
		
		mockMvc.perform(get("/api/cases/{id}", saved.getId()))
		.andExpect(status().isNotFound());
	}
	
	@Test
	void 顧客名が101文字なら400と文字数のメッセージが返る() throws Exception {
		String json = """
				{
				  "customerName": "%s",
				  "workType": "決算申告",
				  "dueDate": "2026-11-30",
				  "status": "NOT_STARTED"
				}
				""".formatted("あ".repeat(101));

		mockMvc.perform(post("/api/cases")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.customerName").value("顧客名は100文字以内で入力してください"));
	}
	
	@Test
	void 顧客名100文字なら登録できて201が返る() throws Exception {
		String json = """
				{
				  "customerName": "%s",
				  "workType": "決算申告",
				  "dueDate": "2026-11-30",
				  "status": "NOT_STARTED"
				}
				""".formatted("あ".repeat(100));

		mockMvc.perform(post("/api/cases")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerName").value("あ".repeat(100)));
	}
}