package com.codeit.otboo.domain.clothes.attribute.attributedef.service;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchCondition;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeAlreadyExistsException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeDefNotFoundException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeValueDuplicateExceptionException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.mapper.ClothesAttributeDefMapper;
import com.codeit.otboo.domain.clothes.attribute.attributedef.repository.ClothesAttributeDefRepository;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.mapper.ClothesAttributeValueMapper;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ClothesAttributeDefServiceImpl implements ClothesAttributeDefService {

    private final ClothesAttributeDefRepository clothesAttributeDefRepository;
    private final ClothesAttributeValueRepository clothesAttributeValueRepository;
    private final ClothesAttributeValueMapper clothesAttributeValueMapper;
    private final ClothesAttributeDefMapper clothesAttributeDefMapper;

    @Override
    @Transactional
    public ClothesAttributeDefResponse createAttributeDef(ClothesAttributeDefCreateRequest request) {

        String name = request.name().trim();

        if (clothesAttributeDefRepository.existsByNameIgnoreCase(name)) {
            throw new ClothesAttributeAlreadyExistsException();
        }

        // postman에서 동일한 속성값 요청 시 입력되는 것 방지
        Function<String, String> normalize = v -> v.trim().toLowerCase();

        Set<String> normalizedValues = request.selectableValues().stream()
                .map(normalize)
                .collect(Collectors.toSet());

        if(normalizedValues.size() != request.selectableValues().size()) {
            throw new ClothesAttributeValueDuplicateExceptionException();
        }

        ClothesAttributeDef clothesAttributeDef = new ClothesAttributeDef(name);
        ClothesAttributeDef saveDef = clothesAttributeDefRepository.save(clothesAttributeDef);

        List<ClothesAttributeValue> valueList = request.selectableValues().stream()
                .map(value ->
                        clothesAttributeValueMapper.toClothesAttributeValue(value, saveDef))
                .toList();
        clothesAttributeValueRepository.saveAll(valueList);

        return clothesAttributeDefMapper
                .toClothesAttributeDefResponse(saveDef, request.selectableValues());
    }

    @Override
    public List<ClothesAttributeDefResponse> getAllAttributeDef(
            ClothesAttributeSearchRequest searchRequest
    ) {
        ClothesAttributeSearchCondition searchCondition = ClothesAttributeSearchCondition.from(searchRequest);

        // 속성명 조회
        List<ClothesAttributeDef> getAttributes = clothesAttributeDefRepository.searchAttributes(searchCondition);

        // 속성명 ID 리스트 생성
        List<UUID> defIds = getAttributes.stream()
                .map(ClothesAttributeDef::getId)
                .toList();

        // true상태인 속성값 조회
        List<ClothesAttributeValue> activeValues
                = clothesAttributeValueRepository.findByAttributeDefIdInAndIsActiveTrue(defIds);

        // 데이터 그룹화
        Map<UUID, List<String>> valueMap = activeValues.stream()
                .collect(Collectors.groupingBy(v -> v.getAttributeDef().getId(),
                        Collectors.mapping(ClothesAttributeValue::getSelectableValue, Collectors.toList())));

        return getAttributes.stream().map(
                def -> clothesAttributeDefMapper.toClothesAttributeDefResponse(
                        def, valueMap.getOrDefault(def.getId(), List.of())
                )).toList();
    }

    @Override
    @Transactional
    public ClothesAttributeDefResponse updateAttributeDef(
            UUID definition_id,
            ClothesAttributeDefUpdateRequest request
    ) {
        // Def존재확인 및 이름 수정
        ClothesAttributeDef clothesAttributeDef = clothesAttributeDefRepository.findById(definition_id)
                .orElseThrow(() -> new ClothesAttributeDefNotFoundException(definition_id));

        String newName = request.name().trim();

        if (!clothesAttributeDef.getName().equalsIgnoreCase(newName) &&
                clothesAttributeDefRepository.existsByNameIgnoreCaseAndIdNot(newName, definition_id)) {
            throw new ClothesAttributeAlreadyExistsException();
        }

        Function<String, String> normalize = v -> v.trim().toLowerCase();

        // value 중복 검증
        Set<String> normalizedRequest = request.selectableValues().stream()
                .map(normalize)
                .collect(Collectors.toSet());

        if(normalizedRequest.size() != request.selectableValues().size()) {
            throw new ClothesAttributeValueDuplicateExceptionException();
        }

        clothesAttributeDef.updateClothesAttributeDefName(newName);

        // DB에 저장된 기존 Value갖고오기
        List<ClothesAttributeValue> attributeValues =
                clothesAttributeValueRepository.findByAttributeDefId(definition_id);

        // 요청으로 들어온 value처리
        Set<String> requestValues = attributeValues.stream()
                .map(v -> normalize.apply(v.getSelectableValue()))
                .collect(Collectors.toSet());

        for (ClothesAttributeValue value : attributeValues) {
            value.updateIsActive(
                    normalizedRequest.contains(normalize.apply(value.getSelectableValue()))
            );
        }

        List<ClothesAttributeValue> saveValue = request.selectableValues().stream()
                .filter(v -> !requestValues.contains(normalize.apply(v)))
                .map(v -> clothesAttributeValueMapper
                        .toClothesAttributeValue(v.trim(), clothesAttributeDef))
                .toList();

        clothesAttributeValueRepository.saveAll(saveValue);

        return clothesAttributeDefMapper
                .toClothesAttributeDefResponse(clothesAttributeDef, request.selectableValues());
    }

    @Override
    @Transactional
    public void deleteAttributeDef(UUID definition_id) {
        // Def Hart Delete
        ClothesAttributeDef attributeDef = clothesAttributeDefRepository.findById(definition_id)
                .orElseThrow(() -> new ClothesAttributeDefNotFoundException(definition_id));

        clothesAttributeDefRepository.delete(attributeDef);
    }
}
