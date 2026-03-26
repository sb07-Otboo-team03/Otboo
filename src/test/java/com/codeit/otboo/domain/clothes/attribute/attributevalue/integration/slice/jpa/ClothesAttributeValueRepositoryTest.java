package com.codeit.otboo.domain.clothes.attribute.attributevalue.integration.slice.jpa;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.entity.ClothesAttributeValue;
import com.codeit.otboo.domain.clothes.attribute.attributevalue.repository.ClothesAttributeValueRepository;
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
class ClothesAttributeValueRepositoryTest {

    @Autowired
    ClothesAttributeValueRepository repository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("true만 조회 가능")
    void isActiveTrue() {
    // given
        ClothesAttributeDef defColor = new ClothesAttributeDef("색상");
        em.persist(defColor);

        ClothesAttributeValue valueColor1 = ClothesAttributeValue.builder()
                .attributeDef(defColor)
                .selectableValue("그린")
                .isActive(true)
                .build();

        ClothesAttributeValue valueColor2 = ClothesAttributeValue.builder()
                .attributeDef(defColor)
                .selectableValue("블랙")
                .isActive(false)
                .build();

        em.persist(valueColor1);
        em.persist(valueColor2);

        em.flush();
        em.clear();

    // when
        List<ClothesAttributeValue> list
                = repository.findByAttributeDefIdInAndIsActiveTrue(List.of(defColor.getId()));

    // then
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("모든 값 조회")
    void searchAllValue() {
        // given
        ClothesAttributeDef defColor = new ClothesAttributeDef("색상");
        em.persist(defColor);

        ClothesAttributeValue valueColor1 = ClothesAttributeValue.builder()
                .attributeDef(defColor)
                .selectableValue("그린")
                .isActive(true)
                .build();

        ClothesAttributeValue valueColor2 = ClothesAttributeValue.builder()
                .attributeDef(defColor)
                .selectableValue("블랙")
                .isActive(false)
                .build();

        em.persist(valueColor1);
        em.persist(valueColor2);

        em.flush();
        em.clear();

        // when
        List<ClothesAttributeValue> list
                = repository.findByAttributeDefId(defColor.getId());

        // then
        assertThat(list).hasSize(2);
    }
}