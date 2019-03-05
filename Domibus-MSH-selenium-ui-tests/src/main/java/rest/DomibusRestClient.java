package rest;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.PROPERTIES;
import utils.TestDataProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DomibusRestClient {

	private static Client client = Client.create();
	private static WebResource resource = client.resource(PROPERTIES.UI_BASE_URL);
	private static TestDataProvider dataProvider = new TestDataProvider();

	private String sanitizeResponse(String response) {
		return response.replaceFirst("\\)]}',\n", "");
	}

	private ClientResponse requestGET(WebResource resource, HashMap<String, String> params, List<NewCookie> cookies) {

		if (params != null) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				resource = resource.queryParam(param.getKey(), param.getValue());
			}
		}

		WebResource.Builder builder = resource.getRequestBuilder();

		if (cookies != null) {
			for (NewCookie cookie : cookies) {
				builder = builder.cookie(cookie);
			}
		}

		return builder.get(ClientResponse.class);
	}

	private ClientResponse requestPOST(WebResource resource, HashMap<String, String> params, List<NewCookie> cookies) {

		WebResource.Builder builder = resource.getRequestBuilder();
		String xrfTokenValue = "";

		if (cookies != null) {
			for (NewCookie cookie : cookies) {
				builder = builder.cookie(cookie.toCookie());
				if (cookie.getName().equalsIgnoreCase("XSRF-TOKEN")) {
					xrfTokenValue = cookie.getValue();
				}
			}
		}

		if (xrfTokenValue != null) {
			builder = builder.header("X-XSRF-TOKEN", xrfTokenValue);
		}


		JSONObject object = new JSONObject();
		if (params != null) {
			object = new JSONObject(params);
		}

		return builder
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, object.toString());
	}

	private ClientResponse requestPOST(WebResource resource, String params, List<NewCookie> cookies) {

		WebResource.Builder builder = resource.getRequestBuilder();
		String xrfTokenValue = "";

		if (cookies != null) {
			for (NewCookie cookie : cookies) {
				builder = builder.cookie(cookie.toCookie());
				if (cookie.getName().equalsIgnoreCase("XSRF-TOKEN")) {
					xrfTokenValue = cookie.getValue();
				}
			}
		}

		if (xrfTokenValue != null) {
			builder = builder.header("X-XSRF-TOKEN", xrfTokenValue);
		}


		return builder
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, params);
	}

	private ClientResponse requestPOSTFile(WebResource resource, String filePath, HashMap<String, String> fields, List<NewCookie> cookies) {

		WebResource.Builder builder = resource.getRequestBuilder();
		String xrfTokenValue = "";

		if (cookies != null) {
			for (NewCookie cookie : cookies) {
				builder = builder.cookie(cookie.toCookie());
				if (cookie.getName().equalsIgnoreCase("XSRF-TOKEN")) {
					xrfTokenValue = cookie.getValue();
				}
			}
		}

		if (xrfTokenValue != null) {
			builder = builder.header("X-XSRF-TOKEN", xrfTokenValue);
		}

		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(filePath).getFile());
		FileDataBodyPart filePart = new FileDataBodyPart("file", file);
		FormDataMultiPart multipartEntity = new FormDataMultiPart(); //.bodyPart(filePart).;
		for (String s : fields.keySet()) {
			multipartEntity.field(s, fields.get(s));
		}
		MultiPart multipart = multipartEntity.bodyPart(filePart);

		return builder.type(MediaType.MULTIPART_FORM_DATA_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.accept(MediaType.TEXT_PLAIN_TYPE)
				.post(ClientResponse.class, multipartEntity);
	}

	private ClientResponse requestPUT(WebResource resource, String params, List<NewCookie> cookies) {

		WebResource.Builder builder = resource.getRequestBuilder();
		String xrfTokenValue = "";

		if (cookies != null) {
			for (NewCookie cookie : cookies) {
				builder = builder.cookie(cookie.toCookie());
				if (cookie.getName().equalsIgnoreCase("XSRF-TOKEN")) {
					xrfTokenValue = cookie.getValue();
				}
			}
		}

		if (xrfTokenValue != null) {
			builder = builder.header("X-XSRF-TOKEN", xrfTokenValue);
		}

		return builder
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.put(ClientResponse.class, params);
	}

	public List<NewCookie> login(String username, String password) {
		HashMap<String, String> params = new HashMap<>();
		params.put("username", username);
		params.put("password", password);

		ClientResponse response = requestPOST(resource.path(RestServicePaths.LOGIN), params, null);

		if (response.getStatus() == 200) {
			return response.getCookies();
		}
		return null;
	}

	private HashMap<String, String> getAdminUser() {
		return dataProvider.getAdminUser();
	}

	public JSONArray getUsers() {
		HashMap<String, String> user = getAdminUser();
		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));

		ClientResponse response = requestGET(resource.path(RestServicePaths.USERS), null, cookies);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not get users ");
		}

		try {
			String rawResp = response.getEntity(String.class);
			return new JSONArray(sanitizeResponse(rawResp));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void createUser(String username, String role, String pass, String domain) {
		HashMap<String, String> user = getAdminUser();

		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));
		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		} else {
			domain = "default";
		}

		String payloadTemplate = "[{\"roles\":\"%s\",\"domain\":\"%s\",\"userName\":\"%s\",\"email\":\"\",\"password\":\"%s\",\"status\":\"NEW\",\"active\":true,\"suspended\":false,\"authorities\":[],\"deleted\":false,\"$$index\":2}]";
		int index = getUsers().length();
		String payload = String.format(payloadTemplate, role, domain, username, pass);


		ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), payload, cookies);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not create user");
		}
	}

	public void deleteUser(String username, String domain) throws Exception {
		HashMap<String, String> user = getAdminUser();

		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));

		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		}

		String getResponse = requestGET(resource.path(RestServicePaths.USERS), null, cookies).getEntity(String.class);

		JSONArray pusers = new JSONArray(sanitizeResponse(getResponse));
		JSONArray toDelete = new JSONArray();
		for (int i = 0; i < pusers.length(); i++) {
			if (pusers.getJSONObject(i).getString("userName").equalsIgnoreCase(username)) {
				JSONObject tmpUser = pusers.getJSONObject(i);
				tmpUser.put("status", "REMOVED");
				tmpUser.put("deleted", true);
				tmpUser.put("$$index", 0);
				toDelete.put(tmpUser);
			}
		}

		ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), toDelete.toString(), cookies);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not delete user");
		}
	}


	public void createPluginUser(String username, String role, String pass, String domain) {
		HashMap<String, String> user = getAdminUser();
		String payloadTemplate = "[{\"status\":\"NEW\",\"userName\":\"%s\",\"active\":true,\"suspended\":false,\"authenticationType\":\"BASIC\",\"$$index\":0,\"authRoles\":\"%s\",\"password\":\"%s\"}]";
		String payload = String.format(payloadTemplate, username, role, pass);

		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));

		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		}
		ClientResponse response = requestPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload, cookies);
		if (response.getStatus() != 204) {
			throw new RuntimeException("Could not create plugin user");
		}
	}

	public void deletePluginUser(String username, String domain) throws Exception {
		HashMap<String, String> user = getAdminUser();

		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));

		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		}

		String getResponse = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), null, cookies).getEntity(String.class);

		JSONArray pusers = new JSONObject(sanitizeResponse(getResponse)).getJSONArray("entries");
		JSONArray toDelete = new JSONArray();
		for (int i = 0; i < pusers.length(); i++) {
			if (pusers.getJSONObject(i).getString("username").equalsIgnoreCase(username)) {
				JSONObject tmpUser = pusers.getJSONObject(i);
				tmpUser.put("status", "REMOVED");
				toDelete.put(tmpUser);
			}
		}

		ClientResponse response = requestPUT(resource.path(RestServicePaths.PLUGIN_USERS), toDelete.toString(), cookies);
		if (response.getStatus() != 204) {
			throw new RuntimeException("Could not delete plugin user");
		}
	}

	public void updateUser(String username, HashMap<String, String> toUpdate) {
		HashMap<String, String> adminUser = getAdminUser();
		JSONObject user = null;

		try {
			JSONArray array = getUsers();
			for (int i = 0; i < array.length(); i++) {
				JSONObject tmpUser = array.getJSONObject(i);
				if (tmpUser.getString("userName").equalsIgnoreCase(username)) {
					user = tmpUser;
				}
			}

			if (null == user) {
				return;
			}

			for (Map.Entry<String, String> entry : toUpdate.entrySet()) {
				user.put(entry.getKey(), entry.getValue());
			}

			user.put("status", "UPDATED");

			List<NewCookie> cookies = login(adminUser.get("username"), adminUser.get("pass"));
			ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), "[" + user.toString() + "]", cookies);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Could not UPDATE user");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public List<String> getDomainNames() {
		HashMap<String, String> adminUser = getAdminUser();
		List<NewCookie> cookies = login(adminUser.get("username"), adminUser.get("pass"));

		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMAINS), null, cookies);
		if (response.getStatus() != 200) {
			return null;
		}
		String rawStringResponse = response.getEntity(String.class);

		List<String> toReturn = null;
		try {
			JSONArray domainArray = new JSONArray(sanitizeResponse(rawStringResponse));
			toReturn = new ArrayList<>();
			for (int i = 0; i < domainArray.length(); i++) {
				toReturn.add(domainArray.getJSONObject(i).getString("name"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	public void switchDomain(List<NewCookie> cookies, String domainName) {

		WebResource.Builder builder = resource.path(RestServicePaths.SESSION_DOMAIN).getRequestBuilder();
		String xrfTokenValue = "";

		if (cookies != null) {
			for (NewCookie cookie : cookies) {
				System.out.println("cookie = " + cookie);
				builder = builder.cookie(cookie.toCookie());
				if (cookie.getName().equalsIgnoreCase("XSRF-TOKEN")) {
					xrfTokenValue = cookie.getValue();
				}
			}
		}

		if (xrfTokenValue != null) {
			builder = builder.header("X-XSRF-TOKEN", xrfTokenValue);
		}

		builder.accept(MediaType.TEXT_PLAIN_TYPE).type(MediaType.TEXT_PLAIN_TYPE)
				.put(ClientResponse.class, domainName);


	}

	public void createMessageFilter(String actionName, String domain) {
		HashMap<String, String> user = getAdminUser();
		String payloadTemplate = "{\"entityId\":0,\"index\":0,\"backendName\":\"backendWebservice\",\"routingCriterias\":[{\"entityId\":0,\"name\":\"action\",\"expression\":\"%s\"}],\"persisted\":false,\"from\":null,\"to\":null,\"action\":{\"entityId\":0,\"name\":\"action\",\"expression\":\"%s\"},\"service\":null,\"$$index\":2}";
		String payload = String.format(payloadTemplate, actionName, actionName);

		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));

		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		}

		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null, cookies).getEntity(String.class);
		JSONArray currentMSGF = null;
		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");
			currentMSGF.put(new JSONObject(payload));
		} catch (JSONException e) {
			e.printStackTrace();
		}


		ClientResponse response = requestPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), currentMSGF.toString(), cookies);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not message filter");
		}
	}

	public void deleteMessageFilter(String actionName, String domain) {
		HashMap<String, String> user = getAdminUser();
		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));

		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		}

		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null, cookies).getEntity(String.class);
		JSONArray currentMSGF = null;
		JSONArray deletedL = new JSONArray();

		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");

			for (int i = 0; i < currentMSGF.length(); i++) {
				JSONObject filter = currentMSGF.getJSONObject(i);
				if (!filter.toString().contains(actionName)) {
					deletedL.put(filter);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}


		ClientResponse response = requestPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), deletedL.toString(), cookies);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not message filter");
		}
	}


	public void uploadPMode(String pmodeFilePath, String domain) {
		HashMap<String, String> user = getAdminUser();
		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));
		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		}
		HashMap<String, String> fields = new HashMap<>();
		fields.put("description", "automatic red");
		requestPOSTFile(resource.path(RestServicePaths.PMODE), pmodeFilePath, fields, cookies);

	}

	public boolean isPmodeUploaded(String domain) throws Exception {
		HashMap<String, String> user = getAdminUser();
		List<NewCookie> cookies = login(user.get("username"), user.get("pass"));
		if (null != domain && !domain.isEmpty()) {
			switchDomain(cookies, domain);
		}

		String getResponse = requestGET(resource.path(RestServicePaths.PMODE_LIST), null, cookies).getEntity(String.class);
		JSONArray entries = new JSONArray(sanitizeResponse(getResponse));

		return entries.length() > 0;
	}


}
