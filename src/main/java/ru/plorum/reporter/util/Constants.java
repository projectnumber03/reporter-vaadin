package ru.plorum.reporter.util;

import java.time.format.DateTimeFormatter;

public class Constants {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static final String TOPIC = "Тема";
    public static final String STATUS = "Статус";
    public static final String PRIORITY = "Приоритет";
    public static final String START_DATE = "Дата начала";
    public static final String DUE_DATE = "Срок завершения";
    public static final String SAVE = "Сохранить";
    public static final String REQUIRED_FIELD = "Поле обязательно к заполнению";
    public static final String CHANGES_SAVED = "Изменения сохранены";
    public static final String DELETE = "Удалить";
    public static final String EDIT = "Редактировать";

}
