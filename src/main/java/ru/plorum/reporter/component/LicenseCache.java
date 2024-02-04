package ru.plorum.reporter.component;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.plorum.reporter.model.License;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Profile({"business", "corporative", "professional"})
public class LicenseCache implements ILicenseCache {

    private final Map<UUID, License> licenseMap = new HashMap<>();

    public void clear() {
        licenseMap.clear();
    }

    public void addAll(final Collection<License> licenses) {
        licenseMap.putAll(licenses.stream().collect(Collectors.toMap(License::getId, Function.identity())));
    }

    public Collection<License> getAll() {
        return licenseMap.values();
    }

    public Optional<License> getActive() {
        return licenseMap.values().stream().filter(License::isActive).findAny();
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
    public void schedule() {
        licenseMap.values().forEach(v -> v.setActive(!v.getFinishDate().isBefore(LocalDate.now())));
    }

}
