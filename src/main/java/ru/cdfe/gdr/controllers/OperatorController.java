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
import ru.cdfe.gdr.services.RecordsService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
@Profile(Profiles.OPERATOR)
public class OperatorController {
	private final RecordsService recordsService;
	
	@Autowired
	public OperatorController(RecordsService recordsService) {
		this.recordsService = recordsService;
	}
	
	@RequestMapping(path = Relations.RECORD_COLLECTION, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void saveRecord(@RequestBody Resource<Record> requestEntity) {
		recordsService.save(requestEntity.getContent());
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.PUT)
	public Resource<Record> replaceRecord(@RequestParam(Parameters.ID) String id, @RequestBody Resource<Record> request) {
		return null; // TODO
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRecord(@RequestParam(Parameters.ID) String id) {
		// TODO
	}
	
	@RequestMapping(path = Relations.CREATE_RECORD, method = RequestMethod.POST)
	public Resource<Record> createRecord(@RequestBody Resource<Record> request) {
		return new Resource<>(
			recordsService.createRecord(request.getContent()),
			linkTo(methodOn(OperatorController.class).createApproximation(null)).withRel(Relations.CREATE_APPROXIMATION));
	}
	
	@RequestMapping(path = Relations.CREATE_APPROXIMATION, method = RequestMethod.POST)
	public Resource<Approximation> createApproximation(@RequestBody Resource<Approximation> request) {
		return new Resource<>(recordsService.createApproximation(request.getContent()));
	}
}
