package ru.cdfe.gdr.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import ru.cdfe.gdr.domain.Record;

public interface RecordsRepository extends PagingAndSortingRepository<Record, String> {}
