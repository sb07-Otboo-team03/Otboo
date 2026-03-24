package com.codeit.otboo.domain.clothes.management.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request.ClothesAttributeRequest;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.exception.ClothesAttributeValueNotFoundException;
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
        Set<ClothesAttributeValue> attributeValues = getClothesAttributeValues(request.attributes());
        Clothes savedClothes = clothesRepository.save(
            new Clothes(request.name(), request.type(), owner, binaryContent, attributeValues)
        );
        
        return clothesMapper.toDto(
                savedClothes,
                binaryContentUrlResolver.resolve(binaryContent.getId()),
                groupingDefinitionSelectable(savedClothes)
        );
    }

    // 옷 속성값 request DTO 들을 받아 DB 에 있는지 확인하고 있으면 엔티티 Set 으로 반환하는 메소드
    private Set<ClothesAttributeValue> getClothesAttributeValues(List<ClothesAttributeRequest> requests){
        return requests.stream()
                .map(attributeRequest->
                        clothesAttributeValueRepository.findByAttributeDefIdAndSelectableValue(
                                attributeRequest.definitionId(), attributeRequest.value()
                        ).orElseThrow(() -> new ClothesAttributeValueNotFoundException(
                                        attributeRequest.definitionId(), attributeRequest.value()
                                )
                        )
                ).collect(Collectors.toSet());
    }

    // 옷이 가진 속성들별로 속성이 선택할 수 있는 값들을 반환하는 메소드
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