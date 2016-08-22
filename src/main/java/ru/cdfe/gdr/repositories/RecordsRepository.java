package ru.cdfe.gdr.repositories;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import ru.cdfe.gdr.domain.Record;

import java.util.Optional;

public interface RecordsRepository extends PagingAndSortingRepository<Record, String> {
	@Query("{'exforSubEntNumber': ?0}")
	Optional<Record> findByExfor(@Param("subEntNumber") String subEntNumber);
}
