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
		
		// Self
		home.add(linkTo(methodOn(HomeController.class).home()).withSelfRel());
		
		// All records
		home.add(new Link(
			new UriTemplate(linkTo(methodOn(RecordsController.class).findAll(null, null)).toUriComponentsBuilder().toUriString())
				.with("page", REQUEST_PARAM).with("size", REQUEST_PARAM).with("sort", REQUEST_PARAM),
			Relations.RECORD_COLLECTION
		));
		
		// One record
		home.add(new Link(
			new UriTemplate(
				linkTo(methodOn(RecordsController.class).findOne(null, null)).toUriComponentsBuilder().replaceQuery(null).toUriString()
			).with(Parameters.ID, REQUEST_PARAM).with(Parameters.SUBENT_NUMBER, REQUEST_PARAM),
			Relations.RECORD
		));
		
		// TODO Operator links
		if (operatorController.isPresent()) {
			home.add(new Link(
				new UriTemplate(
					linkTo(methodOn(RecordsOperatorController.class).createRecordFromExfor("")).toUriComponentsBuilder().replaceQuery(null).toUriString()
				).with(Parameters.SUBENT_NUMBER, REQUEST_PARAM),
				Relations.CREATE_RECORD_FROM_EXFOR
			));
			home.add(linkTo(methodOn(RecordsOperatorController.class).createApproximation(null)).withRel(Relations.CREATE_APPROXIMATION));
		}
		
		return home;
	}
}
