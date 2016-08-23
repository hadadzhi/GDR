package ru.cdfe.gdr.controllers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.UriTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.cdfe.gdr.constants.Parameters;
import ru.cdfe.gdr.constants.Relations;

import static org.springframework.hateoas.TemplateVariable.VariableType.REQUEST_PARAM;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
public class HomeController {
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public ResourceSupport home() {
		final ResourceSupport home = new ResourceSupport();
		
		home.add(linkTo(methodOn(HomeController.class).home()).withSelfRel());
		
		home.add(new Link(
			new UriTemplate(linkTo(methodOn(ReadController.class).findAll(null, null)).toUriComponentsBuilder().toUriString())
				.with(Parameters.PAGE, REQUEST_PARAM).with(Parameters.SIZE, REQUEST_PARAM).with(Parameters.SORT, REQUEST_PARAM),
			Relations.RECORD_COLLECTION
		));
		
		home.add(new Link(
			new UriTemplate(linkTo(methodOn(ReadController.class).findRecord("")).toUriComponentsBuilder().replaceQuery(null).toUriString())
				.with(Parameters.ID, REQUEST_PARAM),
			Relations.RECORD
		));
		
		return home;
	}
}
