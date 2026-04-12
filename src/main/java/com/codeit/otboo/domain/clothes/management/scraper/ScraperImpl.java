package com.codeit.otboo.domain.clothes.management.scraper;

import com.codeit.otboo.domain.clothes.management.dto.response.ClothesUrlResponse;
import com.codeit.otboo.domain.clothes.management.fetcher.DocumentFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperImpl implements Scraper{
    private final DocumentFetcher documentFetcher;

    @Override
    public ClothesUrlResponse scrap(String url) {
        Document document = documentFetcher.fetch(url);
        String name = extractContent(document, "og:title");
        String imageUrl = extractContent(document, "og:image");

        if (name == null || name.isBlank()) {
            name = document.title();
        }

        log.info("스크랩 결과 => name: {}, imageUrl: {}", name, imageUrl);
        return new ClothesUrlResponse(name, imageUrl);
    }

    private String extractContent(Document document, String property) {
        Element element = document.selectFirst("meta[property=" + property + "]");
        return element != null ? element.attr("content") : null;
    }
}
