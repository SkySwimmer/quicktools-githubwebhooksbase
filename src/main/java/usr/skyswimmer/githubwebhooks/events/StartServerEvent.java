package usr.skyswimmer.githubwebhooks.events;

import usr.skyswimmer.githubwebhooks.server.GithubWebhookEventServer;

public class StartServerEvent extends ServerEvent {

	public StartServerEvent(GithubWebhookEventServer server) {
		super(server);
	}

}
