package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.representations.ErrorResource;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static ru.cdfe.gdr.Constants.PROFILE_OPERATOR;
import static ru.cdfe.gdr.Constants.RELATION_RECORD_COLLECTION;

@Profile(PROFILE_OPERATOR)
@RestController
@RequestMapping(RELATION_RECORD_COLLECTION)
@Slf4j
public class RecordsOperatorController {
	private final RecordsRepository repo;
	private final Validator validator;
	
	@Autowired
	public RecordsOperatorController(RecordsRepository repo, Validator validator) {
		this.repo = repo;
		this.validator = validator;
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<?> save(@RequestBody Resource<Record> requestEntity) {
		final Record record = requestEntity.getContent();
		final Set<ConstraintViolation<Record>> constraintViolations = validator.validate(record);
		
		if (!constraintViolations.isEmpty()) {
			return ResponseEntity.badRequest().body(new ErrorResource(
				constraintViolations.stream()
					.map(v -> v.getPropertyPath() + " " + v.getMessage())
					.collect(joining(", "))
			));
		}
		
		repo.save(record);
		
		return ResponseEntity.noContent().build();
	}
	
	@RequestMapping(path = "{id}", method = RequestMethod.DELETE)
	public ResponseEntity<?> delete(@PathVariable String id) {
		if (repo.exists(id)) {
			repo.delete(id);
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
