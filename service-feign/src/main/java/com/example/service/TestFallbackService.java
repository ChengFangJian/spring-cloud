package com.example.service;

import org.springframework.stereotype.Component;

@Component
public class TestFallbackService implements TestService {
    @Override
    public String sayHiFromClientOne(String name) {
        return "sorry:" + name;
    }
}
