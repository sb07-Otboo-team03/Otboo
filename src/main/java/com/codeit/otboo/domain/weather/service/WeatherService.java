package com.codeit.otboo.domain.weather.service;

import com.codeit.otboo.domain.weather.dto.response.WeatherAPILocationResponse;
import com.codeit.otboo.domain.weather.dto.response.WeatherResponse;
import com.codeit.otboo.domain.weather.repository.WeatherRepository;
import com.codeit.otboo.global.util.KakaoLocalUtil;
import com.codeit.otboo.global.util.KakaoLocalUtil.KakaoRegionType;
import com.codeit.otboo.global.util.KmaGridConverter;
import com.codeit.otboo.global.util.KmaGridConverter.GridResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeatherService {

    private final WeatherRepository weatherRepository;

    public List<WeatherResponse> getAll(double longitude, double latitude) {
        return null;
    }

    public WeatherAPILocationResponse getLocation(double longitude, double latitude) {

        GridResult gridResult = KmaGridConverter.convertToGrid(latitude, longitude);

        return new WeatherAPILocationResponse(
                latitude,
                longitude,
                gridResult.nx(),
                gridResult.ny(),
                KakaoLocalUtil.getAddressLevels(longitude, latitude, KakaoRegionType.H)
        );
    }

}
