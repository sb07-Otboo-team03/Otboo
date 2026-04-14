package com.codeit.otboo.domain.clothes.management.util;

import com.codeit.otboo.domain.clothes.management.entity.Clothes;
import com.codeit.otboo.domain.clothes.management.entity.ClothesType;
import com.codeit.otboo.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClothesTestUtils {

    public static List<Clothes> createClothesList(
        User owner,
        TestEntityManager testEntityManager,
        int count
    ){

        EntityManager entityManager = testEntityManager.getEntityManager();
        List<Clothes> clothesList = new ArrayList<>();

                testEntityManager.persist(owner);
        testEntityManager.flush();

        for(int i = 1; i <= count; i++){
            String name = "옷" + i;
            ClothesType type = ClothesType.values()[(i - 1) % ClothesType.values().length];
            Clothes clothes = new Clothes(name, type, owner, null, List.of());
            testEntityManager.persist(clothes);

            // createdAt 랜덤화 (최근 365일)
            LocalDateTime createdAt = LocalDateTime.now()
                    .minusDays((long) (Math.random() * 365))
                    .minusHours((long) (Math.random() * 24))
                    .minusMinutes((long) (Math.random() * 60));

            entityManager.createQuery("update Clothes c set c.createdAt = :createdAt where c.id = :id")
                    .setParameter("createdAt", createdAt)
                    .setParameter("id", clothes.getId())
                    .executeUpdate();
            clothesList.add(clothes);
        }
        testEntityManager.clear();

        return clothesList;
    }
}
