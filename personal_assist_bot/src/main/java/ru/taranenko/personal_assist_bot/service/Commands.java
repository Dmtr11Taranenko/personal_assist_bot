package ru.taranenko.personal_assist_bot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.taranenko.personal_assist_bot.utils.Controls;

import java.util.Collections;
import java.util.List;

import static ru.taranenko.personal_assist_bot.bot.PersonalAssistBot.DESCRIPTIONS;

/**
 * @author Dmitrii Taranenko
 */
public interface Commands {

    /**
     * Стартовая команда, которая отправляется клиенту после ввода команды "/start"
     *
     * @param chatId
     * @return message
     */
    static SendMessage startCommand(Long chatId) {
        var text = """
                Привет! Это твой личный ассистент.
                Я помогу тебе выбрать услугу и передам заявку нашей команде.
                Нажми «Услуги», чтобы посмотреть, что мы предлагаем, или "Оставить заявку", если уже определился.
                """;
        SendMessage message = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();

        message.setReplyMarkup(Controls.createDefaultKeyboard());

        return message;
    }

    /**
     * Сообщение с подсказкой, которое отправляется клиенту после ввода команды "/help"
     *
     * @param chatId
     * @return message
     */
    static SendMessage helpCommand(Long chatId) {
        var text = """
                - Воспользуйся командой "/start" для запуска бота;
                - Воспользуйся кнопками в нижнем меню для просмотра услуг или отправки заявки нашей команде.
                """;

        return SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
    }

    /**
     * Отправляет пользователю сообщение со списком услуг в виде кнопок при нажатии пользователем по кнопке "Услуги"
     *
     * @param chatId
     * @return message
     */
    static SendMessage sendServices(Long chatId) {
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

        return message;
    }

    /**
     * Предлагает пользователю отправить соглашение на обработку персональных данных перед заполнением заявки на услугу.
     * Подтягивается после нажатия на кнопку "Оставить заявку"
     *
     * @param chatId
     * @return message
     */
    static SendMessage proposalToSubmitApp(Long chatId) {
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

        return message;
    }

    /**
     * Сообщение, в котором отображается описание выбранной услуги
     *
     * @param chatId
     * @return message
     */
    static SendMessage sendDescription(Long chatId, String service) {
        String description = DESCRIPTIONS.get(service);
        String textFormatted = String.format("%s", description);

        return SendMessage.builder()
                .text(textFormatted)
                .chatId(chatId)
                .build();
    }
}
