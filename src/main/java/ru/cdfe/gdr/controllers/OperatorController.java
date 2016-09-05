package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.constants.Parameters;
import ru.cdfe.gdr.constants.Relations;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.NoSuchRecordException;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.services.ExforService;

import javax.validation.Validator;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
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
	
	@RequestMapping(path = Relations.RECORD_COLLECTION, method = RequestMethod.POST)
	public ResponseEntity<?> insertRecord(@RequestBody Resource<Record> requestEntity) {
		Record newRecord = requestEntity.getContent();
		
		validator.validate(newRecord);
		newRecord = records.save(newRecord);
		
		return ResponseEntity.created(linkTo(methodOn(ConsumerController.class).findRecord(newRecord.getId())).toUri()).build();
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void replaceRecord(@RequestParam(Parameters.ID) String id, @RequestBody Resource<Record> request) {
		final Record newRecord = request.getContent();
		
		validator.validate(newRecord);
		
		final Record oldRecord = records.findOne(id);
		
		if (oldRecord != null) {
			newRecord.setId(oldRecord.getId());
			newRecord.setVersion(oldRecord.getVersion());
		}
		
		records.save(newRecord);
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRecord(@RequestParam(Parameters.ID) String id) {
		if (!records.exists(id)) {
			throw new NoSuchRecordException();
		}
		
		records.delete(id);
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.POST)
	public Resource<Record> generateRecord(@RequestParam(Parameters.ID) String subEntNumber,
	                                       @RequestParam(Parameters.ENERGY_COLUMN) int energyColumn,
	                                       @RequestParam(Parameters.CROSS_SECTION_COLUMN) int crossSectionColumn,
	                                       @RequestParam(Parameters.CROSS_SECTION_ERROR_COLUMN) int crossSectionErrorColumn) {
		return new Resource<>(
			Record.builder()
				.sourceData(exforService.getData(subEntNumber, energyColumn, crossSectionColumn, crossSectionErrorColumn))
				.reactions(exforService.getReactions(subEntNumber))
				// TODO calculate GDR properties
				.build(),
			linkTo(methodOn(OperatorController.class).generateRecord(subEntNumber, energyColumn, crossSectionColumn, crossSectionErrorColumn)).withSelfRel()
		);
	}
	
	// TODO approximation endpoint
}
