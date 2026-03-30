package com.codeit.otboo.global.util;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Component
public class TimeProvider {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    public LocalDateTime nowDateTime() {
        return LocalDateTime.now(SEOUL);
    }

    public LocalDate nowDate() {
        return LocalDate.now(SEOUL);
    }

    public LocalTime nowTime() {
        return LocalTime.now(SEOUL);
    }
}