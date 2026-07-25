package dev.interviewkata.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${interviewkata.ai.provider:openai}")
    private String provider;

    @Value("${interviewkata.ai.fallback.api-key:#{null}}")
    private String fallbackApiKey;

    @Value("${interviewkata.ai.fallback.base-url:https://integrate.api.nvidia.com/v1}")
    private String fallbackBaseUrl;

    @Value("${interviewkata.ai.fallback.model:nvidia/llama-3.1-nemotron-70b-instruct}")
    private String fallbackModel;

    @Bean
    public ChatClient chatClient(
            @Qualifier("openAiChatModel") ChatModel openAiModel,
            @Qualifier("anthropicChatModel") ChatModel anthropicModel) {
        ChatModel selected = "anthropic".equalsIgnoreCase(provider) ? anthropicModel : openAiModel;
        return ChatClient.builder(selected).build();
    }

    @Bean
    public ChatClient fallbackChatClient() {
        if (fallbackApiKey == null || fallbackApiKey.isBlank() || "sk-placeholder".equals(fallbackApiKey)) {
            return null;
        }
        OpenAiApi api = new OpenAiApi(fallbackBaseUrl, fallbackApiKey);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(fallbackModel)
                .withMaxTokens(4096)
                .build();
        OpenAiChatModel model = new OpenAiChatModel(api, options);
        return ChatClient.builder(model).build();
    }
}
