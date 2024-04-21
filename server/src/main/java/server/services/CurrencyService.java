package server.services;

import commons.Expense;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Currency;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    private final WebClient webClient;
    private final String apiKey = "8a8af079f5fc4dbcacfa59ea01c8064d";
    private final String baseUrl = "https://openexchangerates.org/api/latest.json?app_id=" + apiKey;

    public CurrencyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Get exchange rate
     * @param from currency
     * @param to currency
     * @param date of exchange rate
     * @return exchange rate
     * @throws IOException if rate cannot be fetched
     */
    public float getExchangeRate(String from, String to, String date) throws IOException {
        Path cachePath = Paths.get("rates", date, from, to + ".txt");
        Files.createDirectories(cachePath.getParent());

        if (Files.exists(cachePath)) {
            return Float.parseFloat(Files.readString(cachePath));
        } else {
            return fetchAndCacheRate(from, to, cachePath);
        }
    }

    /**
     * Fetch and cache rate
     * @param from currency
     * @param to currency
     * @param cachePath to cache
     * @return exchange rate
     * @throws IOException if rate cannot be fetched
     */
    private float fetchAndCacheRate(String from, String to, Path cachePath) throws IOException {
        Mono<Map<String, Object>> responseMono = webClient.get().retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> responseBody = responseMono.block();
        if (responseBody == null || !responseBody.containsKey("rates")) {
            throw new RuntimeException("Failed to fetch rates from API");
        }

        @SuppressWarnings("unchecked") Map<String, Number> rates = (Map<String, Number>) responseBody.get("rates");

        double fromToUsd = from.equals("USD") ? 1.0 : inverseRate(rates.get(from));
        double usdToTo = rate(rates.get(to));
        double exchangeRate = fromToUsd * usdToTo;

        Files.writeString(cachePath, String.valueOf(exchangeRate));
        return (float) exchangeRate;
    }

    private double rate(Number number) {
        return number != null ? number.doubleValue() : 0.0;
    }

    private double inverseRate(Number number) {
        double value = rate(number);
        return value != 0.0 ? 1.0 / value : 0.0;
    }

    /**
     *
     * @param expenses list of expenses
     * @param currency currency to convert to
     * @return the converted list of expenses
     */
    public List<Expense> convertExpenses(List<Expense> expenses, String currency) {
        return expenses.stream().peek(expense -> {
            try {
                float rate = getExchangeRate(expense.getCurrency().getCurrencyCode(),
                        currency, expense.getDate().toString());
                expense.setAmount(expense.getAmount() * rate);
                expense.setCurrency(Currency.getInstance(currency));
            } catch (Exception e) {
                // ToDO handle exception
            }
        }).toList();
    }
}
