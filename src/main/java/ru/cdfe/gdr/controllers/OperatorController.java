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
import ru.cdfe.gdr.services.ExforService;
import ru.cdfe.gdr.services.OperatorService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Slf4j
@RestController
@Profile(OPERATOR)
public class OperatorController {
	private final OperatorService operatorService;
	private final ExforService exforService;
	
	@Autowired
	public OperatorController(OperatorService operatorService, ExforService exforService) {
		this.operatorService = operatorService;
		this.exforService = exforService;
	}
	
	@RequestMapping(path = Relations.RECORD_COLLECTION, method = RequestMethod.POST)
	public ResponseEntity<?> insertRecord(@RequestBody Resource<Record> requestEntity) {
		final Record insertedRecord = operatorService.insertRecord(requestEntity.getContent());
		return ResponseEntity.created(linkTo(methodOn(ConsumerController.class).findRecord(insertedRecord.getId())).toUri()).build();
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void replaceRecord(@RequestParam(Parameters.ID) String id, @RequestBody Resource<Record> request) {
		request.getContent().setId(id);
		operatorService.putRecord(request.getContent());
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteRecord(@RequestParam(Parameters.ID) String id) {
		operatorService.deleteRecord(id);
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
