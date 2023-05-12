package ru.plorum.reporter.model;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.plorum.reporter.util.Constants;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public record SchedulerCronExpression(LocalTime beginAt, Collection<Constants.Day> days, Integer interval) {

    @Override
    public String toString() {
        if (CollectionUtils.isEmpty(this.days())) {
            return String.format("0 %d/%d %d ? * *", this.beginAt().getMinute(), this.interval(), this.beginAt().getHour());
        }
        return String.format("0 %d %d ? * %s", this.beginAt().getMinute(), this.beginAt().getHour(), this.days().stream().map(Constants.Day::getShortName).collect(Collectors.joining(",")));
    }

    public static Optional<SchedulerCronExpression> parse(final String expression) {
        if (!StringUtils.hasText(expression)) return Optional.empty();
        try {
            final var splitExpression = expression.split(" ");
            final var minuteIntervalPart = splitExpression[1];
            final var interval = minuteIntervalPart.contains("/") ? Integer.parseInt(minuteIntervalPart.split("/")[1]) : null;
            final var minute = Integer.parseInt(minuteIntervalPart.split("/")[0]);
            final var hour = Integer.parseInt(splitExpression[2]);
            final var days = Arrays.stream(splitExpression[5].split(",")).map(Constants.Day::getByShortName).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());

            return Optional.of(new SchedulerCronExpression(LocalTime.of(hour, minute), days, interval));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
