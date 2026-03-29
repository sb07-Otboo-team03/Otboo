package com.codeit.otboo.domain.clothes.management.unit.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.binarycontent.storage.BinaryContentStorage;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeWithDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.fixture.ClothesAttributeDefFixture;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request.ClothesAttributeRequest;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.exception.ClothesAttributeValueNotFoundException;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.fixture.ClothesAttributeValueFixture;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.fixture.ClothesFixture;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.management.service.ClothesServiceImpl;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
public class ClothesServiceImplTest {
    @Mock
    UserRepository userRepository;

    @Mock
    ClothesRepository clothesRepository;

    @Mock
    ClothesAttributeValueRepository clothesAttributeValueRepository;

    @Mock
    BinaryContentStorage binaryContentStorage;

    @Mock
    BinaryContentService binaryContentService;

    @Mock
    BinaryContentUrlResolver binaryContentUrlResolver;

    @Mock
    ClothesMapper clothesMapper;

    @InjectMocks
    ClothesServiceImpl clothesService;

    @Nested
    @DisplayName("옷 생성")
    class ClothesCreate {
        @Test
        @DisplayName("성공: 유효한 ownerId, name, type이 들어올 경우 옷이 생성된다")
        void  createClothes_Success(){
            // given
            User user = UserFixture.create();
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), "새 옷", ClothesType.ETC, List.of());
            Clothes clothes = ClothesFixture.create(request, null);
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    clothes.getName(),
                    null,
                    ClothesType.ETC,
                    List.of()
            );
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
            given(clothesMapper.toDto(clothes, null, Map.of())).willReturn(response);

            // when
            ClothesResponse result = clothesService.createClothes(null, request);

            // then
            assertThat(result).extracting(ClothesResponse::name, ClothesResponse::ownerId, ClothesResponse::imageUrl)
                    .containsExactly(request.name(), request.ownerId(), null);

            then(userRepository).should().findById(user.getId());
            then(clothesRepository).should().save(any(Clothes.class));
            then(clothesMapper).should().toDto(clothes, null, Map.of());
        }

        @Test
        @DisplayName("""
            성공: 유효한 파라미터와 이미지가 들어올 경우
            옷 이미지 정보와 이미지 데이터가 저장되고,
            이미지 URL 을 반환한다
        """)
        void createClothes_Success_with_image() {
            // given
            User user = UserFixture.create();
            BinaryContentCreateRequest imageRequest = new BinaryContentCreateRequest(
                    "test".getBytes(), "test_file", "image/png", 30L);
            BinaryContent binaryContent = BinaryContentFixture.create();
            String binaryContentUrl = "http://example.com/binary/test.png";
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), "새 옷", ClothesType.ETC, List.of());
            Clothes clothes = ClothesFixture.create(request, null);
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    clothes.getName(),
                    binaryContentUrl,
                    ClothesType.ETC,
                    List.of()
            );

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(binaryContentService.upload(any(BinaryContentCreateRequest.class)))
                .willReturn(binaryContent);
            given(binaryContentUrlResolver.resolve(binaryContent.getId()))
                    .willReturn(binaryContentUrl);
            given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
            given(clothesMapper.toDto(clothes, binaryContentUrl, Map.of())).willReturn(response);

            // when
            ClothesResponse result = clothesService.createClothes(imageRequest, request);

            // then
            assertThat(result.imageUrl()).isEqualTo(binaryContentUrl);
            then(userRepository).should().findById(user.getId());
            then(binaryContentService).should().upload(imageRequest);
            then(binaryContentStorage).should().put(binaryContent.getId(), imageRequest.data());
            then(binaryContentUrlResolver).should().resolve(binaryContent.getId());
            then(clothesRepository).should().save(any(Clothes.class));
        }

        @Test
        @DisplayName("성공: 유효한 파라미터들과 여러 속성 값들이 들어올 경우 속성값이 저장되고 반환된다")
        void createClothes_Success_with_attribute() {
            // given
            User user = UserFixture.create();
            ClothesAttributeDef definition1 = ClothesAttributeDefFixture.create();
            List<ClothesAttributeValue> selectableList1 = ClothesAttributeValueFixture.createList(definition1);
            ClothesAttributeDef definition2 = ClothesAttributeDefFixture.create();
            List<ClothesAttributeValue> selectableList2 = ClothesAttributeValueFixture.createList(definition2);
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(),
                    "옷 이름",
                    ClothesType.ETC,
                    List.of(new ClothesAttributeRequest(
                                    definition1.getId(), selectableList1.get(0).getSelectableValue()),
                            new ClothesAttributeRequest(
                                    definition2.getId(), selectableList2.get(1).getSelectableValue())
                    )
            );
            Clothes clothes = ClothesFixture.create(request, null);
            Map<UUID, List<String>> expectedGrouping = Map.of(
                    definition1.getId(),
                    selectableList1.stream().map(ClothesAttributeValue::getSelectableValue).toList(),
                    definition2.getId(),
                    selectableList2.stream().map(ClothesAttributeValue::getSelectableValue).toList()
            );
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    clothes.getName(),
                    null,
                    clothes.getType(),
                    clothes.getValues().stream().map(
                            attributeValue -> new ClothesAttributeWithDefResponse(
                                    attributeValue.getAttributeDef().getId(),
                                    attributeValue.getAttributeDef().getName(),
                                    expectedGrouping.get(attributeValue.getAttributeDef().getId()),
                                    attributeValue.getSelectableValue()
                            )
                    ).toList()
            );

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(clothesAttributeValueRepository.findByAttributeDefIdAndSelectableValue(any(UUID.class), anyString()))
                    .willAnswer(invocation -> {
                        UUID defId = invocation.getArgument(0);
                        String value = invocation.getArgument(1);
                        return Optional.of(ClothesAttributeValueFixture.create(defId, value));
                    });
            given(clothesRepository.save(any(Clothes.class)))
                    .willReturn(clothes);
            given(clothesAttributeValueRepository.findByAttributeDefIdIn(
                    clothes.getValues().stream()
                            .map(value -> value.getAttributeDef().getId())
                            .toList()))
                    .willReturn(Stream.concat(selectableList1.stream(), selectableList2.stream()).toList());
            given(clothesMapper.toDto(clothes, null, expectedGrouping))
                    .willReturn(response);

            // when
            ClothesResponse result = clothesService.createClothes(null, request);

            // then
            assertThat(result.attributes()).hasSize(2);
            result.attributes().forEach(depResponse ->
                    assertThat(depResponse.selectableValue()).isEqualTo(expectedGrouping.get(depResponse.definitionId()))
            );

            then(userRepository).should().findById(user.getId());
            then(clothesAttributeValueRepository).should(times(request.attributes().size()))
                    .findByAttributeDefIdAndSelectableValue(any(UUID.class), anyString());
            then(clothesAttributeValueRepository).should().findByAttributeDefIdIn(
                    argThat(list -> list.size() == request.attributes().size() && list.containsAll(
                                    List.of(definition1.getId(), definition2.getId())
                            )
                    )
            );
            then(clothesRepository).should().save(any(Clothes.class));
            then(clothesMapper).should().toDto(clothes, null, expectedGrouping);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 유저로 요청할 경우 예외가 발생한다")
        void createClothes_Fail_NotFoundUser() {
            // given
            ClothesCreateRequest request = new ClothesCreateRequest(
                    UUID.randomUUID(), "옷 이름", ClothesType.ETC, List.of());
            BinaryContentCreateRequest imageRequest = new BinaryContentCreateRequest(
                    "test".getBytes(), "test_file", "image/png", 30L);
            given(userRepository.findById(any(UUID.class)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(imageRequest, request))
                    .isInstanceOf(UserNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
            then(clothesRepository).should(never()).save(any(Clothes.class));
        }


        @Test
        @DisplayName("실패: 존재하지 않는 옷 속성-값으로 요청할 경우 예외가 발생한다")
        void createClothes_Fail_NotFoundAttribute() {
            // given
            User user = UserFixture.create();
            ClothesAttributeDef definition = ClothesAttributeDefFixture.create();
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), "옷 이름", ClothesType.ETC,
                    List.of(new ClothesAttributeRequest(definition.getId(), "속성값"))
            );
            BinaryContentCreateRequest imageRequest = new BinaryContentCreateRequest(
                    "test".getBytes(), "test_file", "image/png", 30L);
            BinaryContent binaryContent = BinaryContentFixture.create(imageRequest);
            given(userRepository.findById(user.getId()))
                    .willReturn(Optional.of(user));
            given(binaryContentService.upload(any(BinaryContentCreateRequest.class)))
                    .willReturn(binaryContent);
            given(clothesAttributeValueRepository.findByAttributeDefIdAndSelectableValue(any(UUID.class), anyString()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(imageRequest, request))
                    .isInstanceOf(ClothesAttributeValueNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CLOTHES_ATTRIBUTE_VALUES_NOT_FOUND);
            then(clothesRepository).should(never()).save(any(Clothes.class));
        }
    }
}