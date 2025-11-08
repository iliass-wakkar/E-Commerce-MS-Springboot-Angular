package com.Gateway.Server.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomGatewayFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // la méthode filtre prend en entrée
        // ServerWebExchange: contient les détails de la requete et de la réponse
        //GatewayFilterChain : contient une chaine d'objet de filtre
        //(elle permet de passer la requette au prochain filtre ou directement au bach-end)
        //Cette méthode contient la Logique avant d'appeler le prochain filtre
        System.out.println(" Logique avant d'appeler le prochain filtre appliquée avec succés");
        //Passer la requete au prochain filtre dans return grace à (chain.filtre(exchange)
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    System.out.println("CustomGatewayFilter : traitement après la requête");
                }));
    }
}
