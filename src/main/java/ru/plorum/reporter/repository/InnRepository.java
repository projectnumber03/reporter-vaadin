package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.Inn;

public interface InnRepository extends JpaRepository<Inn, String> {
}
