package com.cryptofolio.backend.infrastructure.external;

import com.cryptofolio.backend.application.port.out.CryptoPriceProvider;
import com.cryptofolio.backend.domain.valueobject.Crypto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

@Component
public class CoinGeckoPriceProvider implements CryptoPriceProvider {

    private static final ParameterizedTypeReference<Map<String, Map<String, BigDecimal>>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
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
    @SuppressWarnings("null")
    public BigDecimal getCurrentPrice(Crypto crypto) {
        String coinId = resolveCoinId(crypto);
        String url = baseUrl + "/simple/price?ids={coinId}&vs_currencies=usd";
        HttpMethod httpMethod = HttpMethod.GET;
        Map<String, String> uriVariables = Map.of("coinId", coinId);

        ResponseEntity<Map<String, Map<String, BigDecimal>>> response = restTemplate.exchange(
                url,
                httpMethod,
                null,
                RESPONSE_TYPE,
                uriVariables);

        Map<String, Map<String, BigDecimal>> body = Objects.requireNonNull(
                response.getBody(),
                "CoinGecko response body cannot be null");
        Map<String, BigDecimal> coinData = body.get(coinId);
        if (coinData == null) {
            throw new IllegalStateException("CoinGecko price not available for crypto: " + crypto.getSymbol());
        }

        BigDecimal usdPrice = coinData.get("usd");
        if (usdPrice == null) {
            throw new IllegalStateException("CoinGecko price not available for crypto: " + crypto.getSymbol());
        }

        return usdPrice;
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
