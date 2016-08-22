package ru.cdfe.gdr.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.controllers.exceptions.RecordNotFoundException;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.representations.RecordResource;

import java.util.Optional;

import static ru.cdfe.gdr.Constants.RELATION_RECORD_COLLECTION;

@RestController
@RequestMapping(RELATION_RECORD_COLLECTION)
@Slf4j
public class RecordsController {
	private final RecordsRepository repo;
	
	@Autowired
	public RecordsController(RecordsRepository repo) {
		this.repo = repo;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public PagedResources<RecordResource> findAll(Pageable pageable, PagedResourcesAssembler<Record> assembler) {
		return assembler.toResource(repo.findAll(pageable), RecordResource::new);
	}
	
	@RequestMapping(path = "{id}", method = RequestMethod.GET)
	public RecordResource findOne(@PathVariable String id) {
		return new RecordResource(Optional.ofNullable(repo.findOne(id)).orElseThrow(RecordNotFoundException::new));
	}
	
	@RequestMapping(path = "findByExfor", method = RequestMethod.GET)
	public RecordResource findByExfor(@RequestParam String subEntNumber) {
		return new RecordResource(repo.findByExfor(subEntNumber).orElseThrow(RecordNotFoundException::new));
	}
}
