package Config;

public class Launcher {
    //Сколько попыток нужно для поиска кнопок - FindButtonAndPress
    {
        String attemptsAmount;
    }

    //Настройки для оповещения о событиях - EndIsNear
    //Ну, то есть, надо заменить проверку файлов на простое нажатие в этом лаунчере, где оно будет сохраняться
    {
        Boolean success;
        Boolean failure;

        Boolean report;
    }

    //Здесь каким то образом надо сделать кнопки открыть и придумать, где будет база данных
    //В которой будут храниться сообщения, посылающиеся в телеграм.
    //Опять же заменить текстовые файлы, на что-то более удобное - TelegramBotSender
    {
        String successMessages;
        String failureMessages;
        String reportMessages;
    }

    //Здесь тоже чет тип открыть, ну или просто поля, для ввода и хранения BOT_TOKEN и CHAT_ID
    //Сущности можешь чекнуть в том же TelegramBotSender
    {
        String bot_token;
        String chat_id;
    }

    //А здесь открыть readme, который можно читать, и тоже надо придумать как открывать, редактировать
    {
        String readme;
    }
}
