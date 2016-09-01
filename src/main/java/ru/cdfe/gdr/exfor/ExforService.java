package ru.cdfe.gdr.exfor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Service
@Profile(OPERATOR)
public class ExforService {
	// TODO The exfor service
}

@Component
@Slf4j
class TestExfor implements ApplicationRunner {
	private final ExforDataHeaderRowRepository headers;
	private final ExforDataRowRepository rows;
	
	@Autowired
	public TestExfor(ExforDataHeaderRowRepository headers, ExforDataRowRepository rows) {
		this.headers = headers;
		this.rows = rows;
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		headers.findByKeySubEntNumber("L0028002").forEach(h -> log.info(h.toString()));
		rows.findByKeySubEntNumber("L0028002").forEach(r -> log.info(r.toString()));
	}
}
