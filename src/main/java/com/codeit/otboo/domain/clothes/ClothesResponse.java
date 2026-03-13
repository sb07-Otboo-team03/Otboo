package com.codeit.otboo.domain.clothes;

import java.util.ArrayList;
import java.util.UUID;

public record ClothesResponse (
    UUID id,
    UUID ownerId,
    String name,
    String imageUrl,
    String type, //ENUM
    ArrayList<ClothesAttributeWithDefResponse> attributes
){}
