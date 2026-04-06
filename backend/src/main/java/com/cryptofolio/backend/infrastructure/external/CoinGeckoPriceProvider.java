package com.cryptofolio.backend.infrastructure.external;

import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class CoinGeckoPriceProvider implements CryptoPriceProvider {

    private static final ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public CoinGeckoPriceProvider(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${coingecko.base-url:https://api.coingecko.com/api/v3}") String baseUrl) {
        this(restTemplateBuilder.build(), baseUrl);
    }

    CoinGeckoPriceProvider(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public BigDecimal getCurrentPrice(Crypto crypto) {
        String coinId = resolveCoinId(crypto);
        String url = baseUrl + "/simple/price?ids={coinId}&vs_currencies=usd";

        ResponseEntity<Map<String, Map<String, BigDecimal>>> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                null,
                RESPONSE_TYPE,
                Map.of("coinId", coinId));

        Map<String, Map<String, BigDecimal>> body = response.getBody();
        if (body == null || !body.containsKey(coinId) || body.get(coinId).get("usd") == null) {
            throw new IllegalStateException("CoinGecko price not available for crypto: " + crypto.getSymbol());
        }

        return body.get(coinId).get("usd");
    }

    private String resolveCoinId(Crypto crypto) {
        return switch (crypto.getSymbol()) {
            case "BTC" -> "bitcoin";
            case "ETH" -> "ethereum";
            case "ADA" -> "cardano";
            case "SOL" -> "solana";
            case "XRP" -> "ripple";
            case "DOGE" -> "dogecoin";
            case "DOT" -> "polkadot";
            case "LTC" -> "litecoin";
            case "LINK" -> "chainlink";
            case "AVAX" -> "avalanche-2";
            case "MATIC" -> "matic-network";
            default -> throw new IllegalArgumentException("Unsupported crypto for CoinGecko pricing: " + crypto.getSymbol());
        };
    }
}
