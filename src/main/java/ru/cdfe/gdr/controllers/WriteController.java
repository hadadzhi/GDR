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
import ru.cdfe.gdr.services.OperatorService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Slf4j
@RestController
@Profile(OPERATOR)
public class WriteController {
	private final OperatorService operatorService;
	
	@Autowired
	public WriteController(OperatorService operatorService) {
		this.operatorService = operatorService;
	}
	
	@RequestMapping(path = Relations.RECORD_COLLECTION, method = RequestMethod.POST)
	public ResponseEntity<?> insertRecord(@RequestBody Resource<Record> requestEntity) {
		final Record insertedRecord = operatorService.insertRecord(requestEntity.getContent());
		return ResponseEntity.created(linkTo(methodOn(ReadController.class).findRecord(insertedRecord.getId())).toUri()).build();
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void replaceRecord(@RequestParam(Parameters.ID) String id, @RequestBody Resource<Record> request) {
		operatorService.putRecord(id, request.getContent());
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRecord(@RequestParam(Parameters.ID) String id) {
		operatorService.deleteRecord(id);
	}
}
