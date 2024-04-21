//package server.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//public class CurrencyServiceTest {
//
//    @Mock
//    private WebClient.Builder webClientBuilder;
//
//    @Mock
//    private WebClient webClient;
//
//    @Mock
//    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
//
//    @Mock
//    private WebClient.RequestHeadersSpec requestHeadersSpec;
//
//    @Mock
//    private WebClient.ResponseSpec responseSpec;
//
//    @InjectMocks
//    private CurrencyService currencyService;
//
//    @BeforeEach
//    public void setup() {
//        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
//        when(webClientBuilder.build()).thenReturn(webClient);
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//
//        Map<String, Double> rates = new HashMap<>();
//        rates.put("USD", 1.0);
//        rates.put("EUR", 0.9);
//        Mono<Map> resultMap = Mono.just(rates);
//        when(responseSpec.bodyToMono(Map.class)).thenReturn(resultMap);
//
//        currencyService = new CurrencyService(webClientBuilder);
//    }
//
//    @Test
//    public void getCurrencyRateTest() {
//        String from = "USD";
//        String to = "EUR";
//        LocalDate date = LocalDate.now();
//
//        try {
//            StepVerifier.create(currencyService.getExchangeRate(from, to, date.toString()))
//                    .expectNext(0.9)  // Expect the exchange rate
//                    .verifyComplete();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
