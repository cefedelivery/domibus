package rest;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.PROPERTIES;
import utils.TestDataProvider;

import javax.ws.rs.core.Cookie;
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

	private List<NewCookie> cookies;
	private String token;

	public DomibusRestClient() {
		refreshCookies();
	}

	private void refreshCookies() {
		if (isLoggedIn()) {
			return;
		}

		HashMap<String, String> user = dataProvider.getAdminUser();
		cookies = login();

		if (null != cookies) {
			for (NewCookie cookie : cookies) {
				if (cookie.getName().equalsIgnoreCase("XSRF-TOKEN")) {
					token = cookie.getValue();
				}
			}
		} else {
			throw new RuntimeException("Could not login, tests will not be able to generate necessary data!");
		}

		if (null == token) {
			throw new RuntimeException("Could not obtain XSRF token, tests will not be able to generate necessary data!");
		}
	}

	private boolean isLoggedIn() {
		WebResource.Builder builder = decorateBuilder(resource.path(RestServicePaths.USERNAME));
		String response = builder.get(ClientResponse.class).getEntity(String.class);
		return (null != response && !response.isEmpty());
	}

	private String sanitizeResponse(String response) {
		return response.replaceFirst("\\)]}',\n", "");
	}

	private WebResource.Builder decorateBuilder(WebResource resource) {

		WebResource.Builder builder = resource.getRequestBuilder();

		if (null != cookies) {
			for (NewCookie cookie : cookies) {
//				builder = builder.cookie(cookie.toCookie());
				builder = builder.cookie(
						new Cookie(cookie.getName(),
						cookie.getValue(),
								"/",
								""
						)
				);

			}
		}

		if (null != token) {
			builder = builder.header("X-XSRF-TOKEN", token);
		}
		return builder;
	}

	public List<NewCookie> login() {
		HashMap<String, String> adminUser = dataProvider.getAdminUser();
		HashMap<String, String> params = new HashMap<>();
		params.put("username", adminUser.get("username"));
		params.put("password", adminUser.get("pass"));

		ClientResponse response = resource.path(RestServicePaths.LOGIN)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, new JSONObject(params).toString());

		if (response.getStatus() == 200) {
			return response.getCookies();
		}
		return null;
	}

	public void switchDomain(String domainName) {
		if (null == domainName || domainName.isEmpty()) {
			domainName = "default";
		}

		if (getDomainNames().contains(domainName)) {
			WebResource.Builder builder = decorateBuilder(resource.path(RestServicePaths.SESSION_DOMAIN));

			builder.accept(MediaType.TEXT_PLAIN_TYPE).type(MediaType.TEXT_PLAIN_TYPE)
					.put(ClientResponse.class, domainName);
		}

	}

	//	------------------------------------------------------------------------------------------
	private ClientResponse requestGET(WebResource resource, HashMap<String, String> params) {
		if (params != null) {
			for (Map.Entry<String, String> param : params.entrySet()) {
				resource = resource.queryParam(param.getKey(), param.getValue());
			}
		}

		WebResource.Builder builder = decorateBuilder(resource);
		return builder.get(ClientResponse.class);
	}

	private ClientResponse requestPOST(WebResource resource, HashMap<String, String> params) {
		WebResource.Builder builder = decorateBuilder(resource);
		JSONObject object = new JSONObject();
		if (params != null) {
			object = new JSONObject(params);
		}

		return builder
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, object.toString());
	}

	private ClientResponse requestPOST(WebResource resource, String params) {

		WebResource.Builder builder = decorateBuilder(resource);
		return builder
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, params);
	}

	private ClientResponse requestPOSTFile(WebResource resource, String filePath, HashMap<String, String> fields) {

		WebResource.Builder builder = decorateBuilder(resource);

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

	private ClientResponse requestPUT(WebResource resource, String params) {

		WebResource.Builder builder = decorateBuilder(resource);

		return builder
//				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.put(ClientResponse.class, params);
	}

	// ------------------------------------------------------------------------------------------------------------
	public JSONArray getUsers() {
		ClientResponse response = requestGET(resource.path(RestServicePaths.USERS), null);
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
		switchDomain(domain);
		if (null == domain || domain.isEmpty()) {
			domain = "default";
		}

		String payloadTemplate = "[{\"roles\":\"%s\",\"domain\":\"%s\",\"userName\":\"%s\",\"email\":\"\",\"password\":\"%s\",\"status\":\"NEW\",\"active\":true,\"suspended\":false,\"authorities\":[],\"deleted\":false,\"$$index\":2}]";
		int index = getUsers().length();
		String payload = String.format(payloadTemplate, role, domain, username, pass);

		ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), payload);
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not create user");
		}
	}


	public void deleteUser(String username, String domain) throws Exception {
		switchDomain(domain);

		String getResponse = requestGET(resource.path(RestServicePaths.USERS), null).getEntity(String.class);

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

		ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), toDelete.toString());
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not delete user");
		}
	}

	public void createPluginUser(String username, String role, String pass, String domain) {
		String payloadTemplate = "[{\"status\":\"NEW\",\"userName\":\"%s\",\"active\":true,\"suspended\":false,\"authenticationType\":\"BASIC\",\"$$index\":0,\"authRoles\":\"%s\",\"password\":\"%s\"}]";
		String payload = String.format(payloadTemplate, username, role, pass);

		switchDomain(domain);
		ClientResponse response = requestPUT(resource.path(RestServicePaths.PLUGIN_USERS), payload);
		if (response.getStatus() != 204) {
			throw new RuntimeException("Could not create plugin user");
		}
	}

	public void deletePluginUser(String username, String domain) throws Exception {

		switchDomain(domain);

		String getResponse = requestGET(resource.path(RestServicePaths.PLUGIN_USERS), null).getEntity(String.class);

		JSONArray pusers = new JSONObject(sanitizeResponse(getResponse)).getJSONArray("entries");
		JSONArray toDelete = new JSONArray();
		for (int i = 0; i < pusers.length(); i++) {
			if (pusers.getJSONObject(i).getString("userName").equalsIgnoreCase(username)) {
				JSONObject tmpUser = pusers.getJSONObject(i);
				tmpUser.put("status", "REMOVED");
				toDelete.put(tmpUser);
			}
		}

		ClientResponse response = requestPUT(resource.path(RestServicePaths.PLUGIN_USERS), toDelete.toString());
		if (response.getStatus() != 204) {
			throw new RuntimeException("Could not delete plugin user");
		}
	}

	public void updateUser(String username, HashMap<String, String> toUpdate) {
		HashMap<String, String> adminUser = dataProvider.getAdminUser();
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

			ClientResponse response = requestPUT(resource.path(RestServicePaths.USERS), "[" + user.toString() + "]");
			if (response.getStatus() != 200) {
				throw new RuntimeException("Could not UPDATE user");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public List<String> getDomainNames() {
		ClientResponse response = requestGET(resource.path(RestServicePaths.DOMAINS), null);
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


	public void createMessageFilter(String actionName, String domain) {
		String payloadTemplate = "{\"entityId\":0,\"index\":0,\"backendName\":\"backendWebservice\",\"routingCriterias\":[{\"entityId\":0,\"name\":\"action\",\"expression\":\"%s\"}],\"persisted\":false,\"from\":null,\"to\":null,\"action\":{\"entityId\":0,\"name\":\"action\",\"expression\":\"%s\"},\"service\":null,\"$$index\":2}";
		String payload = String.format(payloadTemplate, actionName, actionName);


		switchDomain(domain);


		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
		JSONArray currentMSGF = null;
		try {
			currentMSGF = new JSONObject(sanitizeResponse(currentMSGFRaw)).getJSONArray("messageFilterEntries");
			currentMSGF.put(new JSONObject(payload));
		} catch (JSONException e) {
			e.printStackTrace();
		}


		ClientResponse response = requestPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), currentMSGF.toString());
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not get message filter");
		}
	}

	public void deleteMessageFilter(String actionName, String domain) {


		switchDomain(domain);

		String currentMSGFRaw = requestGET(resource.path(RestServicePaths.MESSAGE_FILTERS), null).getEntity(String.class);
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


		ClientResponse response = requestPUT(resource.path(RestServicePaths.MESSAGE_FILTERS), deletedL.toString());
		if (response.getStatus() != 200) {
			throw new RuntimeException("Could not message filter");
		}
	}


	public void uploadPMode(String pmodeFilePath, String domain) {
		switchDomain(domain);

		HashMap<String, String> fields = new HashMap<>();
		fields.put("description", "automatic red");
		requestPOSTFile(resource.path(RestServicePaths.PMODE), pmodeFilePath, fields);

	}

	public boolean isPmodeUploaded(String domain) throws Exception {

		switchDomain(domain);

		String getResponse = requestGET(resource.path(RestServicePaths.PMODE_LIST), null).getEntity(String.class);
		JSONArray entries = new JSONArray(sanitizeResponse(getResponse));

		return entries.length() > 0;
	}


}
