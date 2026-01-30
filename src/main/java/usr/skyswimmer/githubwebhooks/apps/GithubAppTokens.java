package usr.skyswimmer.githubwebhooks.apps;

import java.util.HashMap;

import usr.skyswimmer.githubwebhooks.tokens.SessionToken;
import usr.skyswimmer.quicktoolsutils.tasks.async.AsyncTaskManager;

public class GithubAppTokens {

	private static HashMap<String, SessionToken> tokens = new HashMap<String, SessionToken>();

	static {
		// Token expiry
		AsyncTaskManager.runAsync(() -> {
			while (true) {
				// Go through tokens
				synchronized (tokens) {
					String[] keys = tokens.keySet().toArray(t -> new String[t]);
					for (String app : keys) {
						SessionToken tkn = tokens.get(app);
						if (tkn != null && tkn.expiry <= System.currentTimeMillis() / 1000) {
							// Expired
							tokens.remove(app);
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

	public static String getOrCreateAuthToken(GithubApp app) {
		SessionToken res = null;
		synchronized (tokens) {
			SessionToken tkn = tokens.get(app.getId());
			if (tkn != null && tkn.expiry - (3 * 60) > System.currentTimeMillis() / 1000) {
				// Valid
				res = tkn;
			} else if (tkn != null) {
				// Expired
				tokens.remove(app.getId());
			}
		}

		// Return if needed
		if (res != null)
			return res.toTokenString(app.getKey());

		// Create new token
		return createNewAuthToken(app);
	}

	public static String createNewAuthToken(GithubApp app) {
		SessionToken tkn = new SessionToken();
		tkn.issuer = app.getConfig().clientId;
		tkn.expiry = (System.currentTimeMillis() + 10 * 60 * 1000) / 1000;
		tkn.stamp();
		return createNewAuthToken(app, tkn);
	}

	public static String createNewAuthToken(GithubApp app, SessionToken tkn) {
		// Update tokens
		synchronized (tokens) {
			tokens.put(app.getId(), tkn);
		}

		// Sign
		return tkn.toTokenString(app.getKey());
	}

}
