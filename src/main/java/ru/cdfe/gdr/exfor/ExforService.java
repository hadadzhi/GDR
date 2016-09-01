package ru.cdfe.gdr.exfor;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Service
@Profile(OPERATOR)
public class ExforService {
	// TODO The exfor service
}
