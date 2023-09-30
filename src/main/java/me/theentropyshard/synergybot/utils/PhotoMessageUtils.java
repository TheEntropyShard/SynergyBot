package me.theentropyshard.synergybot.utils;

import org.telegram.telegrambots.meta.api.objects.File;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PhotoMessageUtils {
    public static List<String> savePhotos(List<File> files, String botToken) throws IOException {
        Random random = new Random();
        List<String> paths = new ArrayList<>();
        for (File file : files) {
            String imageUrl = "https://api.telegram.org/file/bot" + botToken + "/" + file.getFilePath();
            String localFileName = "images/" + new Date().getTime() + random.nextLong() + ".jpeg";
            PhotoMessageUtils.saveImage(imageUrl, localFileName);
            paths.add(localFileName);
        }
        return paths;
    }

    public static void saveImage(String url, String fileName) throws IOException {
        try (InputStream inputStream = new URL(url).openStream();
             OutputStream outputStream = Files.newOutputStream(Paths.get(fileName))) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }
}
