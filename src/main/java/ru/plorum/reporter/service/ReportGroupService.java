package ru.plorum.reporter.service;

import com.vaadin.flow.component.Component;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.model.ReportGroup;
import ru.plorum.reporter.repository.ReportGroupRepository;

import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ReportGroupService {

    private final ReportGroupRepository repository;

    public void save(final ReportGroup reportGroup) {
        if (Objects.isNull(reportGroup)) return;
        repository.save(reportGroup);
    }

    public void saveFromContent(final Map<String, Component> content) {

    }

}
