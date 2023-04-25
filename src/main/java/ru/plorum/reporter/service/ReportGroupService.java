package ru.plorum.reporter.service;

import com.vaadin.flow.component.Component;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.component.ReportGroupTabContent;
import ru.plorum.reporter.component.SecurityTabContent;
import ru.plorum.reporter.model.ReportGroup;
import ru.plorum.reporter.model.User;
import ru.plorum.reporter.repository.ReportGroupRepository;

import java.util.*;

import static ru.plorum.reporter.util.Constants.REPORT_GROUP;
import static ru.plorum.reporter.util.Constants.SECURITY;

@Service
@AllArgsConstructor
public class ReportGroupService {

    private final ReportGroupRepository repository;

    private final UserService userService;

    public ReportGroup findById(final UUID id) {
        return repository.findById(id).orElse(null);
    }

    public void save(final ReportGroup reportGroup) {
        if (Objects.isNull(reportGroup)) return;
        repository.save(reportGroup);
    }

    public void saveFromContent(final Map<String, Component> content) {
        final var reportGroupTabContent = (ReportGroupTabContent) content.get(REPORT_GROUP);
        final var reportGroup = new ReportGroup();
        reportGroup.setId(Optional.ofNullable(reportGroup.getId()).orElse(UUID.randomUUID()));
        reportGroup.setName(reportGroupTabContent.getNameField().getValue());
        reportGroup.setDescription(reportGroupTabContent.getDescriptionField().getValue());
        final var securityTabContent = (SecurityTabContent) content.get(SECURITY);
        reportGroup.setVisible(securityTabContent.getReportVisibilityRadioButtonGroup().getValue().getValue());
        final User authenticatedUser = userService.getAuthenticatedUser();
        switch (securityTabContent.getReportVisibilityRadioButtonGroup().getValue()) {
            case ME -> reportGroup.getPermittedUsers().add(authenticatedUser);
            case MY_GROUP ->
                    reportGroup.getPermittedUsers().addAll(userService.findByGroup(authenticatedUser.getGroup()));
            case ALL -> reportGroup.getPermittedUsers().addAll(userService.findAll());
            case GROUPS ->
                    reportGroup.getPermittedUsers().addAll(securityTabContent.getGroupSelect().getSelectedItems().stream().map(userService::findByGroup).flatMap(Collection::stream).toList());
            case USERS -> reportGroup.getPermittedUsers().addAll(securityTabContent.getUserSelect().getSelectedItems());
        }
        repository.save(reportGroup);
    }

    public List<ReportGroup> findMy() {
        final User user = userService.getAuthenticatedUser();
        if (user.isAdmin()) {
            return repository.findAll();
        }
        return repository.findAllByPermittedUsers_Id(user.getId());
    }

    public void create(final String detail) {
        final var reportGroup = new ReportGroup();
        reportGroup.setId(UUID.randomUUID());
        reportGroup.setName(detail);
        reportGroup.getPermittedUsers().add(userService.getAuthenticatedUser());
        repository.save(reportGroup);
    }

}
