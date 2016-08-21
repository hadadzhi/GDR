package ru.cdfe.gdr.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.web.exceptions.ConflictException;

import javax.validation.Valid;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static ru.cdfe.gdr.MongoGDRApplication.PROFILE_OPERATOR;

@Profile(PROFILE_OPERATOR)
@RestController
@RequestMapping("operator")
@Slf4j
public class OperatorController {
	private final RecordsRepository repo;
	
	@Autowired
	public OperatorController(RecordsRepository repo) {
		this.repo = repo;
	}
	
	@RequestMapping(path = "test", method = RequestMethod.GET)
	public Record testRecord() {
		return repo.findAll().iterator().next();
	}
	
	@RequestMapping(path = "test", method = RequestMethod.POST)
	public ResponseEntity<Void> testCreate(@RequestBody @Valid Record newRecord) {
		repo.findByExforSubEntNumber(newRecord.getExforSubEntNumber()).ifPresent(record -> { throw new ConflictException(); });
		if (newRecord.getId() != null || newRecord.getVersion() != null) { throw new ConflictException(); }
		
		newRecord = repo.save(newRecord);
		log.info("Saved Record: " + newRecord);
		
		return ResponseEntity.created(linkTo(methodOn(ClientController.class).getRecord(newRecord.getId())).toUri()).build();
	}
}
