package com.ajay.example.userApp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpUtils {
    @Autowired
    private RestTemplate restTemplate;
}
