package usr.skyswimmer.githubwebhooks.events;

import org.asf.connective.ConnectiveHttpServer;

import usr.skyswimmer.githubwebhooks.server.GithubWebhookEventServer;

public class StopWebserverEvent extends WebServerEvent {

	public StopWebserverEvent(GithubWebhookEventServer server, ConnectiveHttpServer webserver) {
		super(server, webserver);
	}

}
