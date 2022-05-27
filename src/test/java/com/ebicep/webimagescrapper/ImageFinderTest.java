package com.ebicep.webimagescrapper;


import com.google.gson.Gson;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class ImageFinderTest {

    public HttpServletRequest request;
    public HttpServletResponse response;
    public StringWriter sw;
    public HttpSession session;

    @Before
    public void setUp() throws Exception {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);

        sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Mockito.when(response.getWriter()).thenReturn(pw);
        Mockito.when(request.getRequestURI()).thenReturn("/foo/foo/foo");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/foo/foo/foo"));
        session = Mockito.mock(HttpSession.class);
        Mockito.when(request.getSession()).thenReturn(session);
    }

    @Test
    public void test() throws IOException {
//        Mockito.when(request.getServletPath()).thenReturn("/main");
//        System.out.println(request.getParameterMap());
//        ImageFinderManager imageFinderManager = new ImageFinderManager(
//                request.getParameter("url"),
//                Integer.parseInt(request.getParameter("maxURLs")),
//                Boolean.parseBoolean(request.getParameter("useMultithreading"))
//        );
//        imageFinderManager.doPost(request, response);
        //new ImageFinderManager().doPost(request, response);
        //Assert.assertEquals(new Gson().toJson(ImageFinderManager.imageURLs), sw.toString());
    }

    @Test
    public void testURL() throws URISyntaxException {
        String url = "https://stackoverflow.com/questions/2281087/center-a-div-in-css";
        URI uri = new URI(url);
        Assert.assertEquals(uri.getAuthority(), "www.stackoverflow.com");
        Assert.assertEquals(url.substring(0, url.indexOf(uri.getPath())), "https://www.stackoverflow.com");
    }

    @Test
    public void testChrome() {
//        WebDriverManager.chromedriver().setup();
//        new ChromeDriver(new ChromeOptions()
//                //.addArguments("headless")
//                .addExtensions(new File("src/main/java/com/ebicep/webimagescrapper/extensions/ublockorigin.crx"))
//        );
    }
}



