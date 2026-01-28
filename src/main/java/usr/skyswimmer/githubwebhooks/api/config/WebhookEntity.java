package usr.skyswimmer.githubwebhooks.api.config;

import java.io.IOException;

import com.google.gson.JsonObject;

import usr.skyswimmer.githubwebhooks.util.JsonUtils;

public class WebhookEntity implements IBaseJsonConfigEntity {

	public JsonObject json;
	public String secret;
	public String repository;
	public String url;

	@Override
	public void loadFromJson(JsonObject source, String scope) throws IOException {
		json = source;
		secret = JsonUtils.getElementOrError(scope, source, "secret").getAsString();
		repository = JsonUtils.getElementOrError(scope, source, "repository").getAsString();
		url = JsonUtils.getElementOrError(scope, source, "url").getAsString();
	}

}
