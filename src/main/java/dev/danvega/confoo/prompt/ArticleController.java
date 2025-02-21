package dev.danvega.confoo.prompt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleController {

    private final ChatClient chatClient;

    public ArticleController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /*
     * Add @RequestParam String topic
     * u -> u.text() & u.param()
     * blog-post-system-message
     */
    @GetMapping("/posts/new")
    public String newPost() {
        // system message

        return chatClient.prompt()
                .user("Write me a blog post about AI")
                .call()
                .content();
    }

}
