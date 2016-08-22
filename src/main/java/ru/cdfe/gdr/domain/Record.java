package ru.cdfe.gdr.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.core.Relation;
import ru.cdfe.gdr.validation.ExforSubEntNumber;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import static ru.cdfe.gdr.Constants.RELATION_RECORD;
import static ru.cdfe.gdr.Constants.RELATION_RECORD_COLLECTION;

@Document
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Relation(value = RELATION_RECORD, collectionRelation = RELATION_RECORD_COLLECTION)
public class Record {
	@Id
	@JsonIgnore
	private String id;
	
	@Version
	@JsonIgnore
	private Long version;
	
	@Valid
	private List<DataPoint> sourceData;
	
	public List<DataPoint> getSourceData() {
		return Collections.unmodifiableList(sourceData);
	}
	
	@NotEmpty
	@Valid
	private List<Approximation> approximations;
	
	public List<Approximation> getApproximations() {
		return Collections.unmodifiableList(approximations);
	}
	
	@Indexed(unique = true)
	@ExforSubEntNumber
	private String exforSubEntNumber;
	
	@NotNull
	@Valid
	private Nucleus target;
	
	@NotNull
	@Valid
	private Nucleus product;
	
	@NotNull
	@Valid
	private Reaction reaction;
	
	@NotNull
	@Valid
	private Quantity integratedCrossSection;
	
	@NotNull
	@Valid
	private Quantity firstMoment;
	
	@NotNull
	@Valid
	private Quantity energyCenter;
}
