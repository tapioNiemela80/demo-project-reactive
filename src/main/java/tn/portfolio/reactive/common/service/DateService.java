package tn.portfolio.reactive.common.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateService {
    public LocalDateTime now(){
        return LocalDateTime.now();
    }
}
