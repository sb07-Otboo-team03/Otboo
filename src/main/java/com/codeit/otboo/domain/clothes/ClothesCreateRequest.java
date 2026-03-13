package com.codeit.otboo.domain.clothes;

import java.util.ArrayList;
import java.util.UUID;

public class ClothesCreateRequest {
    private UUID ownerId;
    private String name;
    private String type; // TOP, BOTTOM, DRESS, OUTER, UNDERWEAR, ACCESSORY, SHOES, SOCKS, HAT, BAG, SCARF, ETC
    private ArrayList<ClothesAttributeDto> attributes;
}
