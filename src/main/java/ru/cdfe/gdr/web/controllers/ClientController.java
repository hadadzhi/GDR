package ru.cdfe.gdr.web.controllers;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.repositories.RecordsRepository;
import ru.cdfe.gdr.web.exceptions.NotFoundException;
import ru.cdfe.gdr.web.representations.RecordResource;

import java.util.Optional;

@RestController
@RequestMapping("records")
@Slf4j
public class ClientController {
	private final RecordsRepository repo;
	
	@Autowired
	public ClientController(RecordsRepository repo) {
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
}
