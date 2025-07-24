package ru.taranenko.personal_assist_bot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.taranenko.personal_assist_bot.models.ApplicationData;

import java.util.HashMap;
import java.util.Map;

import static ru.taranenko.personal_assist_bot.service.Commands.*;

/**
 * @author Dmitrii Taranenko
 */
@Component
public class PersonalAssistBot extends TelegramLongPollingBot {

    @Autowired
    private ApplicationData applicationData;

    private static final Logger LOG = LoggerFactory.getLogger(PersonalAssistBot.class);

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String SERVICES = "Услуги";
    private static final String SUBMIT = "Оставить заявку";

    private static final Map<Long, String> USER_STATES = new HashMap<>();
    public static final Map<String, String> DESCRIPTIONS = new HashMap<>(
            Map.of("tgBotsDevelopment", """
                            – Автоматизация заявок, рассылок, FAQ, квизов
                            – Воронки, формы, CRM-интеграции""",
                    "miniAppsDev", """
                            – Интерфейс с кнопками, формами, каталогами
                            – Подключение к API, базам данных, платёжным системам""",
                    "botSupport", """
                            – Поддержка существующих решений
                            – Рефакторинг, добавление новых функций
                            – Оптимизация скорости""",
                    "consulting", """
                            – Поможем спроектировать логику бота от А до Я под вашу задачу
                            – Оценим сложность, сроки, подскажем лучшие практики""")
    );

    public PersonalAssistBot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var message = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();
            switch (message) {
                case START -> sendCommandMessage(startCommand(chatId));
                case HELP -> sendCommandMessage(helpCommand(chatId));
                case SERVICES -> sendCommandMessage(sendServices(chatId));
                case SUBMIT -> sendCommandMessage(proposalToSubmitApp(chatId));
                default -> handleUserInput(chatId, message);
            }
        }
        if (update.hasCallbackQuery()) {
            var callBackQuery = update.getCallbackQuery();
            handleCallBackQuery(callBackQuery);
        }
    }

    @Override
    public String getBotUsername() {
        return "taran_pa_bot";
    }

    private void sendTextMessage(Long chatId, String text) {
        var sChatId = String.valueOf(chatId);
        var sendMessage = new SendMessage(sChatId, text);
        sendCommandMessage(sendMessage);
    }

    private void sendCommandMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void handleUserInput(Long chatId, String userInput) {
        if (USER_STATES.containsKey(chatId)) {
            String state = USER_STATES.get(chatId);
            submitApp(chatId, userInput, state);
        } else {
           sendCommandMessage(helpCommand(chatId));
        }
    }

    private void handleCallBackQuery(CallbackQuery callBackQuery) {
        var data = callBackQuery.getData();
        var chatId = callBackQuery.getFrom().getId();
        switch (data) {
            case ("services"):
                sendCommandMessage(sendServices(chatId));
                break;
            case ("tgBotsDevelopment"):
            case ("miniAppsDev"):
            case ("botSupport"):
            case ("consulting"):
                sendCommandMessage(sendDescription(chatId, data));
                break;
            case ("proposalToSubmitApp"):
                sendCommandMessage(proposalToSubmitApp(chatId));
                break;
            case ("ok"):
                askForName(chatId);
                break;
            case ("cancel"):
                sendTextMessage(chatId, "Ну и зря!");
                break;
            default:
                sendTextMessage(chatId, "Unexpected command");
                break;
        }
    }

    private void submitApp(Long chatId, String userInput, String state) {
        switch (state) {
            case "ask_name":
                applicationData.setFullName(userInput);
                USER_STATES.put(chatId, "ask_phone");
                askForPhone(chatId);
                break;

            case "ask_phone":
                applicationData.setPhoneNum(userInput);
                USER_STATES.put(chatId, "ask_service");
                askForService(chatId);
                break;

            case "ask_service":
                applicationData.setServNum(userInput);
                processRequest(chatId);
                USER_STATES.remove(chatId);
                break;

            default:
                sendTextMessage(chatId, "Неизвестное состояние заявки.");
                break;
        }
    }

    private void askForName(Long chatId) {
        USER_STATES.put(chatId, "ask_name");
        sendTextMessage(chatId, "Введи ФИО:");
    }

    private void askForPhone(Long chatId) {
        sendTextMessage(chatId, "Теперь введи свой номер телефона:");
    }

    private void askForService(Long chatId) {
        sendTextMessage(chatId, "Спасибо, теперь введи номер услуги:");
    }

    private void processRequest(Long chatId) {
        sendTextMessage(chatId, "Заявка принята! Твои данные: \n" +
                "ФИО: " + applicationData.getFullName() + "\n" +
                "Телефон: " + applicationData.getPhoneNum() + "\n" +
                "Номер услуги: " + applicationData.getServNum());

        // Здесь типо отправляем заявку в базу данных
    }
}