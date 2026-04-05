package com.codeit.otboo.domain.clothes.attribute.attributedef.unit.service;

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
import com.codeit.otboo.domain.clothes.attribute.attributedef.service.ClothesAttributeDefServiceImpl;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.mapper.ClothesAttributeValueMapper;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import com.codeit.otboo.domain.notification.dto.NotificationDto;
import com.codeit.otboo.domain.notification.entity.Notification;
import com.codeit.otboo.domain.notification.mapper.NotificationMapper;
import com.codeit.otboo.domain.notification.repository.NotificationRepository;
import com.codeit.otboo.domain.sse.event.SseEvent;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClothesAttributeDefServiceTest {

    @Mock
    private ClothesAttributeDefRepository defRepository;
    @Mock
    private ClothesAttributeValueRepository valueRepository;
    @Mock
    private ClothesAttributeDefMapper defMapper;
    @Mock
    private ClothesAttributeValueMapper valueMapper;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private ClothesAttributeDefServiceImpl service;

    @Test
    @DisplayName("속성 및 속성값 생성 - 성공")
    void CreateDefAndValue_Success() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDefCreateRequest createRequest
                = new ClothesAttributeDefCreateRequest("색상", List.of("화이트", "블랙"));
        ClothesAttributeDef attributeDef = new ClothesAttributeDef("색상");
        ReflectionTestUtils.setField(attributeDef, "id", defId);

        given(defRepository.save(any(ClothesAttributeDef.class))).willReturn(attributeDef);

        ClothesAttributeValue color1 = ClothesAttributeValue.builder()
                .selectableValue("화이트")
                .attributeDef(attributeDef)
                .build();
        ClothesAttributeValue color2 = ClothesAttributeValue.builder()
                .selectableValue("블랙")
                .attributeDef(attributeDef)
                .build();

        User user = User.builder().email("test@test.com").build();
        List<User> allUser = List.of(user);

        given(valueMapper.toClothesAttributeValue(eq("화이트"), any(ClothesAttributeDef.class)))
                .willReturn(color1);
        given(valueMapper.toClothesAttributeValue(eq("블랙"), any(ClothesAttributeDef.class)))
                .willReturn(color2);
        given(userRepository.findAll()).willReturn(allUser);
        given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(mock(NotificationDto.class));
        given(notificationRepository.saveAll(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        ClothesAttributeDefResponse defResponse1 = ClothesAttributeDefResponse.builder()
                .id(defId)
                .name(createRequest.name())
                .selectableValues(createRequest.selectableValues())
                .build();

        given(defMapper.toClothesAttributeDefResponse(attributeDef, createRequest.selectableValues()))
                .willReturn(defResponse1);

        // when
        ClothesAttributeDefResponse defResponse2 = service.createAttributeDef(createRequest);

        // then
        assertThat(defResponse2.id()).isEqualTo(defId);
        assertThat(defResponse2.name()).isEqualTo("색상");
        assertEquals(2, defResponse2.selectableValues().size());
        verify(defRepository).existsByNameIgnoreCase("색상");
        verify(defRepository).save(any(ClothesAttributeDef.class));
        verify(valueRepository).saveAll(anyList());
        verify(notificationRepository).saveAll(anyList());
        verify(eventPublisher, times(1))
                .publishEvent(any(SseEvent.class));
    }

    @Test
    @DisplayName("속성 생성 실패 - 동일한 속성 존재")
    void createDef_Fail_DuplicateName() {
        // given
        ClothesAttributeDefCreateRequest request
                = ClothesAttributeDefCreateRequest.builder()
                .name("색상")
                .selectableValues(List.of("화이트"))
                .build();
        given(defRepository.existsByNameIgnoreCase("색상")).willReturn(true);

        // when & then
        assertThrows(ClothesAttributeAlreadyExistsException.class, () -> service.createAttributeDef(request));

        verify(defRepository, never()).save(any());
    }

    @Test
    @DisplayName("속성 생성 실패 - 속성값 중복")
    void createDef_Fail_DuplicateValues() {
        // given
        ClothesAttributeDefCreateRequest request =
                ClothesAttributeDefCreateRequest.builder()
                        .name("색상")
                        .selectableValues(List.of("화이트", "화이트"))
                        .build();

        given(defRepository.existsByNameIgnoreCase(anyString())).willReturn(false);

        // when & then
        assertThrows(ClothesAttributeValueDuplicateExceptionException.class,
                () -> service.createAttributeDef(request));

    }

    @Test
    @DisplayName("속성 생성 실패 - 공백 포함 중복")
    void createDef_Fail_NormalizedDuplicate() {
        // given
        ClothesAttributeDefCreateRequest request =
                new ClothesAttributeDefCreateRequest("색상", List.of("화이트", " 화이트 "));

        given(defRepository.existsByNameIgnoreCase(anyString())).willReturn(false);

        // when & then
        assertThrows(ClothesAttributeValueDuplicateExceptionException.class,
                () -> service.createAttributeDef(request));
    }

    @Test
    @DisplayName("속성 생성 실패 - 속성값 없음")
    void createDef_Fail_EmptyValue() {
        // given
        ClothesAttributeDefCreateRequest request =
                new ClothesAttributeDefCreateRequest("색상", List.of("블랙", " "));

        given(defRepository.existsByNameIgnoreCase(anyString())).willReturn(false);

        // when & then
        assertThrows(ClothesAttributeValueEmptyException.class,
                () -> service.createAttributeDef(request));
    }

    @Test
    @DisplayName("속성명/속성값 변경")
    void updateDefAndValue_Success() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDef attributeDef = new ClothesAttributeDef("크기");
        ReflectionTestUtils.setField(attributeDef, "id", defId);

        ClothesAttributeValue attributeValue1 = ClothesAttributeValue.builder()
                .selectableValue("S")
                .attributeDef(attributeDef)
                .isActive(true)
                .build();
        ClothesAttributeValue attributeValue2 = ClothesAttributeValue.builder()
                .selectableValue("M")
                .attributeDef(attributeDef)
                .isActive(true)
                .build();

        List<ClothesAttributeValue> listValues = List.of(attributeValue1, attributeValue2);

        ClothesAttributeDefUpdateRequest updateRequest
                = new ClothesAttributeDefUpdateRequest(
                "사이즈",
                List.of("S", "L")
        );

        ClothesAttributeValue attributeValue3 = ClothesAttributeValue.builder()
                .selectableValue("L")
                .attributeDef(attributeDef)
                .isActive(true)
                .build();

        User user = User.builder().email("test@test.com").build();
        List<User> allUser = List.of(user);

        given(defRepository.findById(defId)).willReturn(Optional.of(attributeDef));
        given(valueRepository.findByAttributeDefId(defId)).willReturn(listValues);
        given(valueMapper.toClothesAttributeValue(anyString(), eq(attributeDef)))
                .willReturn(attributeValue3);
        given(valueRepository.saveAll(anyList())).willReturn(listValues)
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userRepository.findAll()).willReturn(allUser);
        given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(mock(NotificationDto.class));
        given(notificationRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));

        ClothesAttributeDefResponse expectedResponse = ClothesAttributeDefResponse.builder()
                .id(defId)
                .name("사이즈")
                .selectableValues(List.of("S", "L")).build();
        given(defMapper.toClothesAttributeDefResponse(eq(attributeDef), anyList())).willReturn(expectedResponse);

        // when
        ClothesAttributeDefResponse defResponse2 = service.updateAttributeDef(defId, updateRequest);

        // then
        assertThat(attributeDef.getName()).isEqualTo("사이즈");
        assertThat(attributeValue1.isActive()).isTrue();
        assertThat(attributeValue2.isActive()).isFalse();
        verify(defRepository).existsByNameIgnoreCaseAndIdNot(anyString(), any());
        verify(valueRepository).saveAll(anyList());
        assertThat(defResponse2.name()).isEqualTo("사이즈");
        verify(notificationRepository).saveAll(anyList());
        verify(eventPublisher, times(1))
                .publishEvent(any(SseEvent.class));
    }

    @Test
    @DisplayName("속성 수정 - 삭제된 속성값 false처리")
    void updateDefAndValue_RemovedValue() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDef def = new ClothesAttributeDef("사이즈");

        ClothesAttributeValue size1 = ClothesAttributeValue.builder()
                .selectableValue("s")
                .attributeDef(def)
                .isActive(true)
                .build();

        ClothesAttributeValue size2 = ClothesAttributeValue.builder()
                .selectableValue("M")
                .attributeDef(def)
                .isActive(true)
                .build();

        User user = User.builder().email("test@test.com").build();
        List<User> allUser = List.of(user);

        given(defRepository.findById(defId)).willReturn(Optional.of(def));
        given(valueRepository.findByAttributeDefId(defId)).willReturn(List.of(size1, size2));
        given(userRepository.findAll()).willReturn(allUser);
        given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(mock(NotificationDto.class));
        given(notificationRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when & then
        ClothesAttributeDefUpdateRequest updateRequest
                = new ClothesAttributeDefUpdateRequest("사이즈", List.of("S"));

        service.updateAttributeDef(defId, updateRequest);

        assertThat(size2.isActive()).isFalse();
        verify(notificationRepository).saveAll(anyList());
        verify(eventPublisher, times(1))
                .publishEvent(any(SseEvent.class));
    }

    @Test
    @DisplayName("속성 수정 - 비활성 값 다시 활성화")
    void updateDefAndValue_ReactivateValue() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDef def = new ClothesAttributeDef("사이즈");

        ClothesAttributeValue size1 = ClothesAttributeValue.builder()
                .selectableValue("L")
                .attributeDef(def)
                .isActive(false)
                .build();

        given(defRepository.findById(defId)).willReturn(Optional.of(def));
        given(valueRepository.findByAttributeDefId(defId)).willReturn(List.of(size1));

        User user = User.builder().email("test@test.com").build();
        List<User> allUser = List.of(user);

        given(userRepository.findAll()).willReturn(allUser);
        given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(mock(NotificationDto.class));
        given(notificationRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when & then
        ClothesAttributeDefUpdateRequest updateRequest
                = new ClothesAttributeDefUpdateRequest("사이즈", List.of("L"));

        service.updateAttributeDef(defId, updateRequest);
        assertThat(size1.isActive()).isTrue();
        verify(notificationRepository).saveAll(anyList());
        verify(eventPublisher, times(1))
                .publishEvent(any(SseEvent.class));
    }

    @Test
    @DisplayName("속성을 찾을 수 없음")
    void updateDefAndValue_Fail() {
        UUID defId = UUID.randomUUID();

        given(defRepository.findById(defId)).willReturn(Optional.empty());

        assertThrows(ClothesAttributeDefNotFoundException.class,
                () -> service.updateAttributeDef(defId,
                        new ClothesAttributeDefUpdateRequest("사이즈", List.of("S"))));
    }

    @Test
    @DisplayName("속성 수정 실패 - 다른 속성과 이름 중복")
    void updateDef_Fail_DuplicateName() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDef def = new ClothesAttributeDef("기존");
        ReflectionTestUtils.setField(def, "id", defId);

        ClothesAttributeDefUpdateRequest updateRequest =
                new ClothesAttributeDefUpdateRequest("색상", List.of("화이트"));

        given(defRepository.findById(defId)).willReturn(Optional.of(def));
        given(defRepository.existsByNameIgnoreCaseAndIdNot("색상", defId)).willReturn(true);

        // when & then
        assertThrows(ClothesAttributeAlreadyExistsException.class,
                () -> service.updateAttributeDef(defId, updateRequest));
    }

    @Test
    @DisplayName("속성 수정 실패 - value 중복")
    void updateDef_Fail_DuplicateValues() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDef def = new ClothesAttributeDef("사이즈");

        ClothesAttributeDefUpdateRequest request =
                new ClothesAttributeDefUpdateRequest("사이즈", List.of("S", "s"));

        given(defRepository.findById(defId)).willReturn(Optional.of(def));

        // when & then
        assertThrows(ClothesAttributeValueDuplicateExceptionException.class,
                () -> service.updateAttributeDef(defId, request));
    }

    @Test
    @DisplayName("속성 삭제 성공")
    void deleteDefAndValue_Success() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDef attributeDef = new ClothesAttributeDef("색상");

        given(defRepository.findById(defId)).willReturn(Optional.of(attributeDef));

        User user = User.builder().email("test@test.com").build();
        List<User> allUser = List.of(user);

        given(userRepository.findAll()).willReturn(allUser);
        given(notificationMapper.toDto(any(Notification.class)))
                .willReturn(mock(NotificationDto.class));
        given(notificationRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when & then
        service.deleteAttributeDef(defId);
        verify(defRepository).delete(attributeDef);
        verify(notificationRepository).saveAll(anyList());
        verify(eventPublisher, times(1))
                .publishEvent(any(SseEvent.class));
    }

    @Test
    @DisplayName("속성 삭제 실패")
    void deleteDefAndValue_Fail() {
        UUID defId = UUID.randomUUID();

        when(defRepository.findById(defId)).thenReturn(Optional.empty());

        assertThrows(ClothesAttributeDefNotFoundException.class,
                () -> service.deleteAttributeDef(defId));
    }

    @Test
    @DisplayName("목록 조회")
    void getValueList() {
        // given
        UUID colorDefId = UUID.randomUUID();
        UUID sizeDefId = UUID.randomUUID();

        ClothesAttributeDef colorDef = new ClothesAttributeDef("색상");
        ReflectionTestUtils.setField(colorDef, "id", colorDefId);

        ClothesAttributeDef sizeDef = new ClothesAttributeDef("사이즈");
        ReflectionTestUtils.setField(sizeDef, "id", sizeDefId);

        List<ClothesAttributeDef> listDefs = List.of(colorDef, sizeDef);
        ClothesAttributeSearchRequest searchRequest
                = new ClothesAttributeSearchRequest(
                "name", "ASCENDING", "색상"
        );

        ClothesAttributeSearchCondition searchCondition = ClothesAttributeSearchCondition.from(searchRequest);

        ClothesAttributeValue colorValue1 = ClothesAttributeValue.builder()
                .selectableValue("레드")
                .attributeDef(colorDef)
                .isActive(true)
                .build();
        ClothesAttributeValue colorValue2 = ClothesAttributeValue.builder()
                .selectableValue("그린")
                .attributeDef(colorDef)
                .isActive(true)
                .build();
        ClothesAttributeValue sizeValue = ClothesAttributeValue.builder()
                .selectableValue("L")
                .attributeDef(colorDef)
                .isActive(true)
                .build();

        List<ClothesAttributeValue> listValues = List.of(colorValue1, colorValue2, sizeValue);

        given(defRepository.searchAttributes(any(ClothesAttributeSearchCondition.class)))
                .willReturn(listDefs);
        given(valueRepository.findByAttributeDefIdInAndIsActiveTrue(anyList()))
                .willReturn(listValues);
        given(defMapper.toClothesAttributeDefResponse(eq(colorDef), anyList()))
                .willReturn(new ClothesAttributeDefResponse(
                        colorDefId, "색상", List.of("레드", "그린"), LocalDateTime.now()
                ));
        given(defMapper.toClothesAttributeDefResponse(eq(sizeDef), anyList()))
                .willReturn(new ClothesAttributeDefResponse(
                        sizeDefId, "사이즈", List.of("L"), LocalDateTime.now()
                ));

        // when
        List<ClothesAttributeDefResponse> defResponseList = service.getAllAttributeDef(searchRequest);

        // then
        assertThat(defResponseList).hasSize(2);
        assertThat(defResponseList.get(0).name()).isEqualTo(colorDef.getName());
        assertThat(defResponseList.get(0).selectableValues()).hasSize(2);
        assertThat(defResponseList.get(0).selectableValues()).containsExactly("레드", "그린");

        assertThat(defResponseList.get(1).name()).isEqualTo(sizeDef.getName());
        assertThat(defResponseList.get(1).selectableValues()).containsExactly("L");

        verify(defRepository, times(1)).searchAttributes(searchCondition);
        verify(valueRepository, times(1))
                .findByAttributeDefIdInAndIsActiveTrue(anyList());
    }
}