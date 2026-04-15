package com.cryptofolio.backend.infrastructure.in.web.controller;

import com.cryptofolio.backend.infrastructure.external.LivePriceService;
import com.cryptofolio.backend.infrastructure.in.web.response.CoinPriceResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/prices")
public class PriceController {

    private final LivePriceService livePriceService;

    public PriceController(LivePriceService livePriceService) {
        this.livePriceService = livePriceService;
    }

    @GetMapping
    public List<CoinPriceResponse> getLivePrices() {
        return livePriceService.getLivePrices();
    }

    @GetMapping(path = "/stream", produces = "text/event-stream")
    public SseEmitter streamLivePrices() {
        SseEmitter emitter = new SseEmitter(0L);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Runnable sendPrices = () -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("prices")
                        .data(livePriceService.getLivePrices()));
            } catch (Exception exception) {
                emitter.complete();
            }
        };

        sendPrices.run();
        executor.scheduleAtFixedRate(sendPrices, 1, 1, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(() -> {
            executor.shutdown();
            emitter.complete();
        });
        emitter.onError(error -> executor.shutdown());

        return emitter;
    }
}
