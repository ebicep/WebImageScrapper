package com.ebicep.webimagescrapper.image.imagetypes;


import com.ebicep.webimagescrapper.image.AbstractImage;

@Deprecated
public class ImgImage extends AbstractImage {

    private final String src;
    private final String alt;
    private final String width;
    private final String height;

    public ImgImage(String documentURL, String src, String alt, String width, String height) {
        this.documentURL = documentURL;
        this.src = src;
        this.alt = alt;
        this.width = width;
        this.height = height;
    }

    @Override
    public String getURL() {
        if (width.equals("0") || height.equals("0")) {
            return null;
        }
        if (!src.contains("http")) {
            return documentURL + src;
        }
        return src;
    }

    @Override
    public String toString() {
        return "Image{" +
                "src='" + src + '\'' +
                ", alt='" + alt + '\'' +
                ", width='" + width + '\'' +
                ", height='" + height + '\'' +
                '}';
    }

}
