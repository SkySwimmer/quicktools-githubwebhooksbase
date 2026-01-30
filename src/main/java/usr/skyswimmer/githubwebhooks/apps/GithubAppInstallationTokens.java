package usr.skyswimmer.githubwebhooks.apps;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

import com.google.gson.JsonObject;

import usr.skyswimmer.githubwebhooks.tokens.GithubAppToken;
import usr.skyswimmer.quicktoolsutils.json.JsonUtils;
import usr.skyswimmer.quicktoolsutils.tasks.async.AsyncTaskManager;

public class GithubAppInstallationTokens {

	private static HashMap<String, GithubAppToken> tokens = new HashMap<String, GithubAppToken>();

	static {
		// Token expiry
		AsyncTaskManager.runAsync(() -> {
			while (true) {
				// Go through tokens
				synchronized (tokens) {
					String[] keys = tokens.keySet().toArray(t -> new String[t]);
					for (String keyStr : keys) {
						GithubAppToken tkn = tokens.get(keyStr);
						if (tkn != null && tkn.expiry <= System.currentTimeMillis() / 1000) {
							// Expired
							tokens.remove(keyStr);
						}
					}
				}

				// Wait
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					break;
				}
			}
		});
	}

	public static String getOrRequestInstallationAuthToken(GithubApp app, String installationId) throws IOException {
		GithubAppToken res = null;
		synchronized (tokens) {
			GithubAppToken tkn = tokens.get(app.getId() + "-" + installationId);
			if (tkn != null && tkn.expiry - (3 * 60) > System.currentTimeMillis() / 1000) {
				// Valid
				res = tkn;
			} else if (tkn != null) {
				// Expired
				tokens.remove(app.getId() + "-" + installationId);
			}
		}

		// Return if needed
		if (res != null)
			return res.token;

		// Create new token
		return requestInstallationAuthToken(app, installationId);
	}

	public static String requestInstallationAuthToken(GithubApp app, String installationId) throws IOException {
		try {
			// Request token
			JsonObject response = app.appApiRequest("/app/installations/" + installationId + "/access_tokens", "POST",
					null);
			String token = JsonUtils.getElementOrError("response", response, "token").getAsString();
			String expiresStrinbg = JsonUtils.getElementOrError("response", response, "expires_at").getAsString();

			// Parse expiry
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ssXXX");
			fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
			long expiry = fmt.parse(expiresStrinbg).getTime();

			// Create instance
			GithubAppToken tkn = new GithubAppToken();
			tkn.expiry = expiry;
			tkn.token = token;

			// Add
			synchronized (tokens) {
				tokens.put(app.getId() + "-" + installationId, tkn);
			}

			// Return
			return tkn.token;
		} catch (Exception e) {
			throw new IOException("API request failed", e);
		}
	}

}
