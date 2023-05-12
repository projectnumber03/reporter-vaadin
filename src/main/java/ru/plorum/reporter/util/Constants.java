package ru.plorum.reporter.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

public class Constants {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final String NA = "н/д";
    public static final String PROFILE = "Профиль";
    public static final String USERS = "Пользователи";
    public static final String USER = "Пользователь";
    public static final String USER_GROUPS = "Группы";
    public static final String USER_GROUP = "Группа";
    public static final String CONNECTIONS = "Подключения";
    public static final String CONNECTION = "Подключение";
    public static final String COPY = "Копировать";
    public static final String MODULES = "Модули";
    public static final String SETTINGS = "Настройки системы";
    public static final String MY_REPORTS = "Мои отчёты";
    public static final String ALL_REPORTS = "Все отчёты";
    public static final String REPORT_GROUPS = "Группы отчётов";
    public static final String REPORT_GROUP = "Группа отчётов";
    public static final String REPORT = "Отчёт";
    public static final String SAVE = "Сохранить";
    public static final String REQUIRED_FIELD = "Поле обязательно к заполнению";
    public static final String DELETE = "Удалить";
    public static final String MAKE = "Сформировать";
    public static final String REPORT_QUERIES = "Запросы отчёта";
    public static final String REPORT_PARAMETERS = "Параметры отчёта";
    public static final String SCHEDULER = "Планировщик";
    public static final String SOURCES = "Источники";
    public static final String IMPORT_EXPORT = "Импорт/экспорт";
    public static final String SECURITY = "Безопасность";
    public static final String NAME = "Название";
    public static final String DESCRIPTION = "Описание";
    public static final String REPORT_OUTPUTS = "Сформированные отчёты";
    public static final String REPORT_OUTPUT = "Сформированный отчёт";
    public static final String OPEN = "Открыть";
    public static final String SUCCESS = "Успешно";
    public static final String DEFAULT_VALUE = "Значение по умолчанию";

    @Getter
    @AllArgsConstructor
    public enum Day {

        MONDAY("Понедельник", "MON"),
        TUESDAY("Вторник", "TUE"),
        WEDNESDAY("Среда", "WED"),
        THURSDAY("Четверг", "THU"),
        FRIDAY("Пятница", "FRI"),
        SATURDAY("Суббота", "SAT"),
        SUNDAY("Воскресенье", "SUN");

        private final String name;

        private final String shortName;

        public static Optional<Day> getByShortName(final String shortName) {
            if (!StringUtils.hasText(shortName)) return Optional.empty();
            return Arrays.stream(values()).filter(v -> shortName.equals(v.getShortName())).findAny();
        }

    }

}
