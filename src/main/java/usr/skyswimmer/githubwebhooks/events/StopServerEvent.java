package usr.skyswimmer.githubwebhooks.events;

import usr.skyswimmer.githubwebhooks.server.GithubWebhookEventServer;

public class StopServerEvent extends ServerEvent {

	public StopServerEvent(GithubWebhookEventServer server) {
		super(server);
	}

}
