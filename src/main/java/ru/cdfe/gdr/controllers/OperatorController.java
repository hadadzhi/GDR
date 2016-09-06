package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.GDRParameters;
import ru.cdfe.gdr.constants.Relations;
import ru.cdfe.gdr.domain.DataPoint;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.BadRequestException;
import ru.cdfe.gdr.exceptions.NoSuchRecordException;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.services.ExforService;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static ru.cdfe.gdr.constants.Parameters.*;
import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Slf4j
@RestController
@Profile(OPERATOR)
public class OperatorController {
	private final ExforService exforService;
	private final RecordsRepository records;
	private final Validator validator;
	
	@Autowired
	public OperatorController(ExforService exforService, RecordsRepository records, Validator validator) {
		this.exforService = exforService;
		this.records = records;
		this.validator = validator;
	}
	
	private <T> void validate(T object) {
		final Set<ConstraintViolation<T>> violations = validator.validate(object);
		
		if (!violations.isEmpty()) {
			final String message = violations.stream()
				.map(v -> StreamSupport.stream(v.getPropertyPath().spliterator(), false).reduce((r, e) -> e).orElse(null) + " " + v.getMessage())
				.collect(joining(", "));

			throw new BadRequestException(message);
		}
	}
	
	@RequestMapping(path = Relations.RECORD_COLLECTION, method = RequestMethod.POST)
	public ResponseEntity<?> postRecord(@RequestBody Resource<Record> requestEntity) {
		Record newRecord = requestEntity.getContent();
		
		validate(newRecord);
		
		newRecord = records.save(newRecord);
		
		return ResponseEntity.created(linkTo(methodOn(ConsumerController.class).findRecord(newRecord.getId())).toUri()).build();
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void putRecord(@RequestParam(ID) String id, @RequestBody Resource<Record> request) {
		final Record newRecord = request.getContent();
		
		validate(newRecord);
		
		newRecord.setId(id);
		
		final Record oldRecord = records.findOne(id);
		
		if (oldRecord != null) {
			newRecord.setVersion(oldRecord.getVersion());
		}
		
		records.save(newRecord);
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRecord(@RequestParam(ID) String id) {
		if (!records.exists(id)) {
			throw new NoSuchRecordException();
		}
		
		records.delete(id);
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.POST)
	public Resource<Record> createRecord(@RequestParam(ID) String subEntNumber,
	                                     @RequestParam(ENERGY_COLUMN) int energyColumn,
	                                     @RequestParam(CROSS_SECTION_COLUMN) int crossSectionColumn,
	                                     @RequestParam(CROSS_SECTION_ERROR_COLUMN) int crossSectionErrorColumn) {
		final List<DataPoint> sourceData = exforService.getData(subEntNumber, energyColumn, crossSectionColumn, crossSectionErrorColumn);
		final GDRParameters parameters = new GDRParameters(sourceData);
		
		return new Resource<>(
			Record.builder()
				.reactions(exforService.getReactions(subEntNumber))
				.sourceData(sourceData)
				.integratedCrossSection(parameters.getIntegratedCrossSection())
				.firstMoment(parameters.getFirstMoment())
				.energyCenter(parameters.getEnergyCenter())
				.build(),
			// TODO link to "create approximation" endpoint
			linkTo(methodOn(OperatorController.class).createRecord(subEntNumber, energyColumn, crossSectionColumn, crossSectionErrorColumn)).withSelfRel()
		);
	}
	
	// TODO "create approximation" endpoint
}
