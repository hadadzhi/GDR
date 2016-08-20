package ru.cdfe.gdr.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.cdfe.gdr.domain.Record;

import java.util.Optional;

public interface RecordsRepository extends PagingAndSortingRepository<Record, ObjectId> {
	Optional<Record> findByExforSubEntNumber(String subEntNumber);
}
