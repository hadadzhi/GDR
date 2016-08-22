package ru.cdfe.gdr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.UriTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.cdfe.gdr.constants.Parameters;
import ru.cdfe.gdr.constants.Relations;

import java.util.Optional;

import static org.springframework.hateoas.TemplateVariable.VariableType.REQUEST_PARAM;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class HomeController {
	private final Optional<RecordsOperatorController> operatorController;
	
	@Autowired
	public HomeController(Optional<RecordsOperatorController> operatorController) {
		this.operatorController = operatorController;
	}
	
	@RequestMapping(path =  "/", method = RequestMethod.GET)
	public ResourceSupport home() {
		final ResourceSupport home = new ResourceSupport();
		
		home.add(linkTo(methodOn(HomeController.class).home()).withSelfRel());
		
		home.add(new Link(
			new UriTemplate(linkTo(methodOn(RecordsController.class).findAll(null, null)).toUriComponentsBuilder().toUriString())
				.with(Parameters.PAGE, REQUEST_PARAM).with(Parameters.SIZE, REQUEST_PARAM).with(Parameters.SORT, REQUEST_PARAM),
			Relations.RECORD_COLLECTION
		));
		
		home.add(new Link(
			new UriTemplate(linkTo(methodOn(RecordsController.class).findOne(null, null)).toUriComponentsBuilder().toUriString())
				.with(Parameters.ID, REQUEST_PARAM).with(Parameters.SUBENT_NUMBER, REQUEST_PARAM),
			Relations.RECORD
		));
		
		if (operatorController.isPresent()) {
			home.add(linkTo(methodOn(RecordsOperatorController.class).createRecord(null)).withRel(Relations.CREATE_RECORD));
			home.add(linkTo(methodOn(RecordsOperatorController.class).createApproximation(null)).withRel(Relations.CREATE_APPROXIMATION));
		}
		
		return home;
	}
}
