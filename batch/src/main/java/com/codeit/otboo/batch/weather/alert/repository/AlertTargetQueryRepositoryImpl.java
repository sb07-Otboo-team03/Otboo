package com.codeit.otboo.batch.weather.alert.repository;

import com.codeit.otboo.batch.weather.alert.model.AlertTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AlertTargetQueryRepositoryImpl implements AlertTargetQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AlertTarget> findAllForWeatherAlert() {
        return jdbcTemplate.query("""
            select
                p.user_id,
                p.x,
                p.y
            from profiles p
            where p.x is not null
              and p.y is not null
        """, (rs, rowNum) -> new AlertTarget(
                UUID.fromString(rs.getString("user_id")),
                rs.getInt("x"),
                rs.getInt("y")
        ));
    }
}
