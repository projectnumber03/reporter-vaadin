package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.ReportOutput;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportOutputRepository extends JpaRepository<ReportOutput, UUID> {

    @Query("select ro from ReportOutput as ro join fetch ro.data where ro.id = :id")
    Optional<ReportOutput> findByIdWithData(@Param("id") final UUID id);

    List<ReportOutput> findAllByReportOrderByCreatedAtDesc(final Report report);

}
