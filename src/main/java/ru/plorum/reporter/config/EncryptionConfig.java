package ru.plorum.reporter.config;

import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Bean("connectionEncoder")
    public AES256TextEncryptor getConnectionEncoder(@Value("${jasypt.encryptor.password}") String key) {
        final AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(key);
        return encryptor;
    }

}
