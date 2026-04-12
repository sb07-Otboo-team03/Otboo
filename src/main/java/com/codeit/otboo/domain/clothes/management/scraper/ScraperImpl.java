package com.codeit.otboo.domain.clothes.management.scraper;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesUrlResponse;
import com.codeit.otboo.domain.clothes.management.exception.ScrapFailUrlException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScraperImpl implements Scraper{
    @Override
    public ClothesUrlResponse scrap(String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Referer", "https://www.google.com")
                    .timeout(5000)
                    .get();

            String name = extractContent(document, "og:title");
            String imageUrl = extractContent(document, "og:image");

            if (name == null || name.isBlank()) {
                name = document.title();
            }

            log.info("스크랩 결과 => name: {}, imageUrl: {}", name, imageUrl);
            return new ClothesUrlResponse(name, imageUrl);
        } catch (Exception e) {
            throw new ScrapFailUrlException(url);
        }
    }

    private String extractContent(Document document, String property) {
        Element element = document.selectFirst("meta[property=" + property + "]");
        return element != null ? element.attr("content") : null;
    }
}
