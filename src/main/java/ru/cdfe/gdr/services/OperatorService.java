package ru.cdfe.gdr.services;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.cdfe.gdr.constants.Profiles;
import ru.cdfe.gdr.domain.Record;
import ru.cdfe.gdr.exceptions.NoSuchRecordException;
import ru.cdfe.gdr.repositories.RecordsRepository;

import javax.validation.Valid;
import java.util.Optional;

@Service
@Profile(Profiles.OPERATOR)
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
	
	public void putRecord(String id, @Valid Record newRecord) {
		final Optional<Record> oldRecord = Optional.ofNullable(records.findOne(id));
		
		if (oldRecord.isPresent()) {
			newRecord.setId(oldRecord.get().getId());
			newRecord.setVersion(oldRecord.get().getVersion());
		} else if (isExistingExforSubEnt(id)) {
			newRecord.setId(id);
		} else {
			throw new NoSuchRecordException();
		}
		
		records.save(newRecord);
	}
	
	private boolean isExistingExforSubEnt(String subEntNumber) {
		// TODO check for exfor subent existence
		return false;
	}
}
