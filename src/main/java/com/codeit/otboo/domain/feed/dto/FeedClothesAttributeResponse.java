package com.codeit.otboo.domain.feed.dto;

import java.util.List;
import java.util.UUID;

public record FeedClothesAttributeResponse(
    UUID definitionId,
    String definitionName,
    List<String> selectableValues,
    String value
) {}
