package com.cryptofolio.backend.infrastructure.external;

import com.cryptofolio.backend.domain.valueobject.Crypto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CoinGeckoPriceProviderTest {

    @Test
    void shouldReturnUsdPriceForSupportedCrypto() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        CoinGeckoPriceProvider provider = new CoinGeckoPriceProvider(restTemplate, "https://api.test/coingecko");

        server.expect(requestTo("https://api.test/coingecko/simple/price?ids=bitcoin&vs_currencies=usd"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"bitcoin\":{\"usd\":65000.25}}", MediaType.APPLICATION_JSON));

        BigDecimal price = provider.getCurrentPrice(Crypto.from("BTC"));

        assertThat(price).isEqualByComparingTo("65000.25");
        server.verify();
    }

    @Test
    void shouldThrowWhenCryptoIsUnsupported() {
        CoinGeckoPriceProvider provider = new CoinGeckoPriceProvider(new RestTemplate(), "https://api.test/coingecko");

        assertThatThrownBy(() -> provider.getCurrentPrice(Crypto.from("BNB")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported crypto for CoinGecko pricing: BNB");
    }

    @Test
    void shouldThrowWhenApiResponseDoesNotContainUsdPrice() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        CoinGeckoPriceProvider provider = new CoinGeckoPriceProvider(restTemplate, "https://api.test/coingecko");

        server.expect(requestTo("https://api.test/coingecko/simple/price?ids=ethereum&vs_currencies=usd"))
                .andRespond(withSuccess("{\"ethereum\":{}}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.getCurrentPrice(Crypto.from("ETH")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("CoinGecko price not available for crypto: ETH");

        server.verify();
    }
}
