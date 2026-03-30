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
import com.codeit.otboo.domain.clothes.management.exception.DuplicateClothesAttributeDefinitionException;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.management.vo.ClothesAttributeSelection;
import com.codeit.otboo.domain.clothes.management.vo.ClothesAttributeValueKey;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(UserNotFoundException::new);
        BinaryContent binaryContent = null;
        if(imageRequest != null){
            binaryContent = binaryContentService.upload(imageRequest);
            binaryContentStorage.put(binaryContent.getId(), imageRequest.data());
        }
        ClothesAttributeSelection clothesAttributeSelection = getClothesAttributeValues(request.attributes());
        Clothes savedClothes = clothesRepository.save(
            new Clothes(request.name(), request.type(), owner, binaryContent,  clothesAttributeSelection.selectedValues())
        );
        return clothesMapper.toDto(
                savedClothes,
                binaryContent == null ? null: binaryContentUrlResolver.resolve(binaryContent.getId()),
                groupSelectableValuesByAttributeId(clothesAttributeSelection.allSelectableValues())
        );
    }

    // 옷 속성값 request DTO 들을 받아 DB 에 있는지 확인하고 있으면 엔티티 Set 으로 반환하는 메소드
    private ClothesAttributeSelection getClothesAttributeValues(List<ClothesAttributeRequest> requests){
        if(requests.isEmpty()) return new ClothesAttributeSelection(Set.of(), List.of());
        Set<UUID> requestDefinitionIds = validateAndExtractDefinitionIds(requests);
        List<ClothesAttributeValue> allSelectableValues = clothesAttributeValueRepository.findByAttributeDefIdIn(
                new ArrayList<>(requestDefinitionIds));
        Map<ClothesAttributeValueKey, ClothesAttributeValue> selectableAttributeValueMap =
                allSelectableValues.stream().collect(Collectors.toMap(
        attributeValue ->
                        new ClothesAttributeValueKey(
                                attributeValue.getAttributeDef().getId(),
                                attributeValue.getSelectableValue()
                        ),
        attributeValue -> attributeValue
                ));
        Set<ClothesAttributeValue> selectedValueList =  requests.stream()
                .map(request -> getAttributeValueOrThrowByRequest(
                        request, selectableAttributeValueMap
                )).collect(Collectors.toSet());

        return new ClothesAttributeSelection(selectedValueList, allSelectableValues);
    }

    // 해당 요청에 같은 속성 id 가 여러 개 들어왔는지 검증하는 메소드
    private Set<UUID> validateAndExtractDefinitionIds(List<ClothesAttributeRequest> requests){
        Set<UUID> difinitionIdSet = new HashSet<>();
        for (ClothesAttributeRequest request : requests) {
            if (!difinitionIdSet.add(request.definitionId())) {
                throw new DuplicateClothesAttributeDefinitionException(request.definitionId());
            }
        }
        return difinitionIdSet;
    }

    // Map 에 존재하지 않으면 throws 를 던지는 메소드
    private ClothesAttributeValue getAttributeValueOrThrowByRequest(
            ClothesAttributeRequest attributeRequest,
            Map<ClothesAttributeValueKey, ClothesAttributeValue> selectableAttributeValueMap){
        ClothesAttributeValue attributeValue = selectableAttributeValueMap.get(
                new ClothesAttributeValueKey(attributeRequest.definitionId(), attributeRequest.value())
        );
        if (attributeValue == null) {
            throw new ClothesAttributeValueNotFoundException(
                    attributeRequest.definitionId(),
                    attributeRequest.value()
            );
        }
        return attributeValue;
    }

    // 옷이 가진 속성들별로 속성이 선택할 수 있는 값들을 반환하는 메소드
    private Map<UUID, List<String>> groupSelectableValuesByAttributeId(
            List<ClothesAttributeValue> allSelectableValues
    ){
        if(allSelectableValues.isEmpty()) return Map.of();
        return allSelectableValues.stream()
                .collect(Collectors.groupingBy(
                        attributeValue -> attributeValue.getAttributeDef().getId(),
                        Collectors.mapping(ClothesAttributeValue::getSelectableValue, Collectors.toList())
                ));
    }
}