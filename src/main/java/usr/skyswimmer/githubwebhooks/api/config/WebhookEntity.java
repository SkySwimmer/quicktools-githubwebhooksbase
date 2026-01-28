package usr.skyswimmer.githubwebhooks.api.config;

import java.io.IOException;

import com.google.gson.JsonObject;

import usr.skyswimmer.githubwebhooks.api.util.JsonUtils;

public class WebhookEntity implements IBaseJsonConfigEntity {

	public JsonObject json;
	public String type = "hook";

	public String secret;
	public String repository;
	public String url;

	public String app;

	@Override
	public void loadFromJson(JsonObject source, String scope) throws IOException {
		json = source;
		if (source.has("type"))
			type = source.get("type").getAsString();
		secret = JsonUtils.getElementOrError(scope, source, "secret").getAsString();
		if (type.equals("hook")) {
			repository = JsonUtils.getElementOrError(scope, source, "repository").getAsString();
			url = JsonUtils.getElementOrError(scope, source, "url").getAsString();
		} else if (type.equals("app")) {
			app = JsonUtils.getElementOrError(scope, source, "app").getAsString();
		} else
			throw new IOException(scope + " value for " + type + " is invalid, expected 'app' or 'hook'");
	}

}
