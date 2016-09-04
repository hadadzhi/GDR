package ru.cdfe.gdr.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.NoSuchRecordException;
import ru.cdfe.gdr.repositories.RecordsRepository;

import javax.validation.Valid;
import java.util.Optional;

import static ru.cdfe.gdr.constants.Profiles.OPERATOR;

@Service
@Profile(OPERATOR)
@Validated
public class OperatorService {
	private final RecordsRepository records;
	
	public OperatorService(RecordsRepository records) {
		this.records = records;
	}
		
	public Record insertRecord(@Valid Record record) {
		return records.save(record);
	}
	
	public void deleteRecord(String id) {
		if (!records.exists(id)) {
			throw new NoSuchRecordException();
		}
		
		records.delete(id);
	}
	
	public void putRecord(@Valid Record newRecord) {
		final Optional<Record> oldRecord = Optional.ofNullable(records.findOne(newRecord.getId()));
		
		if (oldRecord.isPresent()) {
			newRecord.setVersion(oldRecord.get().getVersion());
		}
		
		records.save(newRecord);
	}
}
