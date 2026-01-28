package usr.skyswimmer.githubwebhooks.api.events;

import usr.skyswimmer.githubwebhooks.api.server.GithubWebhookEventServer;

public class StopServerEvent extends ServerEvent {

	public StopServerEvent(GithubWebhookEventServer server) {
		super(server);
	}

}
