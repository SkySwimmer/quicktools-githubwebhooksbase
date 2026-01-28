package usr.skyswimmer.githubwebhooks.api.server;

import java.io.File;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asf.connective.ConnectiveHttpServer;
import org.asf.connective.lambda.LambdaPushContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import usr.skyswimmer.githubwebhooks.api.config.ServerHostletEntity;
import usr.skyswimmer.githubwebhooks.api.config.WebhookEntity;
import usr.skyswimmer.githubwebhooks.api.events.StartServerEvent;
import usr.skyswimmer.githubwebhooks.api.events.StartWebserverEvent;
import usr.skyswimmer.githubwebhooks.api.events.StopServerEvent;
import usr.skyswimmer.githubwebhooks.api.events.StopWebserverEvent;
import usr.skyswimmer.githubwebhooks.api.events.WebhookActivateEvent;
import usr.skyswimmer.githubwebhooks.connective.logger.Log4jManagerImpl;
import usr.skyswimmer.githubwebhooks.util.HashUtils;
import usr.skyswimmer.githubwebhooks.util.JsonUtils;
import usr.skyswimmer.githubwebhooks.util.events.Event;
import usr.skyswimmer.githubwebhooks.util.events.EventBus;

public class GithubWebhookEventServer {

	private static boolean debugMode = false;
	static {
		// Setup logging
		if (System.getProperty("debugMode") != null) {
			System.setProperty("log4j2.configurationFile",
					GithubWebhookEventServer.class.getResource("/log4j2-ide.xml").toString());
			debugMode = true;
		} else {
			System.setProperty("log4j2.configurationFile",
					GithubWebhookEventServer.class.getResource("/log4j2.xml").toString());
		}
		new Log4jManagerImpl().assignAsMain();
	}

	private Logger logger;
	private File configFile;
	private File workingDir;
	private boolean running;

	private ConnectiveHttpServer server;

	private Event<StartServerEvent> startEvent = new Event<StartServerEvent>();
	private Event<StartWebserverEvent> startWebserverEvent = new Event<StartWebserverEvent>();
	private Event<StopServerEvent> stopEvent = new Event<StopServerEvent>();
	private Event<StopWebserverEvent> stopWebserverEvent = new Event<StopWebserverEvent>();
	private Event<WebhookActivateEvent> webhookActivateEvent = new Event<WebhookActivateEvent>();

	public Event<StartWebserverEvent> onStartWebserver() {
		return startWebserverEvent;
	}

	public Event<StartServerEvent> onStart() {
		return startEvent;
	}

	public Event<StopWebserverEvent> onStopWebserver() {
		return stopWebserverEvent;
	}

	public Event<StopServerEvent> onStop() {
		return stopEvent;
	}

	public Event<WebhookActivateEvent> onWebhookActivate() {
		return webhookActivateEvent;
	}

	/**
	 * Creates the server instance
	 * 
	 * @param configFile Configuration file
	 * @param scope      Scope name
	 */
	public GithubWebhookEventServer(File configFile, String scope) {
		this.configFile = configFile;
		this.workingDir = configFile.getParentFile();
		logger = LogManager.getLogger(scope);
	}

