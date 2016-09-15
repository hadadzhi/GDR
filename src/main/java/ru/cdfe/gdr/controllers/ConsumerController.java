package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.cdfe.gdr.constants.Parameters;
import ru.cdfe.gdr.constants.Relations;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.NoSuchRecordException;
import ru.cdfe.gdr.repositories.RecordsRepository;

import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Slf4j
@RestController
public class ConsumerController {
	private final RecordsRepository records;
	
	@Autowired
	public ConsumerController(RecordsRepository records) {
		this.records = records;
	}
	
	@RequestMapping(path = Relations.RECORD_COLLECTION, method = RequestMethod.GET)
	public PagedResources<Resource<Record>> listRecords(Pageable pageable, PagedResourcesAssembler<Record> assembler) {
		return assembler.toResource(
			records.findAll(pageable),
			record -> new Resource<>(record, linkTo(methodOn(ConsumerController.class).findRecord(record.getId())).withSelfRel())
		);
	}
	
	@RequestMapping(path = Relations.RECORD, method = RequestMethod.GET)
	public Resource<Record> findRecord(@RequestParam(Parameters.ID) String id) {
		final Record record = Optional.ofNullable(records.findOne(id)).orElseThrow(NoSuchRecordException::new);
		return new Resource<>(record, linkTo(methodOn(ConsumerController.class).findRecord(record.getId())).withSelfRel());
	}
}
