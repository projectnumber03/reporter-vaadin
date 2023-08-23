package ru.plorum.reporter.service;

import jakarta.mail.MessagingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.plorum.reporter.model.Attachment;
import ru.plorum.reporter.model.User;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    @NonNull
    private final JavaMailSender mailSender;

    @Value("${system.domain}")
    private String domain;

    @Value("${spring.mail.from}")
    private String username;

    public void sendMail(final String emails, final String text, final String subject, final Attachment attachment) {
        try {
            final var message = mailSender.createMimeMessage();
            final var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(username);
            helper.setTo(emails.split(","));
            helper.setSubject(subject);
            helper.setText(text, true);
            if (Objects.nonNull(attachment)) {
                helper.addAttachment(attachment.name(), attachment.value());
            }
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Не удалось отправить email пользователям {}", emails);
        }
    }

    private void sendMail(final User user, final String text, final String subject) {
        sendMail(user.getEmail(), text, subject, null);
    }

    public void sendInactive(final User user, final String password) {
        final var subject = "Регистрационные данные в системе Reports";
        final var msg = String.format("Вы зарегистрированы в системе, Ваша учетная запись ожидает активации<br>Ваш логин: %s<br>Ваш пароль: %s<br><br><br><a href='http://%s'>%s</a>", user.getLogin(), password, domain, domain);
        sendMail(user, msg, subject);
    }

    public void sendActive(final User user) {
        final var subject = "Регистрационные данные в системе Reports";
        final var msg = String.format("Вы зарегистрированы в системе<br>Ваш логин: %s<br>Ваш пароль: %s<br><br><br><a href='http://%s'>%s</a>", user.getLogin(), user.getPassword(), domain, domain);
        sendMail(user, msg, subject);
    }

    public void sendInvite(final User user) {
        final var subject = "Учетная запись активирована";
        final var msg = String.format("Уважаемый (ая) %s, теперь Вы можете войти в систему Reports, используя учетные данные из предыдущего письма<br><br><br><a href='http://%s'>%s</a>", user.getName(), domain, domain);
        sendMail(user, msg, subject);
    }

    public void sendChange(final User user) {
        final var subject = "Новые учетные данные в системе Reports";
        final var msg = String.format("Уважаемый (ая) %s, Ваш пароль был изменен администратором системы. <br>Новые данные для входа: <br>Логин: %s <br>Пароль: %s<br><br><br><a href='http://%s'>%s</a>", user.getName(), user.getLogin(), user.getPassword(), domain, domain);
        sendMail(user, msg, subject);
    }

}
