package ru.cdfe.gdr;

import com.mongodb.MongoClient;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.stereotype.Component;
import ru.cdfe.gdr.domain.DataPoint;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.core.query.Update.update;

@Component
@Slf4j
@Profile("experiments")
public class Experiments implements ApplicationRunner {
	@Override
	public void run(ApplicationArguments args) throws Exception {
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
		
		mongo.updateFirst(
			query(where("exfor").is("12345678")),
			update("description", "Modified data"),
			RecordEx.class
		);
		
		r = mongo.findById(r.getId(), RecordEx.class);
		log.info("Modified record: " + r);
		
		log.info("# of records: " + mongo.findAll(RecordEx.class).size());
		log.info("Deleting record: " + r);
		mongo.remove(r);
		log.info("# of records: " + mongo.findAll(RecordEx.class).size());
	}
	
	@Getter
	@ToString
	static abstract class DocumentSupport {
		@Id
		private final ObjectId id = ObjectId.get();
	}
	
	@Getter
	@Setter
	@Builder
	@ToString(callSuper = true)
	static final class RecordEx extends DocumentSupport {
		private final String exfor;
		private final String description;
		
		private List<DataPoint> data;
		private List<ApproximationEx> approximations;
	}
	
	@Data
	static final class ApproximationEx {
		private List<DataPoint> dataSubset;
	}
}
