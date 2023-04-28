package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.Parameter;

import java.util.UUID;

public interface ParameterRepository extends JpaRepository<Parameter, UUID> {
}