	/**
	 * Retrieves the logger instance used by the server
	 * 
	 * @return Logger instance
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Initializes the server
	 * 
	 * @throws IOException If initialization fails
	 */
	public void initServer() throws IOException {
		// Load configuration
		logger.info("Loading configuration...");
		JsonObject config = loadConfig();

		// Load working directory
		if (config.has("workingDirectory")) {
			String dir = config.get("workingDirectory").getAsString();
			File direct = new File(dir);
			if (direct.isAbsolute())
				direct = new File(workingDir, dir);
			if (!direct.exists() || !direct.isDirectory())
				throw new IOException(
						"Working directory does not exist or is not a directory: " + direct.getAbsolutePath());
			workingDir = direct;
		}

		// Create server
		logger.info("Loading server settings...");
		ServerHostletEntity host = new ServerHostletEntity();
		host.loadFromJson(JsonUtils.getElementOrError("config", config, "webserver").getAsJsonObject(), "config");

		// Adapter
		if (ConnectiveHttpServer.findAdapter(host.adapter.protocol) == null)
			throw new IOException("Protocol adapter unrecognized: " + host.adapter.protocol);

		// Create instance
		logger.info("Creating server instance...");
		server = ConnectiveHttpServer.create(host.adapter.protocol, host.adapter.parameters);

		// Create server
		server.registerHandler("/", (LambdaPushContext req) -> {
			String path = req.getRequestPath();

			// Load config
			JsonObject conf;
			try {
				conf = loadConfig();
			} catch (Exception e) {
				logger.error("Could not load configuration file: " + configFile, e);
				req.setResponseStatus(500, "Internal Server Error");
				return;
			}

			// Get webhooks list
			JsonObject webhooksConf;
			try {
				webhooksConf = JsonUtils.getElementOrError("config", conf, "webhooks").getAsJsonObject();
			} catch (Exception e) {
				logger.error(
						"Could not load configuration file: " + configFile + ": failure loading webhooks configuration",
						e);
				req.setResponseStatus(500, "Internal Server Error");
				return;
			}

			// Get webhook
			if (!webhooksConf.has(path)) {
				// Cancel
				req.setResponseStatus(404, "Not Found");
				return;
			}

			// Get webhook
			WebhookEntity webhook = new WebhookEntity();
			try {
				webhook.loadFromJson(webhooksConf.get(path).getAsJsonObject(), "webhook " + path);
			} catch (Exception e) {
				logger.error("Could not load webhook " + path + ": error loading webhook sheet", e);
				req.setResponseStatus(500, "Internal Server Error");
				return;
			}

			// Verify secret
			if (!req.hasHeader("X-Hub-Signature-256")) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " lacked a signature");
				req.setResponseStatus(403, "Forbidden");
				return;
			}
			String sig = req.getHeader("X-Hub-Signature-256");
			if (!sig.startsWith("sha256=")) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " malformed signature");
				req.setResponseStatus(403, "Forbidden");
				return;
			}
			sig = sig.substring("sha256=".length());
			String body = req.getRequestBodyAsString();
			if (body.isEmpty()) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " provided no content");
				req.setResponseStatus(400, "Bad Request");
				return;
			}
			String expectedSig = HashUtils.hmac256(body.getBytes("UTF-8"), webhook.secret);
			if (!sig.equals(expectedSig)) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " signature mismatch");
				req.setResponseStatus(403, "Forbidden");
				return;
			}

			// Check content type
			if (!req.hasHeader("Content-Type")) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " provided no content type");
				req.setResponseStatus(400, "Bad Request");
				return;
			}
			if (!req.getHeader("Content-Type").equals("application/json")) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " used incorrect content type: " + req.getHeader("Content-Type"));
				req.setResponseStatus(400, "Bad Request");
				return;
			}

			// Success
			// Parse json
			JsonObject hook;
			try {
				hook = JsonParser.parseString(body).getAsJsonObject();
			} catch (Exception e) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " used a malformed json", e);
				req.setResponseStatus(400, "Bad request");
				return;
			}

			// Check
			JsonObject repository;
			try {
				repository = JsonUtils.getElementOrError("webhook payload", hook, "repository").getAsJsonObject();
			} catch (Exception e) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " used a malformed json: could not parse repository statement", e);
				req.setResponseStatus(400, "Bad request");
				return;
			}
			if (!repository.has("full_name")) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " used a malformed json: repository block misses the full_name property");
				req.setResponseStatus(400, "Bad request");
				return;
			}
			if (!repository.get("full_name").getAsString().equals(webhook.repository)) {
				logger.error("Webhook request " + req.getRequestMethod() + " " + req.getRequestPath()
						+ " used unexpected repository string " + repository.get("full_name").getAsString()
						+ " while expecting " + webhook.repository);
				req.setResponseStatus(403, "Forbidden");
				return;
			}

			// Call event
			WebhookActivateEvent ev = new WebhookActivateEvent(this, server, webhook, hook, req);
			webhookActivateEvent.dispatchEvent(ev);
			if (ev.isHandled())
				return;
			EventBus.getInstance().dispatchEvent(ev);
		}, true, true, "POST", "GET", "DELETE", "PUT", "PATCH");

	}

	/**
	 * Checks if the server is running
	 * 
	 * @return True if running, false otherwise
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Starts all servers
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		if (running)
			throw new IllegalStateException("Server already running!");
		running = true;
		logger.info("Starting servers...");
		startEvent.dispatchEvent(new StartServerEvent(this));
		EventBus.getInstance().dispatchEvent(new StartServerEvent(this));
		startWebserverEvent.dispatchEvent(new StartWebserverEvent(this, server));
		EventBus.getInstance().dispatchEvent(new StartWebserverEvent(this, server));
		server.start();
		logger.info("Started successfully!");
	}

	/**
	 * Stops the HTTP server
	 * 
	 * @throws IOException If stopping the server fails
	 */
	public void stop() throws IOException {
		if (!running)
			return;
		logger.info("Stopping servers...");
		stopEvent.dispatchEvent(new StopServerEvent(this));
		EventBus.getInstance().dispatchEvent(new StopServerEvent(this));
		server.stop();
		stopWebserverEvent.dispatchEvent(new StopWebserverEvent(this, server));
		EventBus.getInstance().dispatchEvent(new StopWebserverEvent(this, server));
		running = false;
		logger.info("Stopped successfully!");
	}

	/**
	 * Stops the HTTP server without waiting for all clients to disconnect
	 * 
	 * @throws IOException If stopping the server fails
	 */
	public void stopForced() throws IOException {
		if (!running)
			return;
		logger.info("Stopping servers...");
		stopEvent.dispatchEvent(new StopServerEvent(this));
		EventBus.getInstance().dispatchEvent(new StopServerEvent(this));
		server.stopForced();
		stopWebserverEvent.dispatchEvent(new StopWebserverEvent(this, server));
		EventBus.getInstance().dispatchEvent(new StopWebserverEvent(this, server));
		running = false;
		logger.info("Stopped successfully!");
	}

	/**
	 * Waits for the server to shut down
	 */
	public void waitForExit() {
		server.waitForExit();
	}

	/**
	 * Reads the server config file
	 * 
	 * @throws IOException If the config fails to load
	 */
	public JsonObject loadConfig() throws IOException {
		try {
			return JsonUtils.loadConfig(configFile);
		} catch (Exception e) {
			throw new IOException("Invalid config file", e);
		}
	}

	/**
	 * Retrieves the HTTP server instance
	 * 
	 * @return ConnectiveHttpServer instances
	 */
	public ConnectiveHttpServer getServer() {
		return server;
	}

	/**
	 * Retrieves the working directory
	 * 
	 * @return Working directory file
	 */
	public File getWorkingDir() {
		return workingDir;
	}

	/**
	 * Retrieves the configuration file
	 * 
	 * @return Configuration directory file
	 */
	public File getConfigFile() {
		return configFile;
	}

	/**
	 * Checks if running in debug mode
	 * 
	 * @return True if in debug mode, false otherwise
	 */
	public boolean isDebugMode() {
		return debugMode;
	}
}
