package com.codeit.otboo.domain.like.service;

import java.util.UUID;

public interface LikeService {

    void feedLike(UUID feedId, UUID userId);

    void feedUnlike(UUID feedId, UUID userId);
}
