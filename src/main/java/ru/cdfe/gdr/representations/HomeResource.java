package ru.cdfe.gdr.representations;

import org.springframework.hateoas.ResourceSupport;
import ru.cdfe.gdr.Constants;
import ru.cdfe.gdr.controllers.RecordsController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class HomeResource extends ResourceSupport {
	public HomeResource() {
		add(linkTo(methodOn(RecordsController.class).getAll(null, null)).withRel(Constants.RELATION_RECORD_COLLECTION));
	}
}
