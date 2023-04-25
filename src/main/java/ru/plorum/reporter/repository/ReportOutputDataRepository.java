package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.ReportOutputData;

import java.util.UUID;

public interface ReportOutputDataRepository extends JpaRepository<ReportOutputData, UUID> {
}
