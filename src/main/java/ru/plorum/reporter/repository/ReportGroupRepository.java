package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.ReportGroup;

import java.util.Optional;
import java.util.UUID;

public interface ReportGroupRepository extends JpaRepository<ReportGroup, UUID> {
    Optional<ReportGroup> findReportGroupByName(final String name);

}
