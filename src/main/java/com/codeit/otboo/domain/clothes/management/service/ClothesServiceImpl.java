package com.codeit.otboo.domain.clothes.management.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesServiceImpl implements ClothesService{
    private final UserRepository userRepository;
    private final ClothesRepository clothesRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;
    private final BinaryContentUrlResolver binaryContentUrlResolver;
    private final ClothesMapper clothesMapper;

    @Transactional
    public ClothesResponse createClothes(
            BinaryContentCreateRequest imageRequest,
            ClothesCreateRequest request
    ){
        // Todo : 유저 관련 도메인 예외가 생기면 수정
        User owner = userRepository.findById(request.ownerId()).orElseThrow(
                () -> new IllegalArgumentException("유저를 찾을 수 없음")
        );
        BinaryContent binaryContent = binaryContentService.upload(imageRequest);
        binaryContentStorage.put(binaryContent.getId(), imageRequest.data());
        Clothes clothes = new Clothes(request.name(), request.type(), owner, binaryContent);

        // Todo : ClothesAttribute Repository 미존재로 인하여 빈 리스트로 일단 넣어놓음
        List<ClothesAttributeValue> attributes = List.of();
        
        return clothesMapper.toDto(
                clothesRepository.save(clothes),
                binaryContentUrlResolver.resolve(binaryContent.getId()),
                attributes
        );
    }
}