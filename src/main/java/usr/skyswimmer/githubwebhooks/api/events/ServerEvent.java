package usr.skyswimmer.githubwebhooks.api.events;

import usr.skyswimmer.githubwebhooks.api.server.GithubWebhookEventServer;
import usr.skyswimmer.githubwebhooks.util.events.EventObject;

public class ServerEvent extends EventObject {
	private GithubWebhookEventServer server;

	public ServerEvent(GithubWebhookEventServer server) {
		this.server = server;
	}

	public GithubWebhookEventServer getServer() {
		return server;
	}

}
