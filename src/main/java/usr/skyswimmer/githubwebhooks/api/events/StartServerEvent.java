package usr.skyswimmer.githubwebhooks.api.events;

import usr.skyswimmer.githubwebhooks.api.server.GithubWebhookEventServer;

public class StartServerEvent extends ServerEvent {

	public StartServerEvent(GithubWebhookEventServer server) {
		super(server);
	}

}
