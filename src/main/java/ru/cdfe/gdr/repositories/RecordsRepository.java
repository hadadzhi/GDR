package ru.cdfe.gdr.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import ru.cdfe.gdr.domain.Record;

import java.util.Optional;

public interface RecordsRepository extends PagingAndSortingRepository<Record, String> {
	Optional<Record> findByExforSubEntNumber(String subEntNumber);
}
