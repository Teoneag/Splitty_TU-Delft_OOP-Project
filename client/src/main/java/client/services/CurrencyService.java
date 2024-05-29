package client.services;

import java.util.List;

final public class CurrencyService {
    private final List<String> currencies = List.of("EUR", "USD", "CHF", "GBP");
    public List<String> getCurrencies() {
        return currencies;
    }
}
