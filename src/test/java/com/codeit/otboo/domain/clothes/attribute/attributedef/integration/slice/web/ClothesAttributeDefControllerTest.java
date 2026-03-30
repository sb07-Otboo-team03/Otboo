package com.codeit.otboo.domain.clothes.attribute.attributedef.integration.slice.web;

import com.codeit.otboo.domain.clothes.attribute.attributedef.controller.ClothesAttributeDefController;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefCreateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeDefUpdateRequest;
import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.response.ClothesAttributeDefResponse;
import com.codeit.otboo.domain.clothes.attribute.attributedef.service.ClothesAttributeDefService;
import com.codeit.otboo.global.security.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClothesAttributeDefController.class,
        excludeFilters = {
@ComponentScan.Filter( type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtAuthenticationFilter.class
                )
        })
@AutoConfigureMockMvc(addFilters = false)
class ClothesAttributeDefControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ClothesAttributeDefService defService;

    @Test
    @DisplayName("속성 목록 조회 성공")
    void getValueList() throws Exception {
        // given
        List<ClothesAttributeDefResponse> response = List.of(
                new ClothesAttributeDefResponse(
                        UUID.randomUUID(),
                        "색상",
                        List.of("레드", "블랙"),
                        LocalDateTime.now()
                )
        );

        given(defService.getAllAttributeDef(any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/clothes/attribute-defs")
                        .param("keywordLike", "색")
                        .param("sortBy", "name")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("색상"))
                .andExpect(jsonPath("$[0].selectableValues[0]").value("레드"));
    }

    @Test
    @DisplayName("속성 생성 성공")
    void postDefValue_Success() throws Exception {
        // given
        ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
                "색상",
                List.of("레드", "옐로우")
        );

        ClothesAttributeDefResponse response = new ClothesAttributeDefResponse(
                UUID.randomUUID(),
                "색상",
                List.of("레드", "옐로우"),
                LocalDateTime.now()
        );

        given(defService.createAttributeDef(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/clothes/attribute-defs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("색상"))
                .andExpect(jsonPath("$.selectableValues[1]").value("옐로우"));
    }

    @Test
    @DisplayName("속성 생성 실패")
    void postDefValue_Fail() throws Exception {
    // given
    ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest(
            "",
            List.of("레드")
    );
        
    // when & then
        mockMvc.perform(post("/api/clothes/attribute-defs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("속성 수정 성공")
    void patchDefValue() throws Exception {
    // given
    UUID defId = UUID.randomUUID();

        ClothesAttributeDefUpdateRequest request =
                new ClothesAttributeDefUpdateRequest(
                        "색상수정",
                        List.of("블랙", "화이트")
                );
        ClothesAttributeDefResponse response
                = new ClothesAttributeDefResponse(
                defId,
                "색상수정",
                List.of("블랙", "화이트"),
                LocalDateTime.now()
        );

        given(defService.updateAttributeDef(eq(defId), any())).willReturn(response);

    // when & then
        mockMvc.perform(patch("/api/clothes/attribute-defs/{id}", defId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("색상수정"))
                .andExpect(jsonPath("$.selectableValues[0]").value("블랙"));
    }

    @Test
    @DisplayName("속성 삭제 성공")
    void deleteDefValue() throws Exception {
    // given
    UUID defId = UUID.randomUUID();

    willDoNothing().given(defService).deleteAttributeDef(eq(defId));

    // when & then
    mockMvc.perform(delete("/api/clothes/attribute-defs/{id}", defId))
            .andDo(print())
            .andExpect(status().isNoContent());
    }
}