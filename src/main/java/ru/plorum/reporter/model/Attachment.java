package ru.plorum.reporter.model;

import jakarta.mail.util.ByteArrayDataSource;

public record Attachment(String name, ByteArrayDataSource value) {
}
