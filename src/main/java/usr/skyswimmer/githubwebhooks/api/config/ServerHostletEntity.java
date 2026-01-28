package usr.skyswimmer.githubwebhooks.api.config;

import java.io.IOException;

import com.google.gson.JsonObject;

import usr.skyswimmer.githubwebhooks.api.util.JsonUtils;

public class ServerHostletEntity implements IBaseJsonConfigEntity {

	public ConnectiveAdapterEntity adapter;

	@Override
	public void loadFromJson(JsonObject source, String scope) throws IOException {
		// Adapter
		JsonObject adapter = JsonUtils.getElementOrError(scope, source, "adapter").getAsJsonObject();
		this.adapter = new ConnectiveAdapterEntity();
		this.adapter.loadFromJson(adapter, scope + " -> adapter");
	}

}
