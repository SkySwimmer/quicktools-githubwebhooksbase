package usr.skyswimmer.githubwebhooks.api.config;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.util.io.pem.PemReader;

import com.google.gson.JsonObject;

import usr.skyswimmer.githubwebhooks.api.util.JsonUtils;

public class AppEntity implements ISerializedJsonEntity {

	public File workingDir;
	public String appId;

	public JsonObject json;

	public PrivateKey keyInstance;

	public String api = "https://api.github.com/";

	public String clientId;
	public String secret;
	public String key;

	public AppEntity(File workingDir, String appId) {
		this.appId = appId;
	}

	@Override
	public void loadFromJson(JsonObject source, String scope) throws IOException {
		json = source;
		if (source.has("secret"))
			secret = source.get("secret").getAsString();
		if (source.has("api"))
			api = source.get("api").getAsString();
		clientId = JsonUtils.getElementOrError(scope, source, "clientId").getAsString();
		key = JsonUtils.getElementOrError(scope, source, "key").getAsString();
	}

	public void loadKey() throws IOException {
		File appKey = new File(workingDir, key);
		if (!appKey.exists()) {
			throw new IOException("Key file " + appKey + " does not exist");
		}
		try {
			KeyFactory fac = KeyFactory.getInstance("RSA");
			String pem = Files.readString(appKey.toPath());
			PemReader reader = new PemReader(new StringReader(pem));
			keyInstance = fac.generatePrivate(new PKCS8EncodedKeySpec(reader.readPemObject().getContent()));
			reader.close();
		} catch (Exception e) {
			throw new IOException("Key file '" + appKey + "' failed to load", e);
		}
	}

}
