package ru.plorum.reporter.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Visibility {

    ME(0, "Только для меня"),
    MY_GROUP(1, "Для моего отдела"),
    ALL(2, "Для всех"),
    GROUP(3, "Для отдела"),
    USERS(4, "Для пользователей");

    private final Integer value;

    private final String description;

}
