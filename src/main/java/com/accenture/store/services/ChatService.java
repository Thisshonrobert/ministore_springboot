//member 4
package com.accenture.store.services;

import com.accenture.store.ai.AiClient;
import com.accenture.store.ai.AiPrompts;
import com.accenture.store.dtos.ChatResponse;
import com.accenture.store.dtos.ProductDto;
import com.accenture.store.entities.Product;
import com.accenture.store.mappers.ProductMapper;
import com.accenture.store.repositories.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Answers shopping questions with the real catalogue.
 *
 * The service never trusts the model with product facts. It first reads the
 * products straight from the database, writes them into the system
 * instruction, and the model is told to use only that text. The same rows are
 * returned to the storefront, so what the customer reads can be checked
 * against the database. There is no vector store and no RAG framework: the
 * catalogue of a mini store fits in one prompt.
 */
@Service
public class ChatService {
    private static final int MAX_CATALOGUE_PRODUCTS = 40;
    private static final int MAX_MATCHED_PRODUCTS = 5;
    private static final int MAX_KEYWORDS = 4;

    /** Words that carry no product meaning, so they are not used as search keywords. */
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "but", "for", "you", "your", "our", "with", "without", "about",
            "what", "which", "who", "whose", "why", "when", "where", "how", "much", "many",
            "does", "do", "did", "have", "has", "had", "can", "could", "should", "would",
            "will", "are", "is", "was", "were", "be", "been", "get", "got", "give", "show",
            "tell", "want", "need", "like", "buy", "sell", "some", "any", "all", "one", "two",
            "this", "that", "these", "those", "there", "here", "they", "them", "their", "its",
            "from", "into", "out", "off", "not", "yes", "please", "thanks", "thank", "hello",
            "hey", "hi", "good", "help", "mini", "store", "shop", "shopping", "item", "items",
            "product", "products", "thing", "things", "price", "prices", "cost", "costs",
            "cheap", "cheapest", "expensive", "list", "available", "availability",
            "stock", "cart", "order", "orders", "delivery", "shipping", "return", "returns");

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final AiClient aiClient;
    private final AiPrompts aiPrompts;

    public ChatService(ProductRepository productRepository,
                       ProductMapper productMapper,
                       AiClient aiClient,
                       AiPrompts aiPrompts) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.aiClient = aiClient;
        this.aiPrompts = aiPrompts;
    }

    public ChatResponse chat(String message) {
        String question = message.trim();

        List<Product> relevant = findRelevantProducts(question);
        List<Product> catalogue = productRepository.findCatalogue(PageRequest.of(0, MAX_CATALOGUE_PRODUCTS));

        String systemInstruction = aiPrompts.storeAssistant(describeCatalogue(catalogue, relevant));
        String reply = aiClient.generate(systemInstruction, question);

        ChatResponse response = new ChatResponse();
        response.setReply(reply);
        response.setProducts(toDtos(relevant));
        return response;
    }

    /**
     * Looks up the products whose name, description or category contains a
     * keyword from the question. The database decides what matches; the model
     * only ever sees the result.
     */
    private List<Product> findRelevantProducts(String question) {
        List<Product> matches = new ArrayList<>();

        for (String keyword : keywords(question)) {
            List<Product> found = productRepository.search(
                    "%" + keyword + "%", PageRequest.of(0, MAX_MATCHED_PRODUCTS));

            for (Product product : found) {
                if (matches.stream().noneMatch(match -> match.getId().equals(product.getId()))) {
                    matches.add(product);
                }
                if (matches.size() == MAX_MATCHED_PRODUCTS) {
                    return matches;
                }
            }
        }

        return matches;
    }

    private List<String> keywords(String question) {
        List<String> keywords = new ArrayList<>();

        for (String word : question.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (word.length() < 3 || STOP_WORDS.contains(word) || keywords.contains(word)) {
                continue;
            }
            keywords.add(word);
            if (keywords.size() == MAX_KEYWORDS) {
                break;
            }
        }

        return keywords;
    }

    private String describeCatalogue(List<Product> catalogue, List<Product> relevant) {
        if (catalogue.isEmpty()) {
            return "The catalogue is empty. No product is for sale at the moment.";
        }

        StringBuilder text = new StringBuilder();
        text.append("Categories on the shelves: ");
        List<String> categoryNames = new ArrayList<>();
        for (Product product : catalogue) {
            String name = product.getCategory().getName();
            if (!categoryNames.contains(name)) {
                categoryNames.add(name);
            }
        }
        text.append(String.join(", ", categoryNames)).append('\n');
        text.append("Products (").append(catalogue.size()).append(" in this list):\n");

        for (Product product : catalogue) {
            text.append("- name: ").append(product.getName())
                    .append(" | category: ").append(product.getCategory().getName())
                    .append(" | price: ").append(product.getPrice().toPlainString()).append(" INR");

            if (product.getDescription() != null && !product.getDescription().isBlank()) {
                text.append(" | description: ").append(product.getDescription().trim());
            }

            text.append('\n');
        }

        if (!relevant.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (Product product : relevant) {
                names.add(product.getName());
            }
            text.append("Rows from the database that match this question: ")
                    .append(String.join(", ", names)).append('\n');
        }

        return text.toString();
    }

    private List<ProductDto> toDtos(List<Product> products) {
        List<ProductDto> dtos = new ArrayList<>();
        for (Product product : products) {
            dtos.add(productMapper.toDto(product));
        }
        return dtos;
    }
}
