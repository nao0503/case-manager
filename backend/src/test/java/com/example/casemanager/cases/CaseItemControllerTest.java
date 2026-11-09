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
	
	@Test
	void 削除すると204が返り_その後取得すると404になる() throws Exception{
		CaseItem saved = saveSample();
		
		mockMvc.perform(delete("/api/cases/{id}", saved.getId()))
		.andExpect(status().isNoContent());
		
		mockMvc.perform(get("/api/cases/{id}", saved.getId()))
		.andExpect(status().isNotFound());
		
	}

}