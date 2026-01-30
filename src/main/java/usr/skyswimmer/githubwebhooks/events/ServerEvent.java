package usr.skyswimmer.githubwebhooks.events;

import usr.skyswimmer.githubwebhooks.server.GithubWebhookEventServer;
import usr.skyswimmer.quicktoolsutils.events.EventObject;

public class ServerEvent extends EventObject {
	private GithubWebhookEventServer server;

	public ServerEvent(GithubWebhookEventServer server) {
		this.server = server;
	}

	public GithubWebhookEventServer getServer() {
		return server;
	}

}
