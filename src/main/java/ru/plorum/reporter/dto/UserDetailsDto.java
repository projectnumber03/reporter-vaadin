package ru.plorum.reporter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetailsDto {

    @JsonProperty
    String login;

    @JsonProperty
    String password;

    @JsonProperty
    String systemId;

    @JsonProperty
    String tariffId;

    @JsonProperty
    List<LicenseDto> licenses;

}
