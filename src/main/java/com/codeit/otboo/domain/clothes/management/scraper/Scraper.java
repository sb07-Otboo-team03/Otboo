package com.codeit.otboo.domain.clothes.management.scraper;

import com.codeit.otboo.domain.clothes.management.dto.response.ClothesUrlResponse;

public interface Scraper {
    ClothesUrlResponse scrap(String url);
}
