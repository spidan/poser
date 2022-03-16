package de.dfki.asr.poser;

import de.dfki.asr.poser.Converter.PoserGatewayFilterFactory;
import de.dfki.asr.poser.Converter.RdfToJson;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PoserConfig {

	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder,
			PoserGatewayFilterFactory poserFactory) {
		RdfToJson jsonConverter = new RdfToJson();

		return builder.routes()
				.route(p -> p
					.path("/echo")
					.filters(f -> f.cacheRequestBody(String.class)
							.filter(poserFactory.apply(new PoserGatewayFilterFactory.Config("jsonApiMultipleValues.ttl"))) )
				.uri("http://localhost:8123/"))
				.build();
	}
}
