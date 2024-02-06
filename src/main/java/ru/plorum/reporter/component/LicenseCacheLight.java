package ru.plorum.reporter.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.plorum.reporter.model.License;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
@Profile({"free", "professional"})
public class LicenseCacheLight implements ILicenseCache {

    @Value("${tariff.id}")
    private String tariffId;

    @Override
    public void clear() {
    }

    @Override
    public void addAll(final Collection<License> licenses) {
    }

    public Collection<License> getAll() {
        return Collections.singleton(getFreeLicense());
    }

    public Optional<License> getActive() {
        return Optional.of(getFreeLicense());
    }

    private License getFreeLicense() {
        final var license = new License(UUID.randomUUID());
        license.setActive(true);
        license.setValidity(Integer.MAX_VALUE);
        license.setStartDate(LocalDate.now());
        license.setTariffId(tariffId);

        return license;
    }

}
