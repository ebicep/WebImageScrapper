package com.ebicep.webimagescrapper.image.imagetypes;


import com.ebicep.webimagescrapper.image.AbstractImage;

@Deprecated
public class LinkImage extends AbstractImage {

    private final String rel;
    private final String href;

    public LinkImage(String documentURL, String rel, String href) {
        this.documentURL = documentURL;
        this.rel = rel;
        this.href = href;
    }

    @Override
    public String getURL() {
        return href;
    }

    @Override
    public String toString() {
        return "LinkImage{" +
                "rel='" + rel + '\'' +
                ", href='" + href + '\'' +
                '}';
    }

}
