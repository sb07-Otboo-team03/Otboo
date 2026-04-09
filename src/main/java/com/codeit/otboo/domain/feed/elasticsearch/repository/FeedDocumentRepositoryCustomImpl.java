package com.codeit.otboo.domain.feed.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.codeit.otboo.domain.feed.dto.request.FeedSearchCondition;
import com.codeit.otboo.domain.feed.elasticsearch.document.FeedDocument;
import com.codeit.otboo.global.slice.dto.SortDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedDocumentRepositoryCustomImpl implements FeedDocumentRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchHits<FeedDocument> searchFeed(FeedSearchCondition condition) {

        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        boolean hasCondition = false;

        if (StringUtils.hasText(condition.keywordLike())) {
            boolBuilder.must(m -> m.match(t -> t
                    .field("content")
                    .query(condition.keywordLike())
            ));
            hasCondition = true;
        }

        if (condition.skyStatusEqual() != null) {
            boolBuilder.filter(f -> f.term(t -> t
                    .field("skyStatus")
                    .value(condition.skyStatusEqual().name())
            ));
            hasCondition = true;
        }

        if (condition.precipitationTypeEqual() != null) {
            boolBuilder.filter(f -> f.term(t -> t
                    .field("precipitationType")
                    .value(condition.precipitationTypeEqual().name())
            ));
            hasCondition = true;
        }

        if (condition.authorIdEqual() != null) {
            boolBuilder.filter(f -> f.term(t -> t
                    .field("authorId")
                    .value(condition.authorIdEqual().toString())
            ));
            hasCondition = true;
        }

        Query query = hasCondition
                ? Query.of(q -> q.bool(boolBuilder.build()))
                : Query.of(q -> q.matchAll(m -> m));

        SortOrder direction = SortDirection.DESCENDING.equals(condition.sortDirection()) ?
                SortOrder.Desc : SortOrder.Asc;

        List<SortOptions> sort = List.of(
                SortOptions.of(s -> s
                        .field(f -> f.
                                field(condition.sortBy())
                                .order(direction)
                        )
                ),
                SortOptions.of(s -> s
                        .field(f -> f.
                                field("id")
                                .order(direction)
                        )
                )
        );

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(query)
                .withSort(sort)
                .withMaxResults(condition.limit() + 1);

        if (StringUtils.hasText(condition.cursor()) && condition.idAfter() != null) {
            long parsedCursor = Long.parseLong(condition.cursor());
            String parsedIdAfter = condition.idAfter().toString();

            queryBuilder.withSearchAfter(List.of(parsedCursor, parsedIdAfter));
        }

        return elasticsearchOperations.search(queryBuilder.build(), FeedDocument.class);
    }
}
