package de.dfki.asr.poser.Converter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class PoserGatewayFilterFactory extends AbstractGatewayFilterFactory<PoserGatewayFilterFactory.Config> {

	private static final Logger LOG = LoggerFactory.getLogger(PoserGatewayFilterFactory.class);

	public PoserGatewayFilterFactory() {
        super(Config.class);
    }

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			return chain.filter(exchange);
			};
	}

	public static class Config {
		@Getter
		@Setter
		private String loweringTemplateName;

		public Config (final String templateName) {
			this.loweringTemplateName = templateName;
		}
	}
}
