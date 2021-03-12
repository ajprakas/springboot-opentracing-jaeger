package com.ajay.example.mongo;

import io.opentracing.propagation.TextMap;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

public class TextMapCarrier implements TextMap {
    private HttpServletRequest request;

    public TextMapCarrier(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        final Map<String, String> headersMap = new HashMap<>();
        Enumeration<String> headers = this.request.getHeaderNames();
        while(headers.hasMoreElements()){
            String key = headers.nextElement();
            headersMap.put(key, this.request.getHeader(key));
        }
        return headersMap.entrySet().iterator();
    }
    @Override
    public void put(String key, String val) {
        throw new UnsupportedOperationException("carrier is read-only");
    }
}