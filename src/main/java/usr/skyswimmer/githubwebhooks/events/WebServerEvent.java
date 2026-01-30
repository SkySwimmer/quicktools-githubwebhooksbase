package usr.skyswimmer.githubwebhooks.events;

import org.asf.connective.ConnectiveHttpServer;

import usr.skyswimmer.githubwebhooks.server.GithubWebhookEventServer;

public class WebServerEvent extends ServerEvent {

	private ConnectiveHttpServer webserver;

	public WebServerEvent(GithubWebhookEventServer server, ConnectiveHttpServer webserver) {
		super(server);
		this.webserver = webserver;
	}

	public ConnectiveHttpServer getWebserver() {
		return webserver;
	}
}
