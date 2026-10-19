//member 4
package com.accenture.store.ai;

/**
 * The single seam between the store and the external AI provider. Keeping it an
 * interface means tests can supply a fake and the app never needs a live key
 * to start, and that a different provider only means a different implementation.
 */
public interface AiClient {
    /**
     * @param systemInstruction the rules and store data the model must obey
     * @param userPrompt        the customer's own question or text
     * @return the model's plain text answer
     */
    String generate(String systemInstruction, String userPrompt);
}
