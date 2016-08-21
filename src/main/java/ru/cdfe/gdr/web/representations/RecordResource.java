package ru.cdfe.gdr.web.representations;

import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;
import ru.cdfe.gdr.domain.*;
import ru.cdfe.gdr.web.controllers.ClientController;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Getter
@Relation(value = RecordResource.RELATION, collectionRelation = RecordResource.COLLECTION_RELATION)
public class RecordResource extends ResourceSupport {
	public static final String RELATION = "record";
	public static final String COLLECTION_RELATION = "records";

	private final String exforSubEntNumber;
	private final Quantity integratedCrossSection;
	private final Quantity firstMoment;
	private final Quantity energyCenter;
	private final Nucleus target;
	private final Nucleus product;
	private final Reaction reaction;

	private final List<DataPoint> sourceData;
	private final List<Approximation> approximations;

	public RecordResource(Record record) {
		this.exforSubEntNumber = record.getExforSubEntNumber();
		this.integratedCrossSection = record.getIntegratedCrossSection();
		this.firstMoment = record.getFirstMoment();
		this.energyCenter = record.getEnergyCenter();
		this.target = record.getTarget();
		this.product = record.getProduct();
		this.reaction = record.getReaction();

		this.sourceData = record.getSourceData();
		this.approximations = record.getApproximations();

		add(linkTo(methodOn(ClientController.class).getRecord(record.getId())).withSelfRel());
	}
}
