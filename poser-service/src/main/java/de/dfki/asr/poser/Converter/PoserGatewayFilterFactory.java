package de.dfki.asr.poser.Converter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class PoserGatewayFilterFactory extends AbstractGatewayFilterFactory<PoserGatewayFilterFactory.PoserGatewayConfig> {

	public PoserGatewayFilterFactory() {
        super(PoserGatewayConfig.class);
    }

	@Override
	public GatewayFilter apply(PoserGatewayConfig config) {
		return (exchange, chain) -> {
			return chain.filter(exchange);
			};
	}

	public static class PoserGatewayConfig {
		public String transformToJSON() {
			RdfToJson converter = new RdfToJson();
			return "testomator";
		}
	}
}
