package ru.cdfe.gdr.web.controllers;

import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.web.exceptions.NotFoundException;
import ru.cdfe.gdr.web.representations.RecordResource;

import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("records")
public class RecordsController {
	private final RecordsRepository repo;

	@Autowired
	public RecordsController(RecordsRepository repo) {
		this.repo = repo;
	}

	@RequestMapping(method = RequestMethod.GET)
	public Resources<RecordResource> getAll(Pageable pageable, PagedResourcesAssembler<Record> assembler) {
		return assembler.toResource(repo.findAll(pageable), RecordResource::new);
	}

	@RequestMapping(path = "{id}", method = RequestMethod.GET)
	public RecordResource getRecord(@PathVariable ObjectId id) {
		return new RecordResource(Optional.ofNullable(repo.findOne(id)).orElseThrow(NotFoundException::new));
	}

	@RequestMapping(path = "byExfor", method = RequestMethod.GET)
	public RecordResource getByExfor(@RequestParam String subEntNumber) {
		return new RecordResource(repo.findByExforSubEntNumber(subEntNumber).orElseThrow(NotFoundException::new));
	}

	// TODO search

	@RequestMapping(path = "test", method = RequestMethod.GET)
	public Record testRecord() {
		return repo.findAll().iterator().next();
	}

	@RequestMapping(path = "test", method = RequestMethod.POST)
	@Transactional(isolation = Isolation.READ_COMMITTED)
	@ResponseStatus(HttpStatus.CREATED)
	public HttpHeaders testCreate(@RequestBody Record newRecord) {
		newRecord = repo.save(newRecord);

		LoggerFactory.getLogger(getClass()).info("Saved id: " + newRecord.getId());

		HttpHeaders headers = new HttpHeaders();

		headers.setLocation(linkTo(methodOn(RecordsController.class).getRecord(newRecord.getId())).toUri());

		return headers;
	}
}
