package com.codeit.otboo.domain.clothes;

import java.util.ArrayList;
import java.util.UUID;

public record ClothesCreateRequest (
    UUID ownerId,
    String name,
    String type, // TOP, BOTTOM, DRESS, OUTER, UNDERWEAR, ACCESSORY, SHOES, SOCKS, HAT, BAG, SCARF, ETC
    ArrayList<ClothesAttributeResponse> attributes
){}
