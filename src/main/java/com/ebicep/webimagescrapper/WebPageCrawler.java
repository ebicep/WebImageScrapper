package com.ebicep.webimagescrapper;

import com.ebicep.webimagescrapper.webpageoptions.ImageFinder;
import com.ebicep.webimagescrapper.webpageoptions.LinkFinder;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashSet;
import java.util.logging.Level;

public class WebPageCrawler {

    private static final DesiredCapabilities desiredCapabilities;

    static {
        LoggingPreferences loggingPreferences = new LoggingPreferences();
        loggingPreferences.enable(LogType.PERFORMANCE, Level.ALL);

        desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setJavascriptEnabled(true);
        desiredCapabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
    }

    private final String url;
    private final String domain;
    private final WebDriver webDriver;

    public WebPageCrawler(String url) throws URISyntaxException {
        WebDriverManager.chromedriver().setup();
        URI uri = new URI(url);

        this.url = url;
        this.domain = url.substring(0, uri.getHost().length() + url.substring(uri.getHost().length()).indexOf(uri.getPath()));
        this.webDriver = new ChromeDriver(new ChromeOptions()
                .merge(desiredCapabilities)
                .addArguments("start-maximized", "headless")
                //.addExtensions(new File("src/main/java/com/eulerity/hackathon/imagefinder/extensions/ublockorigin.crx"))
        );
        try {
            webDriver.get(url);
            //Waiting for page to load (scripts)
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(5), Duration.ofMillis(50));
            wait.until(driver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
            //Scrolling down to load more images
            ((JavascriptExecutor) webDriver).executeScript("window.scrollBy(0,5000)");
        } catch (Exception e) {
            webDriver.quit();
            e.printStackTrace();
        }
    }

    public static boolean isURL(String url) {
        return (url.contains("http") || url.contains("ftp"));
    }

    public void quit() {
        this.webDriver.close();
        this.webDriver.quit();
    }

    public HashSet<String> getImages() {
        return ImageFinder.getImageURLs(webDriver);
    }

    public HashSet<String> getLinksMatchingDomain() {
        return LinkFinder.getLinksMatchingDomain(webDriver, domain);
    }

    public String getDomain() {
        return domain;
    }

}
