package com.codeit.otboo.domain.clothes.management.vo;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ClothesNextCursor {
    private final String cursor;
    private final UUID after;

    public static ClothesNextCursor from(Slice<Clothes> slice) {
        if (slice.getContent().isEmpty() || !slice.hasNext()) {
            return new ClothesNextCursor(null, null);
        }

        Clothes last = slice.getContent().get(slice.getContent().size() - 1);
        String cursor = last.getCreatedAt().toString();
        UUID after = last.getId();
        return new ClothesNextCursor(cursor, after);
    }
}
