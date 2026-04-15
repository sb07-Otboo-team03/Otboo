package com.codeit.otboo.domain.clothes.recommendation.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public OpenAiChatModel openAiChatModel(
            @Value("${spring.ai.openai.api-key}") String apiKey
    ) {

        OpenAiApi api = OpenAiApi.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .apiKey(apiKey)
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(
                        OpenAiChatOptions.builder()
                                .model("meta-llama/llama-3-8b-instruct:free")
                                .temperature(0.7)
                                .build()
                )
                .build();
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel model) {
        return ChatClient.create(model);
    }
}
