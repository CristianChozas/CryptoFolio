package com.cryptofolio.backend.infrastructure.out.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI cryptoFolioOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT obtenido desde los endpoints de autenticacion.")))
                .info(new Info()
                        .title("CryptoFolio API")
                        .version("v1")
                        .description("REST API para gestionar portfolios de criptomonedas, transacciones y resumenes de inversion.")
                        .contact(new Contact()
                                .name("Cristian Chozas")
                                .email("admin@cryptofolio.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
