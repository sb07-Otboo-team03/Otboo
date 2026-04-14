package com.codeit.otboo.domain.clothes.management.fetcher;

import org.jsoup.nodes.Document;

public interface DocumentFetcher {
    Document fetch(String url);
}
