package com.codeit.otboo.domain.clothes.attribute.attributedef.service;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeDefNotFoundException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeNameMissingException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeSelectableValueMissingException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.mapper.ClothesAttributeDefMapper;
import com.codeit.otboo.domain.clothes.attribute.attributedef.repository.ClothesAttributeDefRepository;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.mapper.ClothesAttributeValueMapper;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import com.codeit.otboo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClothesAttributeDefServiceImpl implements ClothesAttributeDefService {

    private final ClothesAttributeDefRepository clothesAttributeDefRepository;
    private final ClothesAttributeValueRepository clothesAttributeValueRepository;
    private final ClothesAttributeValueMapper clothesAttributeValueMapper;
    private final ClothesAttributeDefMapper clothesAttributeDefMapper;

    @Override
    public ClothesAttributeDefResponse createAttributeDef(ClothesAttributeDefCreateRequest request) {
        ClothesAttributeDef clothesAttributeDef = new ClothesAttributeDef(request.name());
        ClothesAttributeDef saveDef = clothesAttributeDefRepository.save(clothesAttributeDef);

        List<ClothesAttributeValue> valueList = request.selectableValues().stream()
                .map(value -> clothesAttributeValueMapper.toClothesAttributeValue(value, saveDef))
                .toList();
        clothesAttributeValueRepository.saveAll(valueList);

        return clothesAttributeDefMapper
                .toClothesAttributeDefResponse(saveDef, request.selectableValues());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClothesAttributeDefResponse> getAllAttributeDef() {
        List<ClothesAttributeDef> attributeDefList = clothesAttributeDefRepository.findAll();
        List<ClothesAttributeDefResponse> list = attributeDefList.stream()
                .map(def -> {
                    List<String> values = clothesAttributeValueRepository.findByAttributeDefIdAndIsActiveTrue(def.getId())
                            .stream()
                            .map(ClothesAttributeValue::getSelectableValue)
                            .toList();

                    return clothesAttributeDefMapper.toClothesAttributeDefResponse(def, values);
                })
                .toList();
        return list;
    }

    @Override
    public ClothesAttributeDefResponse updateAttributeDef(UUID definition_id, ClothesAttributeDefUpdateRequest request) {
        // Def존재확인 및 이름 수정
        ClothesAttributeDef clothesAttributeDef = clothesAttributeDefRepository.findById(definition_id)
                .orElseThrow(() -> new ClothesAttributeDefNotFoundException(definition_id));
        clothesAttributeDef.updateClothesAttributeDefName(request.name());

        // DB에 저장된 기존 Value갖고오기
        List<ClothesAttributeValue> attributeValues =
                clothesAttributeValueRepository.findByAttributeDefId(definition_id);

        // 요청으로 들어온 value처리
        Map<String, ClothesAttributeValue> valueMap = attributeValues.stream()
                .collect(Collectors.toMap(
                        ClothesAttributeValue::getSelectableValue, value -> value
                ));
        Set<String> requestValues = new HashSet<>(request.selectableValues());

        for (ClothesAttributeValue value : attributeValues) {
            String val = value.getSelectableValue();
            if (!requestValues.contains(val)) {
                value.updateIsActive(false);
            } else {
                value.updateIsActive(true);
            }
        }
        for (String reqVal : requestValues) {
            if (!valueMap.containsKey(reqVal)) {
                ClothesAttributeValue newValue
                        = clothesAttributeValueMapper.toClothesAttributeValue(reqVal, clothesAttributeDef);
                clothesAttributeValueRepository.save(newValue);
            }
        }

        return clothesAttributeDefMapper
                .toClothesAttributeDefResponse(clothesAttributeDef, request.selectableValues());
    }

    @Override
    public void deleteAttributeDef(UUID definition_id) {
        // Def Hart Delete
        ClothesAttributeDef attributeDef = clothesAttributeDefRepository.findById(definition_id)
                .orElseThrow(() -> new ClothesAttributeDefNotFoundException(definition_id));

        clothesAttributeDefRepository.delete(attributeDef);
    }
}
