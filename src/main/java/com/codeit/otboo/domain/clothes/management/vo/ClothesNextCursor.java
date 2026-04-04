package com.codeit.otboo.domain.clothes.management.vo;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.UUID;

@Getter
public class ClothesNextCursor {
    private final String cursor;
    private final UUID after;

    private ClothesNextCursor(String cursor, UUID after) {
        this.cursor = cursor;
        this.after = after;
    }

    public static ClothesNextCursor from(Slice<Clothes> slice) {
        if (!slice.hasNext() || slice.getContent().isEmpty()) {
            return new ClothesNextCursor(null, null);
        }

        Clothes last = slice.getContent().get(slice.getContent().size() - 1);
        String cursor = last.getCreatedAt().toString();
        UUID after = last.getId();
        return new ClothesNextCursor(cursor, after);
    }
}
