package ru.cdfe.gdr.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.core.Relation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static ru.cdfe.gdr.constants.Relations.RECORD;
import static ru.cdfe.gdr.constants.Relations.RECORD_COLLECTION;

@Document
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Relation(value = RECORD, collectionRelation = RECORD_COLLECTION)
public class Record {
	@Id
	@JsonIgnore
	private String id;
	
	@Version
	@JsonIgnore
	private BigInteger version;
	
	@Valid
	private List<DataPoint> sourceData;
	
	public List<DataPoint> getSourceData() {
		if (sourceData != null) {
			return Collections.unmodifiableList(sourceData);
		} else {
			return Collections.emptyList();
		}
	}
	
	@NotEmpty
	@Valid
	private List<Approximation> approximations;
	
	public List<Approximation> getApproximations() {
		if (approximations != null) {
			return Collections.unmodifiableList(approximations);
		} else {
			return Collections.emptyList();
		}
	}
	
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
