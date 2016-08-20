package ru.cdfe.gdr.web.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.cdfe.gdr.web.representations.HomeResource;

@RestController
@RequestMapping("/")
public class HomeController {
	@RequestMapping(method = RequestMethod.GET)
	public HomeResource getHome() {
		return new HomeResource();
	}
}
