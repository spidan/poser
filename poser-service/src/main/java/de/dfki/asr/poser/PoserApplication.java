package de.dfki.asr.poser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PoserApplication {
	public static void main(String[] args) {
		SpringApplication.run(PoserApplication.class, args);
	}

	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p
					.path("/test")
					.filters(f -> f.addRequestHeader("Hello", "World"))
				.uri("http://httpbin.org:80")).build();
	}
}
