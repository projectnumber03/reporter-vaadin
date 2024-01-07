package ru.plorum.reporter.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class TariffInfo {

    @Value("${tariff.id}")
    private String tariffId;

    @Value("${tariff.name}")
    private String tariffName;

}
