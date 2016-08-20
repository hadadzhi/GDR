package ru.cdfe.gdr;

import com.mongodb.MongoClient;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.cdfe.gdr.domain.*;
import ru.cdfe.gdr.repositories.RecordsRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Getter
@ToString
abstract class DocumentSupport {
	@Id
	private final ObjectId id = ObjectId.get();
}

@Getter
@Builder
@ToString(callSuper = true)
@Document
final class RecordEx extends DocumentSupport {
	private String exfor;
	private String description;
	
	private List<DataPoint> data;
	private List<ApproximationEx> approximations;
}

@Data
final class ApproximationEx {
	private List<DataPoint> dataSubset;
}

interface RecordsRepo extends CrudRepository<RecordEx, ObjectId> {
	Optional<RecordEx> findByExfor(String exfor);
}

@Component
@Slf4j
class Experiments implements ApplicationRunner {
	private final RecordsRepo repo;
	private final RecordsRepository records;
	private static final SecureRandom rnd = new SecureRandom();
	
	@Autowired
	Experiments(RecordsRepo repo, RecordsRepository records) {
		this.repo = repo;
		this.records = records;
	}
	
	@Override
	public void run(ApplicationArguments args) throws Exception {
		useMongoTemplate();
		useRepo();
		createTestData();
	}
	
	private void createTestData() {
		records.deleteAll();
		IntStream.range(0, 1000).parallel().forEach(i -> createAndSaveTestRecord());
	}
	
	public Record createAndSaveTestRecord() {
		List<DataPoint> source = new ArrayList<>();
		IntStream.range(0, 10).forEach(i -> source.add(
			new DataPoint(
				new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"),
				new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))));
		
		List<Approximation> approximations = new ArrayList<>();
		IntStream.range(0, 2).forEach(i -> {
			List<Curve> curves = new ArrayList<>();
			IntStream.range(0, 2).forEach(j -> {
				Curve curve = Curve.builder()
					.type(j % 2 == 0 ? "Gaussian" : "Lorentzian")
					.energyAtMaxCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
					.fullWidthAtHalfMaximum(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
					.maxCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))
					.build();
				
				curves.add(curve);
			});
			
			Approximation approximation = Approximation.builder()
				.chiSquaredUnweighted(rnd.nextDouble() * 100)
				.chiSquaredWeighted(rnd.nextDouble() * 100)
				.description("Sample data " + rnd.nextInt())
				.sourceData(source.stream().limit(source.size() / 2).collect(toList()))
				.curves(curves)
				.build();
			
			approximations.add(approximation);
		});
		
		Record record = Record.builder()
			.exforSubEntNumber(UUID.randomUUID().toString().replace("\\-", "").substring(0, 8))
			.energyCenter(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV"))
			.firstMoment(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "mb"))
			.integratedCrossSection(new Quantity(rnd.nextDouble(), rnd.nextDouble(), "MeV*mb"))
			.target(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1, "ZZ"))
			.product(new Nucleus(rnd.nextInt(100) + 1, rnd.nextInt(100) + 1, "ZZ"))
			.reaction(new Reaction("A", "B"))
			.sourceData(source)
			.approximations(approximations)
			.build();
		
		return records.save(record);
	}
	
	private void useMongoTemplate() {
		log.info("Using MongoTemplate");
		
		MongoTemplate mongo = new MongoTemplate(new SimpleMongoDbFactory(new MongoClient(), "experiments"));
		
		mongo.setWriteResultChecking(WriteResultChecking.LOG);
		mongo.dropCollection(RecordEx.class);
		
		RecordEx r = RecordEx.builder()
			.exfor("12345678")
			.description("Sample data")
			.build();
		log.info("Created record: " + r);
		
		mongo.insert(r);
		log.info("Inserted record: " + r);
		
		r = mongo.findById(r.getId(), RecordEx.class);
		log.info("Found record: " + r);
		
		mongo.updateFirst(query(where("exfor").is("12345678")), update("description", "Modified data"), RecordEx.class);
		r = mongo.findById(r.getId(), RecordEx.class);
		log.info("Modified record: " + r);
		
		log.info("# of Records: " + mongo.findAll(RecordEx.class).size());
		log.info("Deleting record: " + r);
		mongo.remove(r);
		log.info("# of Records: " + mongo.findAll(RecordEx.class).size());
	}
	
	private void useRepo() {
		log.info("Using repository");
		
		repo.deleteAll();
		
		RecordEx r = RecordEx.builder()
			.exfor("abcdefgh")
			.description("repo sample data")
			.build();
		log.info("Created RecordEx: " + r);
		
		r = repo.save(r);
		log.info("Saved RecordEx: " + r);
		
		r = repo.findOne(r.getId());
		log.info("Found RecordEx: " + r);
		
		log.info("Modifying by deleting the old record an replacing with a new one");
		repo.delete(r);
		r = repo.save(RecordEx.builder().description("modified " + r.getDescription()).exfor(r.getExfor()).build());
		log.info("Modified record: " + r);
		
		r = repo.findByExfor(r.getExfor()).orElseThrow(RuntimeException::new);
		log.info("Found record by exfor: " + r);
		
		log.info("# of Records: " + repo.count());
		log.info("Deleting RecordEx: " + r);
		repo.delete(r);
		log.info("# of Records: " + repo.count());
	}
}
