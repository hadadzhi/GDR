package ru.cdfe.gdr.controllers;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.cdfe.gdr.constants.Relations;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/")
public class HomeController {
    @RequestMapping(method = RequestMethod.GET)
    public ResourceSupport home() {
        final ResourceSupport home = new ResourceSupport();
        
        home.add(linkTo(methodOn(HomeController.class).home()).withSelfRel());
        
        home.add(linkTo(methodOn(ConsumerController.class).listRecords(null, null)).withRel(Relations.RECORD_COLLECTION));
        
        home.add(new Link(
            linkTo(methodOn(ConsumerController.class).findRecord("")).toUriComponentsBuilder().replaceQuery(null).toUriString(),
            Relations.RECORD
        ));
        
        return home;
    }
}
