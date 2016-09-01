package ru.cdfe.gdr.exfor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExforDataHeaderRowRepository extends JpaRepository<ExforDataHeaderRow, ExforDataHeaderKey> {
	List<ExforDataHeaderRow> findByKeySubEntNumber(String subEntNumber);
}
