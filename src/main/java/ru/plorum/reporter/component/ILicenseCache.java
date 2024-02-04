package ru.plorum.reporter.component;

import ru.plorum.reporter.model.License;

import java.util.Collection;
import java.util.Optional;

public interface ILicenseCache {

    void clear();

    void addAll(final Collection<License> licenses);

    Collection<License> getAll();

    Optional<License> getActive();

}
