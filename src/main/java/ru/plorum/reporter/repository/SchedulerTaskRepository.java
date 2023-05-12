package ru.plorum.reporter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.plorum.reporter.model.SchedulerTask;

import java.util.UUID;

public interface SchedulerTaskRepository extends JpaRepository<SchedulerTask, UUID> {
}
