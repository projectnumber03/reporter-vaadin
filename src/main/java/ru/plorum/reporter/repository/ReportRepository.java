package ru.plorum.reporter.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.Report;
import ru.plorum.reporter.model.User;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {


    List<Report> findAllByAuthorOrLastEditorOrPermittedUsers_Id(User author, User lastEditor, @NonNull UUID permittedUsers_id);

}
