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
import java.util.ArrayList;
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
	/**
	 * Exfor subent number or internal generated id
	 */
	@Id
	@JsonIgnore
	private String id;
	
	/**
	 * Enables optimistic concurrency control
	 */
	@Version
	@JsonIgnore
	private BigInteger version;
	
	@Valid
	private List<DataPoint> sourceData;
	
	public List<DataPoint> getSourceData() {
		if (sourceData == null) {
			sourceData = new ArrayList<>();
		}

		return sourceData;
	}
	
	@NotEmpty
	@Valid
	private List<Approximation> approximations;
	
	public List<Approximation> getApproximations() {
		if (approximations == null) {
			approximations = new ArrayList<>();
		}

		return approximations;
	}
	
	@NotEmpty
	@Valid
	private List<Reaction> reactions;
	
	public List<Reaction> getReactions() {
		if (reactions == null) {
			reactions = new ArrayList<>();
		}

		return reactions;
	}
	
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
