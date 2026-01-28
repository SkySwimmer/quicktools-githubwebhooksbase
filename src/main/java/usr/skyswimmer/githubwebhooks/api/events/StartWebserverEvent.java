package usr.skyswimmer.githubwebhooks.api.events;

import org.asf.connective.ConnectiveHttpServer;

import usr.skyswimmer.githubwebhooks.api.server.GithubWebhookEventServer;

public class StartWebserverEvent extends WebServerEvent {

	public StartWebserverEvent(GithubWebhookEventServer server, ConnectiveHttpServer webserver) {
		super(server, webserver);
	}

}
