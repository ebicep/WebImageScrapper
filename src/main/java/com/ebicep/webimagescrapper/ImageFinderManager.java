package com.ebicep.webimagescrapper;

import com.ebicep.webimagescrapper.image.AbstractImage;
import com.ebicep.webimagescrapper.image.imagetypes.ImgImage;
import com.ebicep.webimagescrapper.image.imagetypes.LinkImage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@WebServlet(
        name = "ImageFinderManager",
        urlPatterns = {"/main"}
)
public class ImageFinderManager extends HttpServlet {
    protected static final Gson GSON = new GsonBuilder().create();
    public String initialURL;
    public int maxURLs;
    public boolean useMultithreading;
    public volatile Set<String> imageURLs = Collections.synchronizedSet(new HashSet<>());
    public volatile Set<String> visitedURLs = Collections.synchronizedSet(new HashSet<>());
    public volatile Set<String> visitingURLs = Collections.synchronizedSet(new HashSet<>());
    public ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
    public AtomicBoolean toCancel = new AtomicBoolean(true);

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        this.initialURL = req.getParameter("url");
        this.maxURLs = Integer.parseInt(req.getParameter("maxURLs"));
        this.useMultithreading = Boolean.parseBoolean(req.getParameter("useMultithreading"));

        resp.setContentType("text/json");

        String path = req.getServletPath();

        System.out.println("Got request of:" + path + " with query param:" + initialURL);

        if (initialURL == null || initialURL.isEmpty()) {
            resp.getWriter().print(GSON.toJson(imageURLs));
            return;
        }

        imageURLs.clear();
        visitedURLs.clear();
        visitingURLs.clear();
        executor = new ScheduledThreadPoolExecutor(8);
        toCancel.set(true);

        try {
            long startTime = System.nanoTime();

            if (useMultithreading) {
                addLinksRecursivelyMultiThreaded(initialURL);
                //blocks main thread until al urls are visited or a minute has passed (safe guard)
                while ((toCancel.get() || visitingURLs.size() > 0 || executor.getActiveCount() > 0) && (System.nanoTime() - startTime) / 1000000 < 30000 + maxURLs * 5000L) {
                }
                Thread.sleep(500);
            } else {
                addLinksRecursively(initialURL);
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;

            System.out.println("VISITING: " + visitingURLs);
            System.out.println("VISITED: " + visitedURLs);
            System.out.println("Took: " + duration + " ms");
            System.out.println(imageURLs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
        resp.getWriter().print(GSON.toJson(imageURLs));
    }


    /**
     * Recursively adds links to the imageURLs set using multithreading
     *
     * @param url the url to start from
     * @throws URISyntaxException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void addLinksRecursivelyMultiThreaded(String url) throws URISyntaxException, ExecutionException, InterruptedException {
        visitingURLs.add(url);

        executor.schedule(() -> {
            if (visitedURLs.size() + visitingURLs.size() - 1 > maxURLs) {
                return;
            }

            System.out.println("VISITING URL: " + url);

            try {
                WebPageCrawler webPageCrawler = new WebPageCrawler(url);

                HashSet<String> links = webPageCrawler.getLinksMatchingDomain();
                HashSet<String> images = webPageCrawler.getImages();
                links.removeIf(s -> s.equalsIgnoreCase(url) || s.equalsIgnoreCase(url + "#"));
                links.removeAll(visitedURLs);
                links.removeAll(visitingURLs);
                imageURLs.addAll(images);

                visitingURLs.remove(url);
                visitedURLs.add(url);

                executor.execute(webPageCrawler::quit);

                System.out.println("VISITED URL: " + url + " | Total: " + visitedURLs.size());
                System.out.println("Links Found: " + links);
                System.out.println("Images Found: " + links);
                if (links.isEmpty()) {
                    toCancel.set(false);
                    return;
                }
                for (String link : links) {
                    if (maxURLs - visitingURLs.size() - visitedURLs.size() <= 0) {
                        toCancel.set(false);
                        return;
                    }
                    addLinksRecursivelyMultiThreaded(link);
                }
            } catch (URISyntaxException | ExecutionException | InterruptedException e) {
                visitingURLs.remove(url);
                e.printStackTrace();
            }
        }, (long) (Math.log(visitingURLs.size()) * 100), TimeUnit.MILLISECONDS);
    }

    /**
     * Recursively adds links to the imageURLs set without using multithreading
     *
     * @param url the url to start from
     * @throws URISyntaxException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void addLinksRecursively(String url) throws URISyntaxException, ExecutionException, InterruptedException {
        visitingURLs.add(url);

        if (visitedURLs.size() + visitingURLs.size() - 1 > maxURLs) {
            return;
        }

        System.out.println("VISITING URL: " + url);

        try {
            WebPageCrawler webPageCrawler = new WebPageCrawler(url);

            HashSet<String> links = webPageCrawler.getLinksMatchingDomain();
            HashSet<String> images = webPageCrawler.getImages();
            links.removeIf(s -> !s.startsWith(webPageCrawler.getDomain()));
            links.removeIf(s -> s.contains(url));
            links.removeAll(visitedURLs);
            links.removeAll(visitingURLs);
            imageURLs.addAll(images);

            visitingURLs.remove(url);
            visitedURLs.add(url);

            webPageCrawler.quit();

            System.out.println("VISITED URL: " + url + " | Total: " + visitedURLs.size());
            System.out.println("Links Found: " + links);
            System.out.println("Images Found: " + links);
            if (links.isEmpty()) {
                toCancel.set(false);
                return;
            }
            for (String link : links) {
                if (maxURLs - visitingURLs.size() - visitedURLs.size() <= 0) {
                    toCancel.set(false);
                    return;
                }
                addLinksRecursively(link);
            }
        } catch (URISyntaxException | ExecutionException | InterruptedException e) {
            visitingURLs.remove(url);
            e.printStackTrace();
        }
    }

    @Deprecated
    private List<String> getImageURLsFromPageJsoup(String url) throws IOException {
        List<String> images = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements imgSelect = doc.select("img");//[src~=(?i)\\.(png|jpe?g|webp|gif|svg)]");
        Elements linkSelect = doc.select("link[href~=.*\\.(png|jpe?g|webp|gif|svg|ico)]");//[src~=(?i)\\.(png|jpe?g|webp|gif)]");

        List<AbstractImage> abstractImages = new ArrayList<>();
        try {
            for (Element element : imgSelect) {
                Attributes attributes = element.attributes();
                ImgImage imgImage = new ImgImage(doc.location(), attributes.get("src"), attributes.get("alt"), attributes.get("width"), attributes.get("height"));
                abstractImages.add(imgImage);
            }
            for (Element element : linkSelect) {
                Attributes attributes = element.attributes();
                LinkImage linkImage = new LinkImage(doc.location(), attributes.get("rel"), attributes.get("href"));
                abstractImages.add(linkImage);
            }
            for (AbstractImage abstractImage : abstractImages) {
                if (abstractImage.getURL() != null) {
                    //images.add(abstractImage.getURL());
                    System.out.println(abstractImage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return images;
    }
}
