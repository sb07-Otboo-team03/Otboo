package com.codeit.otboo;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.codeit.otboo.domain.feed.elasticsearch.repository.FeedDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class OtbooApplicationTests {

    @MockitoBean
    private ElasticsearchOperations elasticsearchOperations;

    @MockitoBean
    private ElasticsearchClient elasticsearchClient;

    @MockitoBean
    private FeedDocumentRepository feedDocumentRepository;

	@Test
	void contextLoads() {
	}

}
