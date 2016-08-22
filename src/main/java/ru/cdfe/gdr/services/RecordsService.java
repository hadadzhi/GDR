package ru.cdfe.gdr.services;

import org.springframework.stereotype.Service;
import ru.cdfe.gdr.domain.Approximation;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.InvalidRecordException;
import ru.cdfe.gdr.repositories.RecordsRepository;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@Service
public class RecordsService {
	private final RecordsRepository repo;
	private final Validator validator;
	
	public RecordsService(RecordsRepository repo, Validator validator) {
		this.repo = repo;
		this.validator = validator;
	}
	
	public Record createRecord(Record spec) {
		return null; // TODO
	}
	
	public Approximation createApproximation(Approximation spec) {
		return null; // TODO
	}
	
	public void save(Record record) {
		final Set<ConstraintViolation<Record>> constraintViolations = validator.validate(record);
		
		if (!constraintViolations.isEmpty()) {
			throw new InvalidRecordException(
				constraintViolations.stream()
					.map(v -> v.getPropertyPath() + " " + v.getMessage())
					.collect(joining(", "))
			);
		}
		
		repo.save(record);
	}
}
