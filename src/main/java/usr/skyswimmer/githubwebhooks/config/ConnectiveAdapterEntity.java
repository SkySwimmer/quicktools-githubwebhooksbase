package usr.skyswimmer.githubwebhooks.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.google.gson.JsonObject;

import usr.skyswimmer.quicktoolsutils.json.ISerializedJsonEntity;
import usr.skyswimmer.quicktoolsutils.json.JsonUtils;

public class ConnectiveAdapterEntity implements ISerializedJsonEntity {

	public String protocol;
	public HashMap<String, String> parameters = new LinkedHashMap<String, String>();

	@Override
	public void loadFromJson(JsonObject source, String scope) throws IOException {
		protocol = JsonUtils.getElementOrError(scope, source, "protocol").getAsString();
		parameters.putAll(JsonUtils
				.objectAsStringHashMap(JsonUtils.getElementOrError(scope, source, "parameters").getAsJsonObject()));
	}

}
