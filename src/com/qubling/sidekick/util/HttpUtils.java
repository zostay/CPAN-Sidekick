package com.qubling.sidekick.util;

import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public class HttpUtils {
    private HttpUtils() {}

    public static String getCharset(HttpResponse res) {
        HttpEntity entity = res.getEntity();

        String charset;
        String contentType = entity.getContentType().getValue();
        int charsetIndex = contentType.indexOf("charset=");
        if (charsetIndex >= 0) {
            int endingIndex = contentType.indexOf(";", charsetIndex + 8);
            if (endingIndex >= 0) {
                charset = contentType.substring(charsetIndex + 8, endingIndex);
            }
            else {
                charset = contentType.substring(charsetIndex + 8);
            }
        }
        else {
            charset = "UTF-8";
        }

        return charset;
    }
  
    public static String slurpContent(HttpResponse res) throws IOException {
        HttpEntity entity = res.getEntity();
        String charset = getCharset(res);

        InputStreamReader contentReader = new InputStreamReader(entity.getContent(), charset);
        char[] buf = new char[1000];
        StringBuilder contentStr = new StringBuilder();
        int readLength;
        while ((readLength = contentReader.read(buf)) > -1) {
            contentStr.append(buf, 0, readLength);
        }

        return contentStr.toString();
    }
}
