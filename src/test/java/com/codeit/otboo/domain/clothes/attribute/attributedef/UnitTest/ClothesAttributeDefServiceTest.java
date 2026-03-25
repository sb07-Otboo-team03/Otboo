package com.codeit.otboo.domain.clothes.attribute.attributedef.UnitTest;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeDefNotFoundException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeNameMissingException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.exception.ClothesAttributeSelectableValueMissingException;
import com.codeit.otboo.domain.clothes.attribute.attributedef.mapper.ClothesAttributeDefMapper;
import com.codeit.otboo.domain.clothes.attribute.attributedef.repository.ClothesAttributeDefRepository;
import com.codeit.otboo.domain.clothes.attribute.attributedef.service.ClothesAttributeDefServiceImpl;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.mapper.ClothesAttributeValueMapper;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

        given(valueMapper.toClothesAttributeValue(eq("화이트"), any(ClothesAttributeDef.class))).willReturn(color1);
        given(valueMapper.toClothesAttributeValue(eq("블랙"), any(ClothesAttributeDef.class))).willReturn(color2);

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
        verify(defRepository).save(any(ClothesAttributeDef.class));
        verify(valueRepository).saveAll(anyList());
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

        given(defRepository.findById(defId)).willReturn(Optional.of(attributeDef));
        given(valueRepository.findByAttributeDefId(defId)).willReturn(listValues);
        given(valueMapper.toClothesAttributeValue(anyString(), eq(attributeDef)))
                .willReturn(attributeValue3);
        given(valueRepository.saveAll(anyList())).willReturn(listValues)
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
        verify(valueRepository).saveAll(anyList());
        assertThat(defResponse2.name()).isEqualTo("사이즈");
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
    @DisplayName("속성 삭제 성공")
    void deleteDefAndValue_Success() {
        // given
        UUID defId = UUID.randomUUID();
        ClothesAttributeDef attributeDef = new ClothesAttributeDef("색상");

        given(defRepository.findById(defId)).willReturn(Optional.of(attributeDef));

        // when & then
        service.deleteAttributeDef(defId);
        verify(defRepository).delete(attributeDef);
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

        given(defRepository.searchAttributes(any(ClothesAttributeSearchRequest.class)))
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

        verify(defRepository, times(1)).searchAttributes(searchRequest);
        verify(valueRepository, times(1))
                .findByAttributeDefIdInAndIsActiveTrue(anyList());
    }
}