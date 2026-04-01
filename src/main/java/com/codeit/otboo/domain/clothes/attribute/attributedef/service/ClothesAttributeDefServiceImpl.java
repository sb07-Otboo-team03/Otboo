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
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeValueEmptyException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.mapper.ClothesAttributeDefMapper;
import com.codeit.otboo.domain.clothes.attribute.attributedef.repository.ClothesAttributeDefRepository;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.mapper.ClothesAttributeValueMapper;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.dto.NotificationLevel;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.sse.event.ClothesAttributeCreateEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ClothesAttributeDefServiceImpl implements ClothesAttributeDefService {

    private final ClothesAttributeDefRepository clothesAttributeDefRepository;
    private final ClothesAttributeValueRepository clothesAttributeValueRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ClothesAttributeValueMapper clothesAttributeValueMapper;
    private final ClothesAttributeDefMapper clothesAttributeDefMapper;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ClothesAttributeDefResponse createAttributeDef(ClothesAttributeDefCreateRequest request) {

        String name = request.name().trim();

        if (clothesAttributeDefRepository.existsByNameIgnoreCase(name)) {
            throw new ClothesAttributeAlreadyExistsException();
        }

        // postman에서 동일한 속성값 요청 시 입력되는 것 방지
        validateAndNormalize(request.selectableValues());

        ClothesAttributeDef clothesAttributeDef = new ClothesAttributeDef(name);
        ClothesAttributeDef saveDef = clothesAttributeDefRepository.save(clothesAttributeDef);

        List<ClothesAttributeValue> valueList = request.selectableValues().stream()
                .map(value ->
                        clothesAttributeValueMapper.toClothesAttributeValue(format(value), saveDef))
                .toList();
        clothesAttributeValueRepository.saveAll(valueList);

        List<String> list = valueList.stream().map(ClothesAttributeValue::getSelectableValue).toList();

        notificationEvent(
                "새로운 의상 속성이 추가되었어요.",
                "내 의상에 [" + saveDef.getName() + "] 속성을 추가해보세요."
                );

        return clothesAttributeDefMapper.toClothesAttributeDefResponse(saveDef, list);
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

        clothesAttributeDef.updateClothesAttributeDefName(newName);

        // 빈 문자열, 중복 검증
        Set<String> requestSet = validateAndNormalize(request.selectableValues());

        List<ClothesAttributeValue> existingValues
                = clothesAttributeValueRepository.findByAttributeDefId(definition_id);

        Set<String> existingNormalized = existingValues.stream()
                .map(clothesAttributeValue -> normalize(clothesAttributeValue.getSelectableValue()))
                .collect(Collectors.toSet());

        for (ClothesAttributeValue value : existingValues) {
            String normalized = normalize(value.getSelectableValue());
            if (requestSet.contains(normalized)) {
                value.updateIsActive(true);
            } else {
                value.updateIsActive(false);
            }
        }

        List<ClothesAttributeValue> newValue = request.selectableValues().stream()
                .filter(value -> !existingNormalized.contains(normalize(value)))
                .map(value -> clothesAttributeValueMapper
                        .toClothesAttributeValue(format(value), clothesAttributeDef))
                .toList();

        clothesAttributeValueRepository.saveAll(newValue);

        List<ClothesAttributeValue> allValues = new ArrayList<>(existingValues);
        allValues.addAll(newValue);

        List<String> list = allValues.stream()
                .filter(ClothesAttributeValue::isActive)
                .map(ClothesAttributeValue::getSelectableValue).toList();

        notificationEvent(
                "의상 속성이 변경되었어요",
                "[" +clothesAttributeDef.getName()+ "] 속성을 확인해보세요."
        );

        return clothesAttributeDefMapper
                .toClothesAttributeDefResponse(clothesAttributeDef, list);
    }

    @Override
    @Transactional
    public void deleteAttributeDef(UUID definition_id) {
        // Def Hart Delete
        ClothesAttributeDef attributeDef = clothesAttributeDefRepository.findById(definition_id)
                .orElseThrow(() -> new ClothesAttributeDefNotFoundException(definition_id));

        notificationEvent(
                "의상 속성이 삭제되었어요.",
                "[" + attributeDef.getName() + "] 속성이 삭제되었어요."
        );
        clothesAttributeDefRepository.delete(attributeDef);
    }

    // 입력값 정규화
    private String normalize(String value) {
        return value.trim().toLowerCase();
    }

    // 저장 시 첫글자 대문자, 두번째부터 소문자로 저장
    private String format(String value) {
        String trimmed = value.trim().toLowerCase();
        if (trimmed.isEmpty()) {
            throw new ClothesAttributeValueEmptyException();
        }
        return trimmed.toUpperCase();
    }

    private Set<String> validateAndNormalize(List<String> values) {
        List<String> normalizedList = values.stream()
                .map(this::normalize)
                .toList();

        // 빈 문자열 체크
        if (normalizedList.stream().anyMatch(String::isEmpty)) {
            throw new ClothesAttributeValueEmptyException();
        }

        // 중복 속성값 체크
        Set<String> normalizedSet = new HashSet<>(normalizedList);
        if (normalizedSet.size() != normalizedList.size()) {
            throw new ClothesAttributeValueDuplicateExceptionException();
        }
        return normalizedSet;
    }

    private void notificationEvent(String title, String content){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User Not Fount"));

        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .level(NotificationLevel.INFO)
                .receiver(currentUser)
                .build();
        notificationRepository.save(notification);

        NotificationDto eventDto = notificationMapper.toEventDto(notification);

        log.debug("eventId: " + notification.getId());
        log.debug("eventReceiverId: " + notification.getReceiver().getId());

        eventPublisher.publishEvent(new ClothesAttributeCreateEvent(
                eventDto,
                notification.getCreatedAt()
        ));
    }
}