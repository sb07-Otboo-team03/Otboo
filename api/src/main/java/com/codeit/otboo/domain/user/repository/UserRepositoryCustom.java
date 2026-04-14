package com.codeit.otboo.domain.user.repository;

import com.codeit.otboo.domain.user.dto.request.UserSearchCondition;
import com.codeit.otboo.domain.user.entity.User;
import org.springframework.data.domain.Slice;

public interface UserRepositoryCustom {
    Slice<User> findAllByKeywordLike(UserSearchCondition condition);

    long countTotalElements(UserSearchCondition condition);


}
