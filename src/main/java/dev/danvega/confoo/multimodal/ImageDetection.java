package dev.danvega.confoo.multimodal;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ImageDetection {

    private final ChatClient chatClient;
    @Value("classpath:/images/elisabeth-zink-8ZES590dzNc-unsplash.jpg")
    Resource sampleImage;

    public ImageDetection(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/image")
    public String image() throws IOException {
        return chatClient.prompt()
                .user(u -> u
                        .text("Can you please explain what you see in the following image?")
                        .media(MimeTypeUtils.IMAGE_JPEG,sampleImage)
                )
                .call()
                .content();
    }
}