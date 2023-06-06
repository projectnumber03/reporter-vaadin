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

    ReportOutput findFirstByReportOrderByCreatedAtDesc(final Report report);

    @Query(value = "select max(count) from (select cast(CREATED_AT as date), count(*) as count from REPORT_OUTPUT group by cast(CREATED_AT as date))", nativeQuery = true)
    Long getMaxByDate();

}
