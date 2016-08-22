package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.constants.Parameters;
import ru.cdfe.gdr.constants.Profiles;
import ru.cdfe.gdr.constants.Relations;
import ru.cdfe.gdr.domain.Approximation;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.BadRequestException;
import ru.cdfe.gdr.exceptions.RecordNotFoundException;
import ru.cdfe.gdr.repositories.RecordsRepository;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@Slf4j
@RestController
@Profile(Profiles.OPERATOR)
public class RecordsOperatorController {
	private final RecordsRepository repo;
	private final Validator validator;
	
	@Autowired
	public RecordsOperatorController(RecordsRepository repo, Validator validator) {
		this.repo = repo;
		this.validator = validator;
	}
	
	@RequestMapping(path = Relations.RECORD_COLLECTION, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void save(@RequestBody Resource<Record> requestEntity) {
		final Record record = requestEntity.getContent();
		final Set<ConstraintViolation<Record>> constraintViolations = validator.validate(record);
		
		if (!constraintViolations.isEmpty()) {
			throw new BadRequestException(
				constraintViolations.stream()
					.map(v -> v.getPropertyPath() + " " + v.getMessage())
					.collect(joining(", "))
			);
		}
		
		repo.save(record);
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@RequestParam(Parameters.ID) String id) {
		if (!repo.exists(id)) {
			throw new RecordNotFoundException();
		}
		
		repo.delete(id);
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void replace(@RequestParam(Parameters.ID) String id, @RequestBody Resource<Record> request) {
		// TODO replace record
	}
	
	@RequestMapping(path = Relations.CREATE_RECORD, method = RequestMethod.POST)
	public Resource<Record> createRecord(@RequestBody Resource<Record> request) {
		return null; // TODO create record from exfor
	}
	
	@RequestMapping(path = Relations.CREATE_APPROXIMATION, method = RequestMethod.POST)
	public Resource<Approximation> createApproximation(@RequestBody Resource<Approximation> request) {
		return null; // TODO create approximation
	}
}
