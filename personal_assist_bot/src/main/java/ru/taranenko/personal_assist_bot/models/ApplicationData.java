package ru.taranenko.personal_assist_bot.models;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author Dmitrii Taranenko
 */
@Data
@Component
public class ApplicationData {
    private String fullName;
    private String phoneNum;
    private String servNum;
}
