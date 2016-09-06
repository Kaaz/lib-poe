package org.libpoe.net;

import com.google.gson.stream.JsonReader;
import org.libpoe.model.StashTab;
import org.libpoe.util.Constants;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by Johan on 2014-02-11.
 * TODO: Add throttle to prevent lockout from GGG server
 */
public class DataReader {
	private static final Pattern usernamePattern = Pattern.compile("<title>View Profile - Path of Exile - ([a-zA-Z0-9_-]+)</title>");
	private static final String LOGIN_URL = "https://www.pathofexile.com/login";
	private static final String MY_ACCOUNT_URL = "https://www.pathofexile.com/my-account";
	private static final String CHARACTER_URL = "https://www.pathofexile.com/character-window/get-characters";
	private static final String STASH_URL = "https://www.pathofexile.com/character-window/get-stash-items?league=%s&tabs=0&tabIndex=%s&accountName=%s";
	private String accountname = "";
	private HttpURLConnection conn;

	private AuthInfo info;

	private HashMap<String, String> properties;

	public DataReader(AuthInfo info) {
		this.info = info;
		this.properties = new HashMap<String, String>();

	}

	private String sendPOST(String url, HashMap<String, String> params) throws Exception {

		StringBuilder sb = new StringBuilder();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
		}

		String p = sb.toString().subSequence(1, sb.toString().length() - 1).toString();

		conn = (HttpsURLConnection) new URL(url + p).openConnection();

		for (Map.Entry<String, String> entry : properties.entrySet()) {
			conn.setRequestProperty(entry.getKey(), entry.getValue());
		}

		conn.setUseCaches(false);

		return readContent(conn);
	}

	private String sendGET(String url) throws Exception {
		conn = (HttpURLConnection) new URL(url).openConnection();
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			conn.setRequestProperty(entry.getKey(), entry.getValue());
		}

		conn.setUseCaches(false);

		return readContent(conn);
	}

	private String readContent(HttpURLConnection conn) throws Exception {
		InputStream inStream;
		if (conn.getHeaderField("Content-Encoding") != null && conn.getHeaderField("Content-Encoding").equals("gzip")) {
			inStream = new GZIPInputStream(conn.getInputStream());
		} else {
			inStream = conn.getInputStream();
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}

	/**
	 * Authenticates with the server.
	 *
	 * @return True if successful,
	 */
	public boolean authenticate() {
		if (info.useSessionId()) {
			properties.put("Cookie", "POESESSID=" + info.getSessionId());

			try {
				System.out.println("TRYING TO LOG IN");
				String loginResponse = sendGET(MY_ACCOUNT_URL);
				Matcher m = usernamePattern.matcher(loginResponse);
				if (m.find()) {
					accountname = m.group(1);
					System.out.println(accountname);
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public StashTab getStashTab(String league, int index) throws Exception {
		String data = sendGET(String.format(STASH_URL, league, index, accountname));
		return Constants.GSON_INSTANCE.fromJson(new JsonReader(new StringReader(data)), StashTab.class);
	}
}
