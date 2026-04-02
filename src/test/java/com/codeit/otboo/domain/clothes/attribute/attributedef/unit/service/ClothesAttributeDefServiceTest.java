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
import org.mockito.ArgumentCaptor;
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

    @Mock private ClothesAttributeDefRepository defRepository;
    @Mock private ClothesAttributeValueRepository valueRepository;
    @Mock private ClothesAttributeDefMapper defMapper;
    @Mock private ClothesAttributeValueMapper valueMapper;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ClothesAttributeDefServiceImpl service;

    @Test
    @DisplayName("속성 및 속성값 생성 - 성공")
    void CreateDefAndValue_Success() {
        // given
        UUID defId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ClothesAttributeDefCreateRequest request =
            new ClothesAttributeDefCreateRequest("색상", List.of("화이트", "블랙"));

        ClothesAttributeDef def = new ClothesAttributeDef("색상");
        ReflectionTestUtils.setField(def, "id", defId);

        given(defRepository.existsByNameIgnoreCase("색상")).willReturn(false);
        given(defRepository.save(any())).willReturn(def);

        ClothesAttributeValue v1 = ClothesAttributeValue.builder()
            .selectableValue("화이트")
            .attributeDef(def)
            .build();

        ClothesAttributeValue v2 = ClothesAttributeValue.builder()
            .selectableValue("블랙")
            .attributeDef(def)
            .build();

        given(valueMapper.toClothesAttributeValue("화이트", def)).willReturn(v1);
        given(valueMapper.toClothesAttributeValue("블랙", def)).willReturn(v2);

        given(valueRepository.saveAll(anyList()))
            .willAnswer(invocation -> invocation.getArgument(0));

        User user = User.builder().email("test@test.com").build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        given(userRepository.findAll()).willReturn(List.of(user));

        // 🔥 핵심: Notification 값 세팅
        given(notificationRepository.saveAll(anyList()))
            .willAnswer(invocation -> {
                List<Notification> list = invocation.getArgument(0);
                for (Notification n : list) {
                    ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
                    ReflectionTestUtils.setField(n, "createdAt", now);
                }
                return list;
            });

        given(defMapper.toClothesAttributeDefResponse(eq(def), anyList()))
            .willReturn(new ClothesAttributeDefResponse(
                defId,
                "색상",
                List.of("화이트", "블랙"),
                now
            ));

        // when
        ClothesAttributeDefResponse result =
            service.createAttributeDef(request);

        // then
        assertThat(result.id()).isEqualTo(defId);
        assertThat(result.name()).isEqualTo("색상");
        assertThat(result.selectableValues()).hasSize(2);

        verify(notificationRepository).saveAll(anyList());

        ArgumentCaptor<SseEvent> captor =
            ArgumentCaptor.forClass(SseEvent.class);

        verify(eventPublisher, times(1))
            .publishEvent(captor.capture());

        // 🔥 이벤트 내부 검증
        SseEvent event = captor.getValue();
        assertThat(event.notificationDtoList()).hasSize(1);
    }

    @Test
    @DisplayName("속성 수정 성공")
    void updateDefAndValue_Success() {
        // given
        UUID defId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ClothesAttributeDef def = new ClothesAttributeDef("크기");
        ReflectionTestUtils.setField(def, "id", defId);

        given(defRepository.findById(defId)).willReturn(Optional.of(def));
        given(defRepository.existsByNameIgnoreCaseAndIdNot(any(), any()))
            .willReturn(false);

        ClothesAttributeValue value = ClothesAttributeValue.builder()
            .selectableValue("S")
            .attributeDef(def)
            .isActive(true)
            .build();

        given(valueRepository.findByAttributeDefId(defId))
            .willReturn(List.of(value));

        given(valueRepository.saveAll(anyList()))
            .willAnswer(invocation -> invocation.getArgument(0));

        User user = User.builder().email("test@test.com").build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        given(userRepository.findAll()).willReturn(List.of(user));

        given(notificationRepository.saveAll(anyList()))
            .willAnswer(invocation -> {
                List<Notification> list = invocation.getArgument(0);
                for (Notification n : list) {
                    ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
                    ReflectionTestUtils.setField(n, "createdAt", now);
                }
                return list;
            });

        given(defMapper.toClothesAttributeDefResponse(eq(def), anyList()))
            .willReturn(new ClothesAttributeDefResponse(
                defId,
                "사이즈",
                List.of("S"),
                now
            ));

        // when
        service.updateAttributeDef(
            defId,
            new ClothesAttributeDefUpdateRequest("사이즈", List.of("S"))
        );

        // then
        verify(notificationRepository).saveAll(anyList());
        verify(eventPublisher, times(1))
            .publishEvent(any(SseEvent.class));
    }

    @Test
    @DisplayName("속성 삭제 성공")
    void deleteDefAndValue_Success() {
        // given
        UUID defId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ClothesAttributeDef def = new ClothesAttributeDef("색상");
        given(defRepository.findById(defId)).willReturn(Optional.of(def));

        User user = User.builder().email("test@test.com").build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        given(userRepository.findAll()).willReturn(List.of(user));

        given(notificationRepository.saveAll(anyList()))
            .willAnswer(invocation -> {
                List<Notification> list = invocation.getArgument(0);
                for (Notification n : list) {
                    ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
                    ReflectionTestUtils.setField(n, "createdAt", now);
                }
                return list;
            });

        // when
        service.deleteAttributeDef(defId);

        // then
        verify(defRepository).delete(def);
        verify(notificationRepository).saveAll(anyList());
        verify(eventPublisher, times(1))
            .publishEvent(any(SseEvent.class));
    }
}