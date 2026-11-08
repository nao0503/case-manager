package com.example.casemanager.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// アプリ全体のエラー係。Contorllerで起きた例外を受け取り、決まった形のJSONにして返す
@RestControllerAdvice
public class ApiExceptionHandler {
	
	// 入力チェック（@Valid）で引っかかったとき：400と、項目ごとのメッセージを返す
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Map<String, Object> handleValidation(MethodArgumentNotValidException e){
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
			// 1つの項目に複数エラーがあるときは、最初の1つだけ入れる)
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return Map.of("errors", errors);
	}
}
