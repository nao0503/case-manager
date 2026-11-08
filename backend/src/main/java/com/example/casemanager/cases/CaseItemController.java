package com.example.casemanager.cases;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// 案件APIの受付係。ブラウザからのリクエストを受け取り、Repositoryに頼んで結果を返す
@RestController
@RequestMapping("/api/cases")
public class CaseItemController {
	private final CaseItemRepository repository;
	
	// DI:Springが用意したRepositoryを、ここで受け取る（自分でnewしない）
	public CaseItemController(CaseItemRepository repository) {
		this.repository = repository;
	}
	
	// 一覧取得：GET /api/cases
	@GetMapping
	public List<CaseItem> list(){
		return repository.findAll();
	}
	
	// 登録：POST /api/cases
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CaseItem create(@RequestBody CaseItem caseItem) {
		return repository.save(caseItem);
	}
}
