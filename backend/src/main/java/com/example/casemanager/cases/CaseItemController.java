package com.example.casemanager.cases;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// 案件APIの受付係。ブラウザからのリクエストを受け取り、Repositoryに頼んで結果を返す
@RestController
@RequestMapping("/api/cases")
public class CaseItemController {
	private final CaseItemRepository repository;

	// DI:Springが用意したRepositoryを、ここで受け取る（自分でnewしない）
	public CaseItemController(CaseItemRepository repository) {
		this.repository = repository;
	}

	// 一覧を取る。status・assignee は任意（付いていれば絞り込む）。期限の早い順
	@GetMapping
	public List<CaseItem> list(
			@RequestParam(required = false) CaseStatus status,
			@RequestParam(required = false) String assignee) {
		Sort sort = Sort.by("dueDate", "id");
		
		if (status != null && assignee != null) {
			return repository.findByStatusAndAssignee(status, assignee, sort);
		}
		if (status != null) {
			return repository.findByStatus(status, sort);
		}
		if (assignee != null) {
			return repository.findByAssignee(assignee, sort);
		}
		return repository.findAll(Sort.by("dueDate", "id"));
	}

	// 登録：POST /api/cases
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CaseItem create(@Valid @RequestBody CaseItem caseItem) {
		return repository.save(caseItem);
	}

	// 1件取得：GET /api/cases/{id}
	@GetMapping("/{id}")
	public CaseItem get(@PathVariable Long id) {
		return findCaseOrThrow(id);
	}

	// 更新：PUT /api/cases/{id}
	@PutMapping("/{id}")
	public CaseItem update(@PathVariable Long id,@Valid @RequestBody CaseItem input) {
		CaseItem caseItem = findCaseOrThrow(id);

		caseItem.setCustomerName(input.getCustomerName());
		caseItem.setWorkType(input.getWorkType());
		caseItem.setDueDate(input.getDueDate());
		caseItem.setAssignee(input.getAssignee());
		caseItem.setStatus(input.getStatus());
		caseItem.setMemo(input.getMemo());

		return repository.save(caseItem);
	}

	// 削除：DELETE api/cases/{id}
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		CaseItem caseItem = findCaseOrThrow(id);

		repository.delete(caseItem);
	}

	// idの案件を書庫から出す。なければ404（get・update・deleteで共通）
	private CaseItem findCaseOrThrow(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "この案件はすでに削除されています"));

	}
}
