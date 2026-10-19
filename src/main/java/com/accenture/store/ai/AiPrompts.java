//member 4
package com.accenture.store.ai;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads the AI prompt templates from the classpath and fills in the live
 * database content. Prompts live in files so they can be read and edited
 * without touching Java code, the same idea the reference project used.
 */
@Component
public class AiPrompts {
    private static final String CATALOG_PLACEHOLDER = "{{catalog}}";

    private final String storeAssistantTemplate;
    private final String summarizeReviewsInstruction;

    public AiPrompts() {
        this.storeAssistantTemplate = read("prompts/store-assistant.txt");
        this.summarizeReviewsInstruction = read("prompts/summarize-reviews.txt");
    }

    /** The system instruction for the shopping assistant, with the catalogue filled in. */
    public String storeAssistant(String catalog) {
        return storeAssistantTemplate.replace(CATALOG_PLACEHOLDER, catalog);
    }

    /** The system instruction that turns review text into one short summary. */
    public String summarizeReviews() {
        return summarizeReviewsInstruction;
    }

    private static String read(String location) {
        try (InputStream input = new ClassPathResource(location).getInputStream()) {
            return stripCommentHeader(StreamUtils.copyToString(input, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the AI prompt " + location, exception);
        }
    }

    /**
     * Drops a leading "#member N" style comment block. The prompt files carry a
     * team ownership comment like every other file, but that comment is written
     * for humans and must never reach the model, so it is removed before use.
     */
    private static String stripCommentHeader(String text) {
        int index = 0;
        while (index < text.length() && text.charAt(index) == '#') {
            int lineEnd = text.indexOf('\n', index);
            if (lineEnd < 0) {
                return "";
            }
            index = lineEnd + 1;
        }
        return text.substring(index);
    }
}
