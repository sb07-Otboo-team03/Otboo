package com.codeit.otboo.domain.clothes.management.service;

import com.codeit.otboo.domain.binarycontent.dto.request.BinaryContentCreateRequest;
import com.codeit.otboo.domain.binarycontent.entity.BinaryContent;
import com.codeit.otboo.domain.binarycontent.resolver.BinaryContentUrlResolver;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentService;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.dto.request.ClothesAttributeRequest;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.exception.ClothesAttributeValueNotFoundException;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
import com.codeit.otboo.domain.clothes.management.dto.query.ClothesCursorQuery;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCreateRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesUpdateRequest;
import com.codeit.otboo.domain.clothes.management.dto.response.ClothesResponse;
import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.clothes.management.exception.ClothesNotFoundException;
import com.codeit.otboo.domain.clothes.management.exception.DuplicateClothesAttributeDefinitionException;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesMapper;
import com.codeit.otboo.domain.clothes.management.mapper.ClothesQueryMapper;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepository;
import com.codeit.otboo.domain.clothes.management.repository.ClothesRepositoryCustomImpl;
import com.codeit.otboo.domain.clothes.management.vo.ClothesAttributeSelection;
import com.codeit.otboo.domain.clothes.management.vo.ClothesAttributeValueKey;
import com.codeit.otboo.domain.clothes.management.vo.ClothesSortBy;
import com.codeit.otboo.domain.clothes.management.vo.ClothesNextCursor;
import com.codeit.otboo.domain.user.entity.User;
import com.codeit.otboo.domain.user.exception.UserNotFoundException;
import com.codeit.otboo.domain.user.repository.UserRepository;
import com.codeit.otboo.global.slice.dto.CursorResponse;
import com.codeit.otboo.global.slice.dto.SortDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final BinaryContentService binaryContentService;
    private final BinaryContentUrlResolver binaryContentUrlResolver;
    private final ClothesMapper clothesMapper;
    private final ClothesQueryMapper clothesQueryMapper;
    private final ClothesRepositoryCustomImpl clothesRepositoryCustom;

    @Override
    @Transactional
    @PreAuthorize("#request.ownerId() == authentication.principal.userResponse.id()")
    public ClothesResponse createClothes(
            BinaryContentCreateRequest imageRequest,
            ClothesCreateRequest request
    ){
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(UserNotFoundException::new);
        BinaryContent binaryContent = null;
        if(imageRequest != null){
            binaryContent = binaryContentService.upload(imageRequest);
        }
        ClothesAttributeSelection selection = getClothesAttributeValues(request.attributes());
        Clothes savedClothes = clothesRepository.save(
            new Clothes(request.name(), request.type(), owner, binaryContent, selection.selectedValues())
        );
        return clothesMapper.toDto(
                savedClothes,
                resolveImageUrl(binaryContent),
                groupSelectableValuesByAttributeId(selection.allSelectableValues())
        );
    }

    @Override
    @Transactional
    @PreAuthorize("@clothesService.isOwner(#clothesId, authentication.principal.userResponse.id())")
    public ClothesResponse updateClothes(
            UUID clothesId, BinaryContentCreateRequest imageRequest, ClothesUpdateRequest request) {
        Clothes clothes = getById(clothesId);
        BinaryContent oldBinaryContent = clothes.getBinaryContent();
        BinaryContent newBinaryContent;
        BinaryContent binaryContent = oldBinaryContent;

        if(imageRequest != null){
            if(oldBinaryContent != null){
                binaryContentService.delete(oldBinaryContent.getId());
            }
            newBinaryContent = binaryContentService.upload(imageRequest);
            binaryContent = newBinaryContent;
        }

        ClothesAttributeSelection clothesAttributeSelection = getClothesAttributeValues(request.attributes());
        clothes.updateClothes(
            request.name(), request.type(), binaryContent, clothesAttributeSelection.selectedValues()
        );

        return clothesMapper.toDto(
                clothes,
                resolveImageUrl(binaryContent),
                groupSelectableValuesByAttributeId(clothesAttributeSelection.allSelectableValues())
        );
    }

    // 옷 속성값 requestDTO 들을 받아 DB에 있는지 확인하고 있으면 선택된 속성-값들과 선택 가능한 속성-값 리스트를 반환하는 메소드
    private ClothesAttributeSelection getClothesAttributeValues(List<ClothesAttributeRequest> requests){
        if(requests.isEmpty()) return new ClothesAttributeSelection(List.of(), List.of());
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
        List<ClothesAttributeValue> selectedValueList =  requests.stream()
                .map(request -> getAttributeValueOrThrowByRequest(
                        request, selectableAttributeValueMap
                )).collect(Collectors.toList());

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

    @Override
    @Transactional
    @PreAuthorize("@clothesService.isOwner(#clothesId, authentication.principal.userResponse.id())")
    public void deleteClothes(UUID clothesId){
        Clothes clothes = getById(clothesId);
        BinaryContent binaryContent = clothes.getBinaryContent();
        if(binaryContent != null){
            binaryContentService.delete(clothes.getBinaryContent().getId());
        }
        clothesRepository.deleteById(clothesId);
    }

    @Override
    @PreAuthorize("#request.ownerId() == authentication.principal.userResponse.id()")
    public CursorResponse<ClothesResponse> getMyClothesList(ClothesCursorPageRequest request) {
        long totalCount = clothesRepositoryCustom.totalCount(
            request.ownerId(), ClothesType.fromString(request.type()));
        String sortBy = ClothesSortBy.CREATED_AT.getValue();
        SortDirection direction = SortDirection.DESCENDING;
        if(totalCount == 0){
            return new CursorResponse<>(
                List.of(), null, null, false, totalCount, sortBy, direction
            );
        }
        ClothesCursorQuery query = clothesQueryMapper.toQuery(request);
        Slice<Clothes> slice = clothesRepositoryCustom.findMyClothesList(query);
        List<Clothes> sliceContent= slice.getContent();
        Map<UUID, List<String>> listAllSelectableGrouping = getListAllSelectableGrouping(sliceContent);
        List<ClothesResponse> content = toClothesResponseList(sliceContent, listAllSelectableGrouping);
        ClothesNextCursor clothesNextCursor = ClothesNextCursor.from(slice);

        return new CursorResponse<>(
                content,
                clothesNextCursor.getCursor(),
                clothesNextCursor.getAfter(),
                slice.hasNext(),
                totalCount,
                sortBy,
                direction
        );
    }

    // Clothes -> ClothesResponse : ClothesList의 모든 속성을 모아둔 Map에서 해당 옷이 가진 속성과 선택 가능값만 선택
    private List<ClothesResponse> toClothesResponseList(
            List<Clothes> clothesList,
            Map<UUID, List<String>> clothesListSelectableGrouping){
        if(clothesListSelectableGrouping.isEmpty()) return List.of();
        return clothesList.stream()
                .map(clothes -> clothesMapper.toDto(
                        clothes,
                        resolveImageUrl(clothes.getBinaryContent()),
                        clothes.getValues().stream()
                                .map(attributeValue -> attributeValue.getAttributeDef().getId())
                                .collect(Collectors.toMap(id -> id, clothesListSelectableGrouping::get))
                )).toList();
    }

    // 옷 리스트의 아이디를 전부 모아서 db 에서 한번에 조회한 뒤, Map<속성ID, List<속성 선택 가능값>>로 반환
    private Map<UUID, List<String>> getListAllSelectableGrouping(List<Clothes> clothesList){
        Set<UUID> clothesDefIdList = clothesList.stream().map(
                clothes -> clothes.getValues().stream().map(
                        attributeValue -> attributeValue.getAttributeDef().getId()).toList()
        ).flatMap(List::stream).collect(Collectors.toSet());
        if(clothesDefIdList.isEmpty()){
            return Map.of();
        }
        List<ClothesAttributeValue> allSelectableValues = clothesAttributeValueRepository.findByAttributeDefIdIn(
                new ArrayList<>(clothesDefIdList));
        return groupSelectableValuesByAttributeId(allSelectableValues);
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

    private String resolveImageUrl(BinaryContent binaryContent){
        if(binaryContent == null) return null;
        return binaryContentUrlResolver.resolve(binaryContent.getId());
    }

    private Clothes getById(UUID clothesId){
        return clothesRepository.findById(clothesId).orElseThrow(
                () -> new ClothesNotFoundException(clothesId));
    }

    // PreAuthorize 에서 권한 검사를 위해 작성자와 같은지 확인
    public boolean isOwner(UUID clothesId, UUID ownerId){
        return clothesRepository.existsByIdAndOwnerId(clothesId, ownerId);
    }
}