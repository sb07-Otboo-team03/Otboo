package com.codeit.otboo.domain.follow.mapper;

import com.codeit.otboo.domain.follow.dto.FollowDto;
import com.codeit.otboo.domain.follow.dto.FollowResponse;
import com.codeit.otboo.domain.follow.entity.Follow;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FollowMapper {
    private final UserMapper userMapper;

    public FollowResponse toDto(Follow follow) {
        User followee = follow.getFollowee();
        User follower = follow.getFollower();
        UUID followeeProfileImageId = null;
        UUID followerProfileImageId = null;
        if(followee.getProfile().getBinaryContent() != null) {
            followeeProfileImageId = followee.getProfile().getBinaryContent().getId();
        }
        if(follower.getProfile().getBinaryContent() != null) {
            followerProfileImageId = follower.getProfile().getBinaryContent().getId();
        }

        return new FollowResponse(
            follow.getId(),
            userMapper.toSummaryDto(followee.getId(), followee.getProfile().getName(), followeeProfileImageId),
            userMapper.toSummaryDto(follower.getId(), follower.getProfile().getName(), followerProfileImageId)
        );
    }

    public FollowResponse toDto(FollowDto follow) {

        return new FollowResponse(
            follow.id(),
            userMapper.toSummaryDto(follow.followeeId(), follow.followeeName(), follow.followeeProfileImageId()),
            userMapper.toSummaryDto(follow.followerId(), follow.followerName(), follow.followerProfileImageId())
        );
    }
}
