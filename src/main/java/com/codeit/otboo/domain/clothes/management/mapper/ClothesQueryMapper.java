package com.codeit.otboo.domain.clothes.management.mapper;

import com.codeit.otboo.domain.clothes.management.dto.query.ClothesCursorQuery;
import com.codeit.otboo.domain.clothes.management.dto.request.ClothesCursorPageRequest;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ClothesQueryMapper {
    public ClothesCursorQuery toQuery(@NonNull ClothesCursorPageRequest request){
        LocalDateTime cursor = request.cursor() == null ? null : LocalDateTime.parse(request.cursor());
        UUID idAfter = request.after() == null ? null : UUID.fromString(request.after());
        ClothesType type = request.type() == null ? null : ClothesType.fromString(request.type());

        return new ClothesCursorQuery(
                cursor,
                idAfter,
                request.limit(),
                type,
                request.ownerId()
        );
    }
}
