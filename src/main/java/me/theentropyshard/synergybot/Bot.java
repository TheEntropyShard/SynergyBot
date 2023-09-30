package me.theentropyshard.synergybot;

import me.theentropyshard.synergybot.functions.FilterOperations;
import me.theentropyshard.synergybot.utils.ImageUtils;
import me.theentropyshard.synergybot.utils.PhotoMessageUtils;
import me.theentropyshard.synergybot.utils.RgbMaster;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class Bot extends TelegramLongPollingBot {
    private static final String IMAGES_DIR = "src/main/java/me/theentropyshard/synergybot/images/";

    public Bot(String botToken) {
        super(botToken);
    }

    // Экспериментируем с фильтрами класса FilterOperations
    public void processImage(String fileName) throws Exception {
        BufferedImage image = ImageUtils.getImage(fileName);
        RgbMaster rgbMaster = new RgbMaster(image);
        //rgbMaster.changeImage(FilterOperations::grayScale);
        rgbMaster.changeImage(FilterOperations::onlyGreen);
        //rgbMaster.changeImage(FilterOperations::onlyBlue);
        //rgbMaster.changeImage(FilterOperations::onlyRed);
        ImageUtils.saveImage(image, fileName);
    }

    private ReplyKeyboardMarkup getKeyboard(Class someClass) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        Method[] methods = someClass.getMethods();
        int columnCount = 3;
        int rowsCount = methods.length / columnCount + ((methods.length % columnCount == 0) ? 0 : 1);
        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
            KeyboardRow row = new KeyboardRow();
            for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                int index = rowIndex * columnCount + columnIndex;
                if (index >= methods.length) continue;
                Method method = methods[index];
                KeyboardButton button = new KeyboardButton(method.getName());
                row.add(button);
            }
            keyboardRows.add(row);
        }
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    private SendPhoto preparePhotoMessage(String localPath, String chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setReplyMarkup(this.getKeyboard(FilterOperations.class));
        sendPhoto.setChatId(chatId);
        InputFile inputFile = new InputFile();
        inputFile.setMedia(new java.io.File(localPath));
        sendPhoto.setPhoto(inputFile);
        return sendPhoto;
    }

    private List<File> getFilesByMessage(Message message) {
        List<PhotoSize> photoSizes = message.getPhoto();
        List<File> files = new ArrayList<>();
        for (PhotoSize photoSize : photoSizes) {
            String fileId = photoSize.getFileId();
            try {
                files.add(this.sendApiMethod(new GetFile(fileId)));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        return files;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        try {
            List<String> photoPaths = PhotoMessageUtils.savePhotos(this.getFilesByMessage(message), this.getBotToken());
            for (String path : photoPaths) {
                try {
                    this.processImage(path);
                    this.execute(this.preparePhotoMessage(path, chatId));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "SynergyBot";
    }
}
