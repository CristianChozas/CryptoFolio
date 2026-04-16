package com.cryptofolio.backend.infrastructure.external;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LivePriceService {

    private static final Logger log = LoggerFactory.getLogger(LivePriceService.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final String BTC_SYMBOL = "BTCEUR";
    private static final String ETH_SYMBOL = "ETHEUR";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final URI streamUri;
    private final boolean streamEnabled;

    private final AtomicReference<List<LiveCoinPrice>> cachedPrices = new AtomicReference<>(List.of());
    private final Map<String, LiveCoinPrice> pricesBySymbol = new ConcurrentHashMap<>();

    private volatile WebSocket webSocket;
    private volatile boolean connecting;

    public LivePriceService(
            ObjectMapper objectMapper,
            @Value("${binance.ws-url:wss://stream.binance.com:9443/stream?streams=btceur@ticker/etheur@ticker}") String streamUrl,
            @Value("${binance.stream-enabled:true}") boolean streamEnabled) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.streamUri = URI.create(streamUrl);
        this.streamEnabled = streamEnabled;
    }

    @PostConstruct
    public void init() {
        ensureConnected();
    }

    @PreDestroy
    public void shutdown() {
        WebSocket current = this.webSocket;
        this.webSocket = null;

        if (current != null) {
            current.sendClose(WebSocket.NORMAL_CLOSURE, "shutdown");
        }
    }

    @Scheduled(fixedDelay = 5_000)
    public void ensureConnected() {
        if (!streamEnabled) {
            return;
        }

        if (webSocket != null || connecting) {
            return;
        }

        connecting = true;

        httpClient.newWebSocketBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .buildAsync(streamUri, new BinanceTickerListener())
                .whenComplete((socket, throwable) -> {
                    connecting = false;

                    if (throwable != null) {
                        log.warn("Failed to connect to Binance ticker stream: {}", throwable.getMessage());
                        return;
                    }

                    this.webSocket = socket;
                    log.info("Connected to Binance ticker stream");
                });
    }

    public List<LiveCoinPrice> getLivePrices() {
        return cachedPrices.get();
    }

    private void updatePrice(JsonNode payload) {
        String symbol = payload.path("s").asText();
        if (!BTC_SYMBOL.equals(symbol) && !ETH_SYMBOL.equals(symbol)) {
            return;
        }

        BigDecimal eurPrice = asBigDecimal(payload.path("c").asText());
        BigDecimal change24hPercent = asBigDecimal(payload.path("P").asText());

        LiveCoinPrice response = new LiveCoinPrice(
                BTC_SYMBOL.equals(symbol) ? "BTC" : "ETH",
                BTC_SYMBOL.equals(symbol) ? "Bitcoin" : "Ethereum",
                eurPrice,
                change24hPercent);

        pricesBySymbol.put(symbol, response);

        LiveCoinPrice btc = pricesBySymbol.get(BTC_SYMBOL);
        LiveCoinPrice eth = pricesBySymbol.get(ETH_SYMBOL);

        if (btc != null && eth != null) {
            cachedPrices.set(List.of(btc, eth));
        }
    }

    private BigDecimal asBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return BigDecimal.ZERO;
        }
    }

    private final class BinanceTickerListener implements WebSocket.Listener {

        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            buffer.append(data);

            if (last) {
                try {
                    JsonNode root = objectMapper.readTree(buffer.toString());
                    JsonNode payload = root.path("data");
                    updatePrice(payload);
                } catch (Exception exception) {
                    log.warn("Failed to parse Binance ticker payload: {}", exception.getMessage());
                } finally {
                    buffer.setLength(0);
                }
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            LivePriceService.this.webSocket = null;
            log.warn("Binance ticker stream closed: status={}, reason={}", statusCode, reason);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            LivePriceService.this.webSocket = null;
            log.warn("Binance ticker stream error: {}", error.getMessage());
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }
}
