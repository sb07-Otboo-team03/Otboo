package com.codeit.otboo.domain.clothes.management.unit.scraper;

import com.codeit.otboo.domain.clothes.management.dto.response.ClothesUrlResponse;
import com.codeit.otboo.domain.clothes.management.fetcher.DocumentFetcher;
import com.codeit.otboo.domain.clothes.management.scraper.ScraperImpl;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ScraperImplTest {
    @Mock
    private DocumentFetcher documentFetcher;

    @InjectMocks
    private ScraperImpl scraper;

    @Nested
    @DisplayName("스크랩")
    class scrap{
        @Test
        @DisplayName("성공: 정상적인 url이 들어오고 Document에 OG태그가 있을 경우 옷 이름과, 이미지 url이 정상 반환된다")
        void scrap_success() {
            // given
            String url = "https://example.com";
            Document document = new Document(url);
            document.head().append("<meta property='og:title' content='테스트 상품' />");
            document.head().append("<meta property='og:image' content='https://image.jpg' />");

            given(documentFetcher.fetch(url)).willReturn(document);

            // when
            ClothesUrlResponse result = scraper.scrap(url);

            // then
            assertThat(result.name()).isEqualTo("테스트 상품");
            assertThat(result.imageUrl()).isEqualTo("https://image.jpg");
        }
    }
}
