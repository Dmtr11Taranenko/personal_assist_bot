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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.taranenko.personal_assist_bot.models.ApplicationData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<String, String> DESCRIPTIONS = new HashMap<>(
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
                case START -> startCommand(chatId);
                case HELP -> helpCommand(chatId);
                case SERVICES -> sendServices(chatId);
                case SUBMIT -> proposalToSubmitApp(chatId);
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

    private void sendMessage(Long chatId, String text) {
        var sChatId = String.valueOf(chatId);
        var sendMessage = new SendMessage(sChatId, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void startCommand(Long chatId) {
        var text = """
                Привет! Это твой личный ассистент.
                Я помогу тебе выбрать услугу и передам заявку нашей команде.
                Нажми «Услуги», чтобы посмотреть, что мы предлагаем, или "Оставить заявку", если уже определился.
                """;
        SendMessage message = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();

        message.setReplyMarkup(createDefaultKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void helpCommand(Long chatId) {
        var text = """
                - Воспользуйся командой "/start" для запуска бота;
                - Воспользуйся кнопками в нижнем меню для просмотра услуг или отправки заявки нашей команде.
                """;
        SendMessage message = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void handleCallBackQuery(CallbackQuery callBackQuery) {
        var data = callBackQuery.getData();
        var chatId = callBackQuery.getFrom().getId();
        switch (data) {
            case ("services"):
                sendServices(chatId);
                break;
            case ("tgBotsDevelopment"):
            case ("miniAppsDev"):
            case ("botSupport"):
            case ("consulting"):
                sendDescription(chatId, data);
                break;
            case ("proposalToSubmitApp"):
                proposalToSubmitApp(chatId);
                break;
            case ("ok"):
                askForName(chatId);
                break;
            case ("cancel"):
                sendMessage(chatId, "Ну и зря!");
                break;
            default:
                sendMessage(chatId, "Unexpected command");
                break;
        }
    }

    private void handleUserInput(Long chatId, String userInput) {
        if (USER_STATES.containsKey(chatId)) {
            String state = USER_STATES.get(chatId);
            submitApp(chatId, userInput, state);
        } else {
            sendMessage(chatId, "Чтобы начать, нажми кнопку 'Оставить заявку'");
        }
    }

    private void sendServices(Long chatId) {
        var text = """
                Нажми на интересующую тебя услугу, чтобы увидеть ее подробное описание.
                """;
        SendMessage message = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();

        var tgBotsDevelopmentButton = InlineKeyboardButton.builder()
                .text("1\uFE0F⃣ Разработка Telegram-ботов под ключ")
                .callbackData("tgBotsDevelopment")
                .build();
        var miniAppsDevButton = InlineKeyboardButton.builder()
                .text("2\uFE0F⃣ Создание Mini Apps (встроенных приложений в Telegram)")
                .callbackData("miniAppsDev")
                .build();
        var botSupportButton = InlineKeyboardButton.builder()
                .text("3\uFE0F⃣ Сопровождение и доработка ботов")
                .callbackData("botSupport")
                .build();
        var consultingButton = InlineKeyboardButton.builder()
                .text("4\uFE0F⃣ Консультации и проектирование")
                .callbackData("consulting")
                .build();

        List<List<InlineKeyboardButton>> keyboardButtons = List.of(
                Collections.singletonList(tgBotsDevelopmentButton),
                Collections.singletonList(miniAppsDevButton),
                Collections.singletonList(botSupportButton),
                Collections.singletonList(consultingButton)
        );
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardButtons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void sendDescription(Long chatId, String service) {
        String description = DESCRIPTIONS.get(service);
        String text = """
                В данную услугу входит:
                %s
                """;
        String textFormatted = String.format(text, description);
        SendMessage message = SendMessage.builder()
                .text(textFormatted)
                .chatId(chatId)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }

    private void proposalToSubmitApp(Long chatId) {
        var text = """
                Для отправки заявки необходимо предоставить твое ФИО, номер телефона и номер услуги
                
                Согласен?
                """;
        SendMessage message = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();

        var okButton = InlineKeyboardButton.builder()
                .text("✅ Да")
                .callbackData("ok")
                .build();
        var cancelButton = InlineKeyboardButton.builder()
                .text("❌ Нет")
                .callbackData("cancel")
                .build();
        List<List<InlineKeyboardButton>> keyboardButtons = List.of(
                Collections.singletonList(okButton),
                Collections.singletonList(cancelButton)
        );
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardButtons);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
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
                sendMessage(chatId, "Неизвестное состояние заявки.");
                break;
        }
    }

    private void askForName(Long chatId) {
        USER_STATES.put(chatId, "ask_name");
        sendMessage(chatId, "Введи ФИО:");
    }

    private void askForPhone(Long chatId) {
        sendMessage(chatId, "Теперь введи свой номер телефона:");
    }

    private void askForService(Long chatId) {
        sendMessage(chatId, "Спасибо, теперь введи номер услуги:");
    }

    private void processRequest(Long chatId) {
        sendMessage(chatId, "Заявка принята! Твои данные: \n" +
                "ФИО: " + applicationData.getFullName() + "\n" +
                "Телефон: " + applicationData.getPhoneNum() + "\n" +
                "Номер услуги: " + applicationData.getServNum());

        // Здесь типо отправляем заявку в базу данных
    }

    private ReplyKeyboardMarkup createDefaultKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Услуги"));
        row1.add(new KeyboardButton("Оставить заявку"));

        keyboardMarkup.setKeyboard(List.of(row1));

        return keyboardMarkup;
    }
}
