package com.codeit.otboo.domain.clothes.attribute.attributedef.integration.slice.jpa;

import com.codeit.otboo.domain.clothes.attribute.attributedef.entity.ClothesAttributeDef;
import com.codeit.otboo.domain.clothes.attribute.attributedef.repository.ClothesAttributeDefRepository;
import com.codeit.otboo.global.config.JpaAuditingConfig;
import com.codeit.otboo.global.config.QueryDslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({JpaAuditingConfig.class, QueryDslConfig.class})
class ClothesAttributeDefRepositoryTest {

    @Autowired
    private ClothesAttributeDefRepository repository;

    @Test
    @DisplayName("대소문자 상관없이 존재여부 확인")
    void existsByNameIgnoreCase() {
    // given
    ClothesAttributeDef def = new ClothesAttributeDef("Color");
    repository.save(def);

    // when
    boolean result = repository.existsByNameIgnoreCase("Color");
    boolean result2 = repository.existsByNameIgnoreCase("Color".toLowerCase());
    boolean result3 = repository.existsByNameIgnoreCase("Color".toUpperCase());

    // then
    assertThat(result).isTrue();
    assertThat(result2).isTrue();
    assertThat(result3).isTrue();
    }

    @Test
    @DisplayName("자기 자신 제외 이름 중복 검사")
    void existsByNameIgnoreCaseAndIdNot() {
    // given
    ClothesAttributeDef def1 = new ClothesAttributeDef("색상");
    ClothesAttributeDef def2 = new ClothesAttributeDef("사이즈");

    repository.save(def1);
    repository.save(def2);

    // when
    boolean result1 = repository.existsByNameIgnoreCaseAndIdNot("색상", def1.getId());
    boolean result2 = repository.existsByNameIgnoreCaseAndIdNot("색상",  def2.getId());

    // then
    // 자기 자신 제외
    assertThat(result1).isFalse();
    // 다른 엔티티 기준으로 중복
    assertThat(result2).isTrue();

    }
}