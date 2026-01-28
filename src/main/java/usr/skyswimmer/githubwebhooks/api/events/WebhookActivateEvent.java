package usr.skyswimmer.githubwebhooks.api.events;

import org.asf.connective.ConnectiveHttpServer;
import org.asf.connective.lambda.LambdaPushContext;

import com.google.gson.JsonObject;

import usr.skyswimmer.githubwebhooks.api.config.WebhookEntity;
import usr.skyswimmer.githubwebhooks.api.server.GithubWebhookEventServer;

public class WebhookActivateEvent extends WebServerEvent {

	private WebhookEntity webhook;

	private String event;
	private JsonObject content;
	private LambdaPushContext req;

	public WebhookActivateEvent(GithubWebhookEventServer server, ConnectiveHttpServer webserver, WebhookEntity webhook,
			String event, JsonObject content, LambdaPushContext req) {
		super(server, webserver);
		this.webhook = webhook;
		this.content = content;
		this.req = req;
	}

	public WebhookEntity getWebhook() {
		return webhook;
	}

	public JsonObject getEventData() {
		return content;
	}

	public String getEvent() {
		return event;
	}

	public LambdaPushContext handler() {
		return req;
	}

}
