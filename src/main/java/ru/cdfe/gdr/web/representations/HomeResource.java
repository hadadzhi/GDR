package ru.cdfe.gdr.web.representations;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import ru.cdfe.gdr.web.controllers.HomeController;
import ru.cdfe.gdr.web.controllers.ClientController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class HomeResource extends ResourceSupport {
	public static final String BY_EXFOR_RELATION = "byExfor";

	public HomeResource() {
		add(linkTo(methodOn(ClientController.class).getAll(null, null)).withRel(RecordResource.COLLECTION_RELATION));
		add(linkTo(methodOn(HomeController.class).getHome()).withSelfRel());
		add(new Link(
			linkTo(methodOn(ClientController.class).getByExfor("dummy exfor")).toUriComponentsBuilder().replaceQuery(null).toUriString(),
			BY_EXFOR_RELATION));
	}
}
