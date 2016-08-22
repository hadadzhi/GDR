package ru.cdfe.gdr.representations;

import org.springframework.hateoas.Resource;
import ru.cdfe.gdr.controllers.RecordsController;
import ru.cdfe.gdr.domain.Record;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class RecordResource extends Resource<Record> {
	public RecordResource(Record record) {
		super(record);
		add(linkTo(methodOn(RecordsController.class).findOne(record.getId())).withSelfRel());
	}
}
