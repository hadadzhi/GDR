package ru.cdfe.gdr.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Component
@Profile(OPERATOR)
@Slf4j
public class ExforServiceTest implements ApplicationRunner {
	private final ExforService exforService;
	
	@Autowired
	public ExforServiceTest(ExforService exforService) {
		this.exforService = exforService;
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		exforService.getData("M0040004", 0, 1, 2).forEach(p -> log.info(p.toString()));
		exforService.getReactions("L0028002").forEach(r -> log.info(r.toString()));
	}
}
