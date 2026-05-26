package it.giuval.cloud.api_gateway.filters;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import it.giuval.cloud.api_gateway.service.JwtService;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

	@Value("${app.security.public-urls}")
	private List<String> publicEndpoints;

	private JwtService jwtService;

	public AuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		String path = request.getURI().getPath();

		//Skip Endpoint Pubblici
		boolean isPublic = publicEndpoints.stream().anyMatch(path::contains);
		if (isPublic) {
			return chain.filter(exchange);
		}
		//Controllo presenza Header Authorization
		if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
			return onError(exchange, "Header Authorization mancante", HttpStatus.UNAUTHORIZED);
		}
		
		String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return onError(exchange, "Formato token non valido", HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7);
		try {
			Claims claims = jwtService.extractClaims(token);
			String userId = claims.get("userId", String.class);
			List<?> roles = claims.get("roles", List.class);

			ServerHttpRequest mutatedRequest = request.mutate()
					.header("X-User-Id", userId)
					.header("X-User-Roles", roles != null ? roles.toString() : "[]")
					.build();

			return chain.filter(exchange.mutate().request(mutatedRequest).build());
		} catch (Exception e) {
			System.out.println("Errore validazione JWT sul Gateway: " + e.getMessage());
			return onError(exchange, "Token non valido o scaduto", HttpStatus.UNAUTHORIZED);
		}
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().add("Content-Type", "application/json");
		return response.setComplete();
	}
}
