package de.dfki.asr.poser.gatewayFilters;

import de.dfki.asr.poser.Converter.RdfToJson;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PoserGatewayFilterFactory extends AbstractGatewayFilterFactory<PoserGatewayFilterFactory.Config> {

	private static final Logger LOG = LoggerFactory.getLogger(PoserGatewayFilterFactory.class);

	public PoserGatewayFilterFactory() {
        super(Config.class);
    }

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			String cachedRequestBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
			String loweringTemplateName = config.getLoweringTemplateName();
			RdfToJson jsonConverter = new RdfToJson();
			InputStream templateStream = this.getClass().getResourceAsStream("/".concat(loweringTemplateName));
			try {
				String loweringTemplate = IOUtils.toString(templateStream, Charset.forName("utf-8"));
				String mappedRequestBody = jsonConverter.buildJsonString(cachedRequestBody, loweringTemplate);
			ModifyRequestBodyGatewayFilterFactory.Config modifyRequestConfig = new ModifyRequestBodyGatewayFilterFactory.Config()
                .setContentType(ContentType.APPLICATION_JSON.getMimeType()) // change content type ...
                .setRewriteFunction(String.class, String.class, (oExchange, newRequestBody) -> Mono.just(mappedRequestBody));
			return new ModifyRequestBodyGatewayFilterFactory().apply(modifyRequestConfig).filter(exchange, chain);
			} catch (IOException ex) {
				return chain.filter(exchange);
			}
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
