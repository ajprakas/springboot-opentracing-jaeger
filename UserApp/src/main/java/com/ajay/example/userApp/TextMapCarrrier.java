package com.ajay.example.userApp;

import io.opentracing.propagation.TextMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.http.HttpHeaders;

public class TextMapCarrrier implements TextMap {
    private HttpHeaders headers;

    public TextMapCarrrier(HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("carrier is write-only");
    }
    @Override
    public void put(String key, String val) {
        headers.add(key, val);
    }
}
