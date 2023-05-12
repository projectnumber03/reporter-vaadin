package ru.plorum.reporter.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query("select r from Report as r left join fetch r.permittedUsers where r.id = :id")
    Optional<Report> findByIdWithPermittedUsers(@Param("id") final UUID id);

    List<Report> findAllByAuthorOrLastEditorOrPermittedUsers_Id(final User author, final User lastEditor, @NonNull final UUID permittedUsers_id);

    List<Report> findAllBySchedulerTaskIsNotNull();

}
