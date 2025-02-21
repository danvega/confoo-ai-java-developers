# ConFoo - AI for Java Devs

Spring AI makes it easy for Java developers to integrate powerful AI capabilities into their applications without becoming machine learning experts. This guide will walk you through creating AI-powered features using familiar Java tools and Spring patterns. With just Java 23, Maven, and an OpenAI API key, you'll learn how to build applications that leverage LLMs for various use cases - from basic text generation to multimodal interactions and connecting AI to your own data sources.

## Requirements

- Java 23
- Maven 
- OpenAI API Key

## Agenda

- [Create your first App](#creating-your-first-app)
- [Synchronous vs Asynchronous](#synchronous-vs-asynchronous)
- [Prompting](#prompt)
- [Structured Output](#structured-output)
- [MultiModal](#multimodal)
- [Spring AI Advisors](#spring-ai-advisors)
- [Bring your own Data](#bring-your-own-data)

## Creating your first App

To get started visit http://start.spring.io, fill in your project metadata and add the following dependencies:

- Spring Web
- OpenAI

```java
@Bean
CommandLineRunner commandLineRunner(ChatClient.Builder builder) {
    return args -> {
        var client = builder.build();

        String content = client.prompt()
                .user("Tell me an Interesting fact about Spring AI")
                .call()
                .content();

        System.out.println(content);
    };
}
```

## Synchronous vs Asynchronous

Spring AI supports both synchronous and Streaming API options.

```java
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/stream")
    public Flux<String> stream() {
        return chatClient.prompt()
                .user("I am visiting Montreal, CA can you give me 10 places I must visit")
                .stream()
                .content();
    }

}
```

## Prompt

### Prompt Parameters

How to take in parameters with your user message? Let's talk about the `PromptUserSpec` interface: 

```
@GetMapping("/posts/new")
public String newPost(@RequestParam String topic) {
    return chatClient.prompt()
            .user(u -> {
                u.text("Write me a blog post about {topic}");
                u.param("topic",topic);
            })
            .call()
            .content();
}
```

### Message Types

Each message is assigned a specific role. These roles categorize the messages, clarifying the context and purpose of each segment of the prompt for the AI model. This structured approach enhances the nuance and effectiveness of communication with the AI, as each part of the prompt plays a distinct and defined role in the interaction.

The primary roles are:

- System Role: Guides the AI’s behavior and response style, setting parameters or rules for how the AI interprets and replies to the input. It’s akin to providing instructions to the AI before initiating a conversation.
- User Role: Represents the user’s input – their questions, commands, or statements to the AI. This role is fundamental as it forms the basis of the AI’s response.
- Assistant Role: The AI’s response to the user’s input. More than just an answer or reaction, it’s crucial for maintaining the flow of the conversation. By tracking the AI’s previous responses (its 'Assistant Role' messages), the system ensures coherent and contextually relevant interactions. The Assistant message may contain Function Tool Call request information as well. It’s like a special feature in the AI, used when needed to perform specific functions such as calculations, fetching data, or other tasks beyond just talking.
- Tool/Function Role: The Tool/Function Role focuses on returning additional information in response to Tool Call Assistant Messages.

Lets discussing improving our prompt "Write me a blog post about {topic}". Start with [Prompt Iteration](./PROMPT-ITERATION.md)

Use the following system message to guide the model

```java
@GetMapping("/posts/new")
public String newPost(@RequestParam String topic) {

    var system = """
            Blog Post Generator Guidelines:
            
            1. Length & Purpose: Generate 500-word blog posts that inform and engage general audiences.
            
            2. Structure:
               - Introduction: Hook readers and establish the topic's relevance
               - Body: Develop 3 main points with supporting evidence and examples
               - Conclusion: Summarize key takeaways and include a call-to-action
            
            3. Content Requirements:
               - Include real-world applications or case studies
               - Incorporate relevant statistics or data points when appropriate
               - Explain benefits/implications clearly for non-experts
            
            4. Tone & Style:
               - Write in an informative yet conversational voice
               - Use accessible language while maintaining authority
               - Break up text with subheadings and short paragraphs
            
            5. Response Format: Deliver complete, ready-to-publish posts with a suggested title.
            """;

    return chatClient.prompt()
            .system(system)
            .user(u -> {
                u.text("Write me a blog post about {topic}");
                u.param("topic",topic);
            })
            .call()
            .content();
}
```

## Structured Output

Structured output refers to generating responses in specific, predefined formats rather than free-form text. 
This allows LLMs to produce data that can be easily parsed and used by other systems.

### Chatbot Examples

- [Google AI Studio](https://aistudio.google.com/)
  - Using Google AI Studio: "Can you list all the teams in the NBA?"
  - Update Structured Output: team name, city, country
- [OpenAI Playground](https://platform.openai.com/playground )
  - Using OpenAI Playground: "Can you list all the teams in the NHL?"
  - Update Structured Output: team name, city, country

Each Large Language Model has a specific way to handle structured output. Fortunately, Spring handles all of that 
under the hood for you and provides a unified way of handling it.

### Spring AI Structured Output

The ability of LLMs to produce structured outputs is important for downstream applications that rely on reliably parsing output values.
Developers want to quickly turn results from an AI model into data types, such as JSON, XML or Java classes,
that can be passed to other application functions and methods.

The Spring AI Structured Output Converters help to convert the LLM output into a structured format.

- StructuredOutputConverter
- BeanOutputConverter


```java
@RestController
public class VacationPlan {

    private final ChatClient chatClient;

    public VacationPlan(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/vacation/unstructured")
    public String vacationUnstructured() {
        return chatClient.prompt()
                .user("What's a good vacation plan while I'm in Montreal CA for 4 days?")
                .call()
                .content();
    }

    @GetMapping("/vacation/structured")
    public Itinerary vacationStructured() {
        return chatClient.prompt()
                .user("What's a good vacation plan while I'm in Montreal CA for 4 days?")
                .call()
                .entity(Itinerary.class);
    }

    record Activity(String activity, String location, String day, String time){}
    record Itinerary(List<Activity> itinerary) {}
}
```

## Multimodal

Multimodality refers to a model’s ability to simultaneously understand and process information from various sources, 
including text, images, audio, and other data formats.

```java
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
```

## Spring AI Advisors

The Spring AI Advisors API provides a flexible and powerful way to intercept, modify, and enhance AI-driven interactions in your Spring applications. By leveraging the Advisors API, developers can create more sophisticated, reusable, and maintainable AI components.

The key benefits include encapsulating recurring Generative AI patterns, transforming data sent to and from Large Language Models (LLMs), and providing portability across various models and use cases.

You can configure existing advisors using the ChatClient API as shown in the following example:

```java
var chatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(
                new MessageChatMemoryAdvisor(chatMemory), // chat-memory advisor
                new QuestionAnswerAdvisor(vectorStore)    // RAG advisor
        )
        .build();

String response = this.chatClient.prompt()
        // Set advisor parameters at runtime
        .advisors(advisor -> advisor.param("chat_memory_conversation_id", "678")
                .param("chat_memory_response_size", 100))
        .user(userText)
        .call()
        .content();
```

The interface ChatMemory represents a storage for chat conversation history. It provides methods to add messages to a conversation, retrieve messages from a conversation, and clear the conversation history.

There are currently two implementations, InMemoryChatMemory and CassandraChatMemory, that provide storage for chat conversation history, in-memory and persisted with time-to-live, correspondingly.

```java
public class MemoryController {

    private final ChatClient chatClient;

    public MemoryController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .build();
    }

    @GetMapping("/memory")
    public String home(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

}
```

## Bring your own data

In this first example you'll ask the LLM "Can you give me an up-to-date list of popular large language models and their current context window size?"

You will receive a response that looks correct, but is it? LLMs can produce hallucinations—confident but incorrect statements 
that appear plausible—due to limitations in their training methodology and inability to verify information against external sources. 
Additionally, their knowledge is limited to what was included in their training data cutoff, making them unable to provide 
reliable information about events, discoveries, or developments that occurred after this date.

```java
    /*
     * How can we improve this?
     * We could send the context along with the request, but we would have to do that every single time.
     *   - we are paying for those tokens even when the question has nothing do with that
     */
    @GetMapping("/models")
    public String models() {
        return chatClient.prompt()
                .user("Can you give me an up to date list of popular large language models and their current context window size")
                .call()
                .content();
    }
```

### Tools

AI models don’t have access to real-time information. Any question that assumes awareness of information such as the 
current date or weather forecast cannot be answered by the model. However, we can provide a tool that can retrieve this 
information, and let the model call this tool when access to real-time information is needed.

```java
public class DatTimeTools {

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

}
```

```java
@RestController
public class DatTimeChatController {

    private final ChatClient chatClient;

    public DatTimeChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/tools")
    public String tools() {
        return chatClient.prompt("What day is tomorrow?")
                .tools(new DatTimeTools())
                .call()
                .content();
    }

}
```

## Presentation Notes

- Open Tabs
  - https://github.com/danvega/confoo-ai-java-developers
  - https://start.spring.io
  - https://docs.spring.io/spring-ai/reference/
- Docker Running?
  - Probably not unless we can't get to OpenAI for some reason
- IntelliJ
  - Open up to starting code