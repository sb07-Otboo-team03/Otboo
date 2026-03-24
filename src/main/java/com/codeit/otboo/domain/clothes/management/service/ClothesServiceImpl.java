package com.codeit.otboo.domain.clothes.management.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClothesServiceImpl implements ClothesService{
    private final UserRepository userRepository;
    private final ClothesRepository clothesRepository;
    private final ClothesAttributeValueRepository clothesAttributeValueRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final BinaryContentService binaryContentService;
    private final BinaryContentUrlResolver binaryContentUrlResolver;
    private final ClothesMapper clothesMapper;

    @Transactional
    public ClothesResponse createClothes(
            BinaryContentCreateRequest imageRequest,
            ClothesCreateRequest request
    ){
        User owner = userRepository.findById(request.ownerId()).orElseThrow(
                UserNotFoundException::new);
        BinaryContent binaryContent = binaryContentService.upload(imageRequest);
        binaryContentStorage.put(binaryContent.getId(), imageRequest.data());

        // 옷 속성-값
        Set<ClothesAttributeValue> attributeValues = request.attributes().stream()
            .map(attributeRequest->
                clothesAttributeValueRepository.findByAttributeDefIdAndSelectableValue(
                    attributeRequest.definitionId(), attributeRequest.value()
                ).orElseThrow(() -> new IllegalArgumentException("해당 옷 속성값이 존재하지 않습니다")
            )
        ).collect(Collectors.toSet());

        Clothes savedClothes = clothesRepository.save(
            new Clothes(request.name(), request.type(), owner, binaryContent, attributeValues)
        );
        
        return clothesMapper.toDto(
                savedClothes,
                binaryContentUrlResolver.resolve(binaryContent.getId()),
                groupingDefinitionSelectable(savedClothes)
        );
    }

    private Map<UUID, List<String>> groupingDefinitionSelectable(Clothes clothes){
        List<ClothesAttributeValue> selectableValues = clothesAttributeValueRepository.findByAttributeDefIdIn(
                clothes.getValues().stream().map(attributeValue ->
                        attributeValue.getAttributeDef().getId()
                ).toList()
        );
        return selectableValues.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getAttributeDef().getId(),
                        Collectors.mapping(ClothesAttributeValue::getSelectableValue, Collectors.toList())
                ));
    }
}