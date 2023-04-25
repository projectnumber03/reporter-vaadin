package ru.plorum.reporter.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.ReportGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportGroupRepository extends JpaRepository<ReportGroup, UUID> {

    List<ReportGroup> findAllByPermittedUsers_Id(@NonNull final UUID permittedUsers_id);

    Optional<ReportGroup> findReportGroupByName(final String name);

}
