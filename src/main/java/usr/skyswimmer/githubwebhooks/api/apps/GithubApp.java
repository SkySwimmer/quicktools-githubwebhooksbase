package usr.skyswimmer.githubwebhooks.api.apps;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivateKey;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import usr.skyswimmer.githubwebhooks.api.config.AppEntity;

public class GithubApp {

	private AppEntity entity;

	public GithubApp(AppEntity config) throws IOException {
		entity = config;
		if (entity.keyInstance == null)
			entity.loadKey();
	}

	public PrivateKey getKey() {
		return entity.keyInstance;
	}

	public String getId() {
		return entity.appId;
	}

	public AppEntity getConfig() {
		return entity;
	}

	public JsonObject appApiRequest(String url, String method, JsonObject body) throws IOException {
		// Get app token
		String appToken = GithubAppTokens.getOrCreateAuthToken(this);

		// Request token
		String uF = entity.api;
		if (!uF.endsWith("/"))
			uF += "/";
		if (url.startsWith("/"))
			url = url.substring(1);
		uF += url;
		URL u = new URL(uF);

		// Create request
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod(method);
		conn.addRequestProperty("Authorization", "Bearer " + appToken);
		conn.addRequestProperty("Accept", "application/vnd.github+json");
		if (body != null) {
			conn.setDoOutput(true);
			conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
			conn.getOutputStream().close();
		}
		InputStream strm = conn.getInputStream();
		try {
			// Read response
			byte[] resp = strm.readAllBytes();
			String res = new String(resp, "UTF-8");
			return JsonParser.parseString(res).getAsJsonObject();
		} finally {
			strm.close();
		}
	}

	public JsonObject apiRequest(String url, String method, JsonObject body) throws IOException {
		// Request token
		String uF = entity.api;
		if (!uF.endsWith("/"))
			uF += "/";
		if (url.startsWith("/"))
			url = url.substring(1);
		uF += url;
		URL u = new URL(uF);

		// Create request
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod(method);
		if (body != null) {
			conn.setDoOutput(true);
			conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
			conn.getOutputStream().close();
		}
		InputStream strm = conn.getInputStream();
		try {
			// Read response
			byte[] resp = strm.readAllBytes();
			String res = new String(resp, "UTF-8");
			return JsonParser.parseString(res).getAsJsonObject();
		} finally {
			strm.close();
		}
	}

	public JsonObject appInstallationApiRequest(String installationId, String url, String method, JsonObject body)
			throws IOException {
		// Get app token
		String appToken = GithubAppInstallationTokens.getOrRequestInstallationAuthToken(this, installationId);

		// Request token
		String uF = entity.api;
		if (!uF.endsWith("/"))
			uF += "/";
		if (url.startsWith("/"))
			url = url.substring(1);
		uF += url;
		URL u = new URL(uF);

		// Create request
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setRequestMethod("POST");
		conn.addRequestProperty("Authorization", "Bearer " + appToken);
		conn.addRequestProperty("Accept", "application/json");
		if (body != null) {
			conn.setDoOutput(true);
			conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
			conn.getOutputStream().close();
		}
		InputStream strm = conn.getInputStream();
		try {
			// Read response
			byte[] resp = strm.readAllBytes();
			String res = new String(resp, "UTF-8");
			return JsonParser.parseString(res).getAsJsonObject();
		} finally {
			strm.close();
		}
	}
}
