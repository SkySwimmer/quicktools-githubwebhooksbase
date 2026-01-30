package usr.skyswimmer.githubwebhooks.events;

import org.asf.connective.ConnectiveHttpServer;

import usr.skyswimmer.githubwebhooks.server.GithubWebhookEventServer;

public class StartWebserverEvent extends WebServerEvent {

	public StartWebserverEvent(GithubWebhookEventServer server, ConnectiveHttpServer webserver) {
		super(server, webserver);
	}

}
