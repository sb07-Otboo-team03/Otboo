package com.codeit.otboo.domain.clothes;

import java.util.ArrayList;
import java.util.UUID;

public class ClothesDto {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String type; //ENUM
    private ArrayList<ClothesAttributeWithDefDto> attributes;
}
