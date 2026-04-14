package com.codeit.otboo.domain.kafka;

import com.codeit.otboo.domain.binarycontent.event.BinaryContentDeletedEvent;
import com.codeit.otboo.domain.binarycontent.service.BinaryContentRetryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;

@Slf4j
@RequiredArgsConstructor
//@Component
public class BinaryContentRequiredTopicListener {

    private final BinaryContentRetryService binaryContentRetryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "otboo.BinaryContentDeletedEvent", groupId = "BinaryContent")
    public void onBinaryContentDeletedEvent(String kafkaEvent) {
        try {
            BinaryContentDeletedEvent event =
                objectMapper.readValue(kafkaEvent, BinaryContentDeletedEvent.class);

            binaryContentRetryService.delete(event.binaryContentId());
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
