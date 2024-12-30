package com.sivalabs.shoppingcartui.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthHeaderFilter extends AbstractGatewayFilterFactory<AuthHeaderFilter.Config> {

    public AuthHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getHeaders().containsKey("AUTH_HEADER")) {
                String sessionId = UUID.randomUUID().toString();
                exchange = exchange.mutate()
                    .request(r -> r.headers(headers -> headers.add("AUTH_HEADER", sessionId)))
                    .build();
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Put the configuration properties here
    }
}
