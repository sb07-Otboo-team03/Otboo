package com.codeit.otboo.batch.weather.alert.repository;

import com.codeit.otboo.batch.weather.alert.model.AlertTarget;

import java.util.List;

public interface AlertTargetQueryRepository {
    List<AlertTarget> findAllForWeatherAlert();
}