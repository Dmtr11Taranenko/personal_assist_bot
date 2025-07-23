package ru.taranenko.personal_assist_bot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.taranenko.personal_assist_bot.bot.PersonalAssistBot;

/**
 * @author Dmitrii Taranenko
 */
@Configuration
public class PersonalAssistBotConfiguration {

    @Bean
    public TelegramBotsApi telegramBotsApi(PersonalAssistBot personalAssistBot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(personalAssistBot);
        return api;
    }
}
