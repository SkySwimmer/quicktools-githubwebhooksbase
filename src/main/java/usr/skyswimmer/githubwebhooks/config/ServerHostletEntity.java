package usr.skyswimmer.githubwebhooks.config;

import java.io.IOException;

import com.google.gson.JsonObject;

import usr.skyswimmer.quicktoolsutils.json.ISerializedJsonEntity;
import usr.skyswimmer.quicktoolsutils.json.JsonUtils;

public class ServerHostletEntity implements ISerializedJsonEntity {

	public ConnectiveAdapterEntity adapter;

	@Override
	public void loadFromJson(JsonObject source, String scope) throws IOException {
		// Adapter
		JsonObject adapter = JsonUtils.getElementOrError(scope, source, "adapter").getAsJsonObject();
		this.adapter = new ConnectiveAdapterEntity();
		this.adapter.loadFromJson(adapter, scope + " -> adapter");
	}

}
