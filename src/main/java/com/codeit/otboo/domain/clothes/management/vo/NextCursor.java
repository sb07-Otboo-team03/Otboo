package com.codeit.otboo.domain.clothes.management.vo;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.UUID;

@Getter
public class NextCursor {
    private final String cursor;
    private final UUID after;

    private NextCursor(String cursor, UUID after) {
        this.cursor = cursor;
        this.after = after;
    }

    public static NextCursor from(Slice<Clothes> slice) {
        if (!slice.hasNext() || slice.getContent().isEmpty()) {
            return new NextCursor(null, null);
        }

        Clothes last = slice.getContent().get(slice.getContent().size() - 1);
        String cursor = last.getCreatedAt().toString();
        UUID after = last.getId();
        return new NextCursor(cursor, after);
    }
}
