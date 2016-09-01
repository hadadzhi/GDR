package ru.cdfe.gdr.exfor;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cdfe.gdr.exfor.ExforDataRow;

public interface ExforDataRowRepository extends JpaRepository<ExforDataRow, String> {}
