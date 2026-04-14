package com.codeit.otboo.domain.clothes.attribute.attributedef.integration.slice.jpa;

import com.codeit.otboo.domain.clothes.attribute.attributedef.dto.request.ClothesAttributeSearchCondition;
import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.repository.ClothesAttributeDefRepository;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.global.config.JpaAuditingConfig;
import com.codeit.otboo.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
class ClothesAttributeDefCustomRepositoryTest {

    @Autowired
    ClothesAttributeDefRepository repository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("속성명 검색")
    void searchDef() {
        // given
        ClothesAttributeDef defColor = new ClothesAttributeDef("색상");
        em.persist(defColor);
        ClothesAttributeDef defSize = new ClothesAttributeDef("사이즈");
        em.persist(defSize);

        ClothesAttributeValue valueColor1 = ClothesAttributeValue.builder()
                .selectableValue("레드")
                .isActive(true)
                .attributeDef(defColor)
                .build();

        ClothesAttributeValue valueColor2 = ClothesAttributeValue.builder()
                .selectableValue("그린")
                .isActive(true)
                .attributeDef(defColor)
                .build();

        ClothesAttributeValue valueSize1 = ClothesAttributeValue.builder()
                .selectableValue("L")
                .isActive(true)
                .attributeDef(defSize)
                .build();

        em.persist(valueColor1);
        em.persist(valueColor2);
        em.persist(valueSize1);

        em.flush();
        em.clear();

        // when
        List<ClothesAttributeDef> result = repository.searchAttributes(
                new ClothesAttributeSearchCondition("name", "ASC", "색상")
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("색상");
    }

    @Test
    @DisplayName("속성값 검색")
    void searchValue() {
        // given
        ClothesAttributeDef defLength = new ClothesAttributeDef("길이");
        em.persist(defLength);
        ClothesAttributeDef defSize = new ClothesAttributeDef("사이즈");
        em.persist(defSize);

        ClothesAttributeValue valueColor1 = ClothesAttributeValue.builder()
                .selectableValue("~80")
                .isActive(true)
                .attributeDef(defLength)
                .build();

        ClothesAttributeValue valueColor2 = ClothesAttributeValue.builder()
                .selectableValue("~90")
                .isActive(true)
                .attributeDef(defLength)
                .build();

        ClothesAttributeValue valueSize1 = ClothesAttributeValue.builder()
                .selectableValue("95")
                .isActive(true)
                .attributeDef(defSize)
                .build();

        em.persist(valueColor1);
        em.persist(valueColor2);
        em.persist(valueSize1);

        em.flush();
        em.clear();

        // when
        List<ClothesAttributeDef> result = repository.searchAttributes(
                new ClothesAttributeSearchCondition("name", "ASC", "9")
        );

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("false는 검색되지 않음")
    void searchNotFalse() {
        // given
        ClothesAttributeDef defColor = new ClothesAttributeDef("색상");
        em.persist(defColor);

        ClothesAttributeValue valueColor1 = ClothesAttributeValue.builder()
                .attributeDef(defColor)
                .selectableValue("그린")
                .isActive(false)
                .build();
        em.persist(valueColor1);

        em.flush();
        em.clear();

        // when
        List<ClothesAttributeDef> result = repository.searchAttributes(
                new ClothesAttributeSearchCondition("name", "ASC", "그린")
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("중복없이 조회")
    void searchDistinct() {
        // given
        ClothesAttributeDef defColor = new ClothesAttributeDef("색상");
        em.persist(defColor);

        ClothesAttributeValue valueColor1 = ClothesAttributeValue.builder()
                .attributeDef(defColor)
                .selectableValue("레드")
                .isActive(true)
                .build();
        ClothesAttributeValue valueColor2 = ClothesAttributeValue.builder()
                .attributeDef(defColor)
                .selectableValue("그린")
                .isActive(true)
                .build();
        em.persist(valueColor1);
        em.persist(valueColor2);

        em.flush();
        em.clear();

        // when
        List<ClothesAttributeDef> result = repository.searchAttributes(
                new ClothesAttributeSearchCondition("name", "ASC", "색")
        );

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("이름기준 오름차 정렬")
    void searchNameAsc() {
        // given
        ClothesAttributeDef defColor = new ClothesAttributeDef("색상");
        em.persist(defColor);
        ClothesAttributeDef defSize = new ClothesAttributeDef("사이즈");
        em.persist(defSize);

        ClothesAttributeValue valueColor1 = ClothesAttributeValue.builder()
                .selectableValue("레드")
                .isActive(true)
                .attributeDef(defColor)
                .build();

        ClothesAttributeValue valueSize1 = ClothesAttributeValue.builder()
                .selectableValue("L")
                .isActive(true)
                .attributeDef(defSize)
                .build();

        em.persist(valueColor1);
        em.persist(valueSize1);

        em.flush();
        em.clear();

        // when
        List<ClothesAttributeDef> result = repository.searchAttributes(
                new ClothesAttributeSearchCondition("name", "ASC", null)
        );

        // then
        assertThat(result)
                .extracting("name")
                .containsExactly("사이즈", "색상");
    }
}