package com.ebicep.webimagescrapper.webpageoptions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.stream.Collectors;

public class LinkFinder {

    /**
     * @param webDriver - WebDriver to read links from
     * @param domain    - Domain to filter links by
     * @return - List of all links found on current page filtered by domain
     */
    public static HashSet<String> getLinksMatchingDomain(WebDriver webDriver, String domain) {
        HashSet<String> links = getLinks(webDriver);
        return links.stream()
                .filter(link -> link != null && link.startsWith(domain))
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * @param webDriver - WebDriver to read links from
     * @return - List of all links found on current page
     */
    public static HashSet<String> getLinks(WebDriver webDriver) {
        return webDriver.findElements(By.tagName("a"))
                .stream()
                .map(webElement -> webElement.getAttribute("href"))
                .collect(Collectors.toCollection(HashSet::new));
    }

}
