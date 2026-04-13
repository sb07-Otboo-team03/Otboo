package com.codeit.otboo.domain.clothes.management.unit.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.fixture.BinaryContentFixture;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeWithDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.fixture.ClothesAttributeDefFixture;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request.ClothesAttributeRequest;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.exception.ClothesAttributeValueNotFoundException;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.fixture.ClothesAttributeValueFixture;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import com.codeit.otboo.domain.clothes.management.dto.query.ClothesSearchCondition;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.exception.ClothesNotFoundException;
import com.codeit.otboo.domain.clothes.management.exception.DuplicateClothesAttributeDefinitionException;
import com.codeit.otboo.domain.clothes.management.fixture.ClothesFixture;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesQueryMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepositoryCustomImpl;
import com.codeit.otboo.domain.clothes.management.scraper.Scraper;
import com.codeit.otboo.domain.clothes.management.service.ClothesServiceImpl;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.fixture.UserFixture;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.exception.ErrorCode;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

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
    BinaryContentService binaryContentService;

    @Mock
    BinaryContentUrlResolver binaryContentUrlResolver;

    @Mock
    ClothesMapper clothesMapper;

    @Mock
    ClothesQueryMapper clothesQueryMapper;

    @Mock
    ClothesRepositoryCustomImpl clothesRepositoryCustom;

    @Mock
    Scraper scraper;

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
                    user.getId(), "새 옷", ClothesType.ETC, List.of(), null);
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
            ClothesResponse result = clothesService.createClothes(request);

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
            BinaryContent binaryContent = BinaryContentFixture.create();
            String binaryContentUrl = "http://example.com/binary/test.png";
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), "새 옷", ClothesType.ETC, List.of(), binaryContent.getId());
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
            given(binaryContentService.getById(binaryContent.getId())).willReturn(binaryContent);
            given(binaryContentUrlResolver.resolve(binaryContent.getId()))
                    .willReturn(binaryContentUrl);
            given(clothesRepository.save(any(Clothes.class))).willReturn(clothes);
            given(clothesMapper.toDto(clothes, binaryContentUrl, Map.of())).willReturn(response);

            // when
            ClothesResponse result = clothesService.createClothes(request);

            // then
            assertThat(result.imageUrl()).isEqualTo(binaryContentUrl);
            then(userRepository).should().findById(user.getId());
            then(binaryContentService).should().getById(binaryContent.getId());
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
                    ),
                    null
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
            given(clothesRepository.save(any(Clothes.class)))
                    .willReturn(clothes);
            given(clothesAttributeValueRepository.findByAttributeDefIdIn(anyList()))
                    .willReturn(Stream.concat(selectableList1.stream(), selectableList2.stream()).toList());
            given(clothesMapper.toDto(clothes, null, expectedGrouping))
                    .willReturn(response);

            // when
            ClothesResponse result = clothesService.createClothes(request);

            // then
            assertThat(result.attributes()).hasSize(2);
            result.attributes().forEach(depResponse ->
                    assertThat(depResponse.selectableValue()).isEqualTo(expectedGrouping.get(depResponse.definitionId()))
            );

            then(userRepository).should().findById(user.getId());
            then(clothesAttributeValueRepository).should(times(1)).findByAttributeDefIdIn(
                    argThat(list -> list.size() == request.attributes().size() &&
                            list.containsAll(List.of(definition1.getId(), definition2.getId()))
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
                    UUID.randomUUID(), "옷 이름", ClothesType.ETC, List.of(), null);
            given(userRepository.findById(any(UUID.class)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(request))
                    .isInstanceOf(UserNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
            then(clothesRepository).should(never()).save(any(Clothes.class));
        }

        @Test
        @DisplayName("실패: 옷 하나의 속성에 여러 개의 값이 요청으로 경우 예외가 발생한다")
        void createClothes_Fail_MultipleAttributeValues() {
            // given
            User user = UserFixture.create();
            ClothesAttributeDef definition = ClothesAttributeDefFixture.create();
            ClothesCreateRequest request = new ClothesCreateRequest(
                    user.getId(), "옷 이름", ClothesType.ETC,
                    List.of(
                            new ClothesAttributeRequest(definition.getId(), "기존값"),
                            new ClothesAttributeRequest(definition.getId(), "중복값")
                    ),
                    null
            );
            given(userRepository.findById(user.getId()))
                    .willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(request))
                    .isInstanceOf(DuplicateClothesAttributeDefinitionException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CLOTHES_DUPLICATED_VALUE);
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
                    List.of(new ClothesAttributeRequest(definition.getId(), "속성값")),
                    null
            );
            given(userRepository.findById(user.getId()))
                    .willReturn(Optional.of(user));

            // when & then
            assertThatThrownBy(() -> clothesService.createClothes(request))
                    .isInstanceOf(ClothesAttributeValueNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CLOTHES_ATTRIBUTE_VALUES_NOT_FOUND);
            then(clothesRepository).should(never()).save(any(Clothes.class));
        }
    }

    @Nested
    @DisplayName("옷 삭제")
    class ClothesDelete {
        @Test
        @DisplayName("성공: 이미지가 없는 옷의 ID가 들어올 경우 옷이 삭제된다")
        void deleteClothes_Success() {
            // given
            Clothes clothes = ClothesFixture.create(
                    "옷",
                    ClothesType.ETC,
                    UserFixture.create(),
                    null,
                    List.of()
            );
            given(clothesRepository.findById(clothes.getId()))
                    .willReturn(Optional.of(clothes));

            // when
            clothesService.deleteClothes(clothes.getId());

            // then
            then(clothesRepository).should().deleteById(clothes.getId());
            then(binaryContentService).should(never()).delete(any(UUID.class));

        }

        @Test
        @DisplayName("성공: 옷의 ID 가 들어올 경우 옷과 옷의 이미지가 삭제된다")
        void deleteClothes_Success_with_image() {
            // given
            Clothes clothes = ClothesFixture.create();
            given(clothesRepository.findById(clothes.getId()))
                    .willReturn(Optional.of(clothes));

            // when
            clothesService.deleteClothes(clothes.getId());

            // then
            then(clothesRepository).should().deleteById(clothes.getId());
            then(binaryContentService).should().delete(any(UUID.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID가 들어올 경우 예외가 발생한다")
        void deleteClothes_Fail_NotFound() {
            // given
            UUID clothesId = UUID.randomUUID();
            given(clothesRepository.findById(clothesId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> clothesService.deleteClothes(clothesId))
                    .isInstanceOf(ClothesNotFoundException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CLOTHES_NOT_FOUND);
            then(clothesRepository).should(never()).deleteById(clothesId);
        }
    }

    @Nested
    @DisplayName("옷 수정")
    class ClothesUpdate {
        @Test
        @DisplayName("성공: 유효한 옷ID, name, type가 들어올 경우 옷 속성이 변경된다")
        void update_clothes_Success(){
            // given
            Clothes clothes = ClothesFixture.create(
                    "옷", ClothesType.ETC, UserFixture.create(), null, List.of());
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    clothes.getName(), clothes.getType(), List.of(), null);
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    request.name(),
                    null,
                    request.type(),
                    List.of()
            );
            given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
            given(clothesMapper.toDto(clothes, null, Map.of())).willReturn(response);

            // when
            ClothesResponse result = clothesService.updateClothes(clothes.getId(), request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo(request.name());
            assertThat(result.type()).isEqualTo(request.type());
            assertThat(result.imageUrl()).isNull();
            assertThat(result.attributes()).isEmpty();

            then(clothesRepository).should().findById(clothes.getId());
            then(clothesMapper).should().toDto(clothes, null, Map.of());
            then(binaryContentService).should(never()).getById(any(UUID.class));
            then(binaryContentService).should(never()).delete(any(UUID.class));
            then(binaryContentUrlResolver).should(never()).resolve(any(UUID.class));
        }

        @Test
        @DisplayName("성공: 이미지가 없는 옷에 이미지를 업로드 할 경우 새로운 이미지로 업로드 된다.")
        void update_clothes_no_image_Success_with_image(){
            // given
            BinaryContent binaryContent = BinaryContentFixture.create();
            Clothes clothes = ClothesFixture.create(
                    "옷", ClothesType.ETC, UserFixture.create(), null, List.of());
            UUID clothesId = UUID.randomUUID();
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    clothes.getName(), clothes.getType(), List.of(), binaryContent.getId());
            String binaryContentUrl = "http://example.com/binary/test.png";
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    request.name(),
                    binaryContentUrl,
                    request.type(),
                    List.of()
            );
            given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
            given(binaryContentService.getById(binaryContent.getId())).willReturn(binaryContent);
            given(binaryContentUrlResolver.resolve(binaryContent.getId())).willReturn(binaryContentUrl);
            given(clothesMapper.toDto(clothes, binaryContentUrl, Map.of())).willReturn(response);

            // when
            ClothesResponse result = clothesService.updateClothes(clothes.getId(), request);

            // then
            assertThat(result.imageUrl()).isNotNull();
            assertThat(result.attributes()).isEmpty();

            then(clothesRepository).should().findById(clothes.getId());
            then(clothesMapper).should().toDto(clothes, binaryContentUrl, Map.of());
            then(binaryContentService).should().getById(any(UUID.class));
            then(binaryContentService).should(never()).delete(any(UUID.class));
            then(binaryContentUrlResolver).should().resolve(any(UUID.class));
        }

        @Test
        @DisplayName("성공: 이미지가 있는 옷에 이미지를 업로드 하지 않을 경우 기존 이미지가 유지된다.")
        void update_clothes_no_image_Success_keep_image(){
            // given
            Clothes clothes = ClothesFixture.create(
                    "옷", ClothesType.ETC, UserFixture.create(), BinaryContentFixture.create(), List.of());
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    clothes.getName(), clothes.getType(), List.of(), null);
            String binaryContentUrl = "http://example.com/binary/test.png";
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    request.name(),
                    binaryContentUrl,
                    request.type(),
                    List.of()
            );
            given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
            given(binaryContentUrlResolver.resolve(clothes.getBinaryContent().getId())).willReturn(binaryContentUrl);
            given(clothesMapper.toDto(clothes, binaryContentUrl, Map.of())).willReturn(response);

            // when
            ClothesResponse result = clothesService.updateClothes(clothes.getId(), request);

            // then
            assertThat(result.imageUrl()).isNotNull();
            assertThat(result.attributes()).isEmpty();

            then(clothesRepository).should().findById(clothes.getId());
            then(clothesMapper).should().toDto(clothes, binaryContentUrl, Map.of());
            then(binaryContentService).should(never()).getById(any(UUID.class));
            then(binaryContentService).should(never()).delete(any(UUID.class));
            then(binaryContentUrlResolver).should().resolve(any(UUID.class));
        }

        @Test
        @DisplayName("""
            성공: 이미지가 있는 옷에 이미지가 업로드될 경우
            기존 이미지가 삭제되고 요청 이미지로 업로드 된다
        """)
        void update_clothes_had_image_Success_with_image(){
            // given
            Clothes clothes = ClothesFixture.create(
                    "옷", ClothesType.ETC, UserFixture.create(), BinaryContentFixture.create(), List.of());
            UUID binaryContentId = UUID.randomUUID();
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    clothes.getName(), clothes.getType(), List.of(), binaryContentId);
            BinaryContentCreateRequest imageRequest = new BinaryContentCreateRequest(
                    "test".getBytes(), "test_file", "image/png", 30L);
            BinaryContent binaryContent = BinaryContentFixture.create(imageRequest);
            String binaryContentUrl = "http://example.com/binary/test.png";
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(),
                    clothes.getOwner().getId(),
                    request.name(),
                    binaryContentUrl,
                    request.type(),
                    List.of()
            );
            given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
            given(binaryContentService.getById(binaryContentId)).willReturn(binaryContent);
            given(binaryContentUrlResolver.resolve(binaryContent.getId())).willReturn(binaryContentUrl);
            given(clothesMapper.toDto(clothes, binaryContentUrl, Map.of())).willReturn(response);

            // when
            ClothesResponse result = clothesService.updateClothes(clothes.getId(), request);

            // then
            assertThat(result.imageUrl()).isNotNull();
            assertThat(result.attributes()).isEmpty();

            then(clothesRepository).should().findById(clothes.getId());
            then(clothesMapper).should().toDto(clothes, binaryContentUrl, Map.of());
            then(binaryContentService).should().getById(any(UUID.class));
            then(binaryContentService).should().delete(any(UUID.class));
            then(binaryContentUrlResolver).should().resolve(any(UUID.class));
        }

        @Test
        @DisplayName("성공: 유효한 UUID와 name, type, attributes가 들어올 경우 옷 속성이 변경된다")
        void update_clothes_Success_with_attribute(){
            // given
            User user = UserFixture.create();
            Clothes clothes = ClothesFixture.create(
                    "옷",
                    ClothesType.ETC,
                    UserFixture.create(),
                    null,
                    List.of(
                            ClothesAttributeValueFixture.create(UUID.randomUUID(), "선택값1"),
                            ClothesAttributeValueFixture.create(UUID.randomUUID(), "선택값2"),
                            ClothesAttributeValueFixture.create(UUID.randomUUID(), "선택값3")
                    )
            );
            ClothesAttributeDef definition1 = ClothesAttributeDefFixture.create();
            List<ClothesAttributeValue> selectableList1 = ClothesAttributeValueFixture.createList(definition1);
            ClothesAttributeDef definition2 = ClothesAttributeDefFixture.create();
            List<ClothesAttributeValue> selectableList2 = ClothesAttributeValueFixture.createList(definition2);
            ClothesUpdateRequest request = new ClothesUpdateRequest(
                    "옷 이름",
                    ClothesType.ETC,
                    List.of(new ClothesAttributeRequest(
                                    definition1.getId(), selectableList1.get(0).getSelectableValue()),
                            new ClothesAttributeRequest(
                                    definition2.getId(), selectableList2.get(1).getSelectableValue())
                    ),
                    null
            );
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
                    request.attributes().stream().map(
                            attributeValueRequest -> new ClothesAttributeWithDefResponse(
                                    attributeValueRequest.definitionId(),
                                    "속성",
                                    expectedGrouping.get(attributeValueRequest.definitionId()),
                                    attributeValueRequest.value()
                            )
                    ).toList()
            );

            given(clothesRepository.findById(clothes.getId())).willReturn(Optional.of(clothes));
            given(clothesAttributeValueRepository.findByAttributeDefIdIn(anyList()))
                    .willReturn(Stream.concat(selectableList1.stream(), selectableList2.stream()).toList());
            given(clothesMapper.toDto(clothes, null, expectedGrouping))
                    .willReturn(response);

            // when
            ClothesResponse result = clothesService.updateClothes(clothes.getId(), request);

            // then
            assertThat(result.attributes()).hasSize(2);
            result.attributes().forEach(depResponse ->
                    assertThat(depResponse.selectableValue()).isEqualTo(expectedGrouping.get(depResponse.definitionId()))
            );
            then(clothesAttributeValueRepository).should(times(1)).findByAttributeDefIdIn(
                    argThat(list -> list.size() == request.attributes().size() &&
                            list.containsAll(List.of(definition1.getId(), definition2.getId()))
                    )
            );
            then(clothesMapper).should().toDto(clothes, null, expectedGrouping);
        }
    }

    @Nested
    @DisplayName("옷 목록 조회")
    class GetClothesList {
        @Test
        @DisplayName("성공: totalCount가 0이면 바로 빈 리스트를 가진 Slice 로 응답한다.")
        void success_getClothesList_empty(){
            // given
            UUID ownerId = UUID.randomUUID();
            ClothesCursorPageRequest request = new ClothesCursorPageRequest(
                    null, null, null, null, ownerId);

            given(clothesRepositoryCustom.totalCount(ownerId, null)).willReturn(0L);

            // when
            CursorResponse<ClothesResponse> result = clothesService.getClothesListByOwnerId(request);

            // then
            assertThat(result.totalCount()).isEqualTo(0L);
            assertThat(result.data()).isEmpty();
            assertThat(result.hasNext()).isFalse();

            then(clothesRepositoryCustom).should(times(1)).totalCount(ownerId, null);
            then(clothesRepositoryCustom).should(never()).findMyClothesList(any(ClothesSearchCondition.class));
        }

        @Test
        @DisplayName("성공: 모든 옷의 속성이 존재하지 않는다면 레포지토리 조회를 하지 않고 빈 Map 을 반환한다.")
        void success_getClothesList_no_attribute(){
            // given
            User owner = UserFixture.create();
            ClothesCursorPageRequest request = new ClothesCursorPageRequest(
                    null, null, 20, null, owner.getId());
            ClothesSearchCondition query = new ClothesSearchCondition(
                    null, null, 20, null, owner.getId());
            Clothes clothes = ClothesFixture.create(
                    "옷", null, owner, null, List.of());
            Slice<Clothes> slice = new SliceImpl<>(
                    List.of(clothes), PageRequest.of(0, 20), false);

            given(clothesRepositoryCustom.totalCount(owner.getId(), null)).willReturn(1L);
            given(clothesQueryMapper.toQuery(request)).willReturn(query);
            given(clothesRepositoryCustom.findMyClothesList(query)).willReturn(slice);

            // when
            clothesService.getClothesListByOwnerId(request);

            // then
            then(clothesAttributeValueRepository).should(never()).findByAttributeDefIdIn(anyList());
        }

        @Test
        @DisplayName("성공: 옷 목록 조회를 하면 repository의 조회가 호출된다.")
        void success_getMyClotheList(){
            // given
            User owner = UserFixture.create();
            ClothesCursorPageRequest request = new ClothesCursorPageRequest(
                    null, null, 20, null, owner.getId());
            ClothesSearchCondition query = new ClothesSearchCondition(
                    null, null, 20, null, owner.getId());
            BinaryContent binaryContent = BinaryContentFixture.create();
            ClothesAttributeDef definition = ClothesAttributeDefFixture.create();
            ClothesAttributeValue attributeValue = ClothesAttributeValueFixture.create(definition, "속성값");
            ClothesAttributeWithDefResponse defResponse = new ClothesAttributeWithDefResponse(
                    definition.getId(),
                    definition.getName(),
                    List.of(attributeValue.getSelectableValue()),
                    attributeValue.getSelectableValue()
            );
            Clothes clothes = ClothesFixture.create(
                    "옷", null, owner, binaryContent, List.of(attributeValue));
            Slice<Clothes> slice = new SliceImpl<>(
                    List.of(clothes), PageRequest.of(0, 20), false);
            String imageUrl = "http://example.com/binary/test.png";
            ClothesResponse response = new ClothesResponse(
                    clothes.getId(), owner.getId(), "옷", imageUrl, null, List.of(defResponse));
            Map<UUID, List<String>> groupingAttribute =  Map.of(
                    definition.getId(),
                    List.of(attributeValue.getSelectableValue())
            );

            given(clothesRepositoryCustom.totalCount(owner.getId(), null)).willReturn(1L);
            given(clothesQueryMapper.toQuery(request)).willReturn(query);
            given(clothesRepositoryCustom.findMyClothesList(query)).willReturn(slice);
            given(clothesAttributeValueRepository.findByAttributeDefIdIn(List.of(definition.getId())))
                    .willReturn(List.of(attributeValue));
            given(binaryContentUrlResolver.resolve(binaryContent.getId())).willReturn(imageUrl);
            given(clothesMapper.toDto(clothes, imageUrl, groupingAttribute))
                    .willReturn(response);

            // when
            CursorResponse<ClothesResponse> result = clothesService.getClothesListByOwnerId(request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.hasNext()).isFalse();

            then(clothesQueryMapper).should().toQuery(request);
            then(clothesRepositoryCustom).should(times(1))
                    .totalCount(owner.getId(), null);
            then(clothesRepositoryCustom).should(times(1))
                    .findMyClothesList(query);
            then(binaryContentUrlResolver).should().resolve(binaryContent.getId());
            then(clothesMapper).should().toDto(clothes, imageUrl, groupingAttribute);
        }
    }

    @Nested
    @DisplayName("옷 주인 검사")
    class IsOwner{
        @Test
        @DisplayName("성공: 옷 주인 검사를 하면 DB에서 옷ID & 주인ID 있는지 확인하고 있으면 true를 반환한다.")
        void success_isOwner(){
            // given
            User owner = UserFixture.create();
            Clothes clothes = ClothesFixture.create(
                    "옷", null, owner, null, List.of());
            given(clothesRepository.existsByIdAndOwnerId(clothes.getId(), owner.getId()))
                    .willReturn(true);

            // when
            boolean result = clothesService.isOwner(clothes.getId(), owner.getId());

            // then
            assertThat(result).isTrue();
            then(clothesRepository).should(times(1))
                    .existsByIdAndOwnerId(clothes.getId(), owner.getId());
        }
    }
}