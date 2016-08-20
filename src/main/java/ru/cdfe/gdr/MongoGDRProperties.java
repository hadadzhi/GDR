package ru.cdfe.gdr;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("gdr")
public class MongoGDRProperties {
	@NotBlank
	private String curieName;
	
	@NotBlank
	private String curieUrlTemplate;
}
