package com.ebicep.webimagescrapper.webpageoptions;

import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.util.HashSet;
import java.util.List;

import static com.ebicep.webimagescrapper.WebPageCrawler.isURL;


public class ImageFinder {


    /**
     * @param webDriver - WebDriver to read logs from
     * @return - List of all the images found on the page
     */
    public static HashSet<String> getImageURLs(WebDriver webDriver) {
        HashSet<String> imageURLs = new HashSet<>();
        List<LogEntry> logEntries = webDriver.manage().logs().get(LogType.PERFORMANCE).toJson();
        for (LogEntry logEntry : logEntries) {
            JSONObject jsonObject = new JSONObject(logEntry.getMessage());
            JSONObject params = jsonObject.getJSONObject("message").getJSONObject("params");
            if (params.has("type") && params.get("type").equals("Image")) {
                if (params.has("request")) {
                    String imgUrl = params.getJSONObject("request").getString("url");
                    if (isURL(imgUrl)) {
                        imageURLs.add(imgUrl);
                    }
                } else if (params.has("response")) {
                    String imgUrl = params.getJSONObject("response").getString("url");
                    if (isURL(imgUrl)) {
                        imageURLs.add(imgUrl);
                    }
                }
            }
        }
        //removing urls in data URI scheme
        imageURLs.removeIf(url -> !isValidPicture(url));
        return imageURLs;
    }

    public static boolean isValidPicture(String url) {
        return !url.startsWith("data:image");
//        String extension = url.substring(url.lastIndexOf(".") + 1);
//        extension = extension.replace("e", ""); //for filtering jpeg, making them jpg to appear as real images
//        if(extension.length() < 3) {
//            return false;
//        }
//        String formattedExtension = extension.substring(0, 3);
//        return formattedExtension.equalsIgnoreCase("gif") ||
//                formattedExtension.equalsIgnoreCase("png") ||
//                formattedExtension.equalsIgnoreCase("jpg") ||
//                formattedExtension.equalsIgnoreCase("svg");
    }

}
