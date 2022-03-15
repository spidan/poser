package de.dfki.asr.poser;

import de.dfki.asr.poser.Converter.PoserGatewayFilterFactory;
import de.dfki.asr.poser.Converter.PoserGatewayFilterFactory.PoserGatewayConfig;
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
	public RouteLocator myRoutes(RouteLocatorBuilder builder,
			PoserGatewayFilterFactory poserFactory) {
		return builder.routes()
				.route(p -> p
					.path("/test")
					.filters(f -> f.filter(poserFactory.apply(new PoserGatewayConfig())))
				.uri("http://httpbin.org:80")).build();
	}
}
