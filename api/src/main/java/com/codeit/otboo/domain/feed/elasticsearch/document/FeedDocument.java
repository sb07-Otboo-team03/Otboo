package com.codeit.otboo.domain.feed.elasticsearch.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Document(indexName = "feeds-v2")
public class FeedDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(suffix = "en", type = FieldType.Text, analyzer = "english")
            }
    )
    private String content;

    @Field(type = FieldType.Keyword)
    private String skyStatus;

    @Field(type = FieldType.Keyword)
    private String precipitationType;

    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Long)
    private Long likeCount;

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long createdAt;

    public void updateLikeCount(long newLikeCount) {
        this.likeCount = newLikeCount;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }
}
