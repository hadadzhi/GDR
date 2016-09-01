package ru.cdfe.gdr.exfor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExforDataRowRepository extends JpaRepository<ExforDataRow, ExforDataKey> {
	List<ExforDataRow> findByKeySubEntNumber(String subEntNumber);
}
