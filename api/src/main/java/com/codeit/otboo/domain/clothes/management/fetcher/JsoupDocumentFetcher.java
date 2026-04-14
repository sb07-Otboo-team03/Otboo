package com.codeit.otboo.domain.clothes.management.fetcher;

import com.codeit.otboo.domain.clothes.management.exception.ScrapFailUrlException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class JsoupDocumentFetcher implements DocumentFetcher{
    @Override
    public Document fetch(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36")
                    .header("Accept-Language", "ko-KR,ko;q=0.9")
                    .header("Referer", "https://www.google.com")
                    .timeout(5000)
                    .get();
        } catch (Exception e) {
            throw new ScrapFailUrlException(url);
        }
    }
}
