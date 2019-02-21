package utils;

import ddsl.enums.DRoles;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class TestDataProvider {

	private String TEST_DATA_FILE = System.getProperty("data.folder") + PROPERTIES.DATA_FILE;
	private JSONObject testData = null;


	public TestDataProvider() {
		loadTestData();
	}

	private void loadTestData() {
		if (null == testData) {
			try {
				testData = new JSONObject(new String(Files.readAllBytes(Paths.get(TEST_DATA_FILE))));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public HashMap<String, String> getUser(String role) {

		HashMap<String, String> toReturn = new HashMap<>();

		try {
			JSONObject user = null;
			user = testData.getJSONObject("loginUsers").getJSONObject(role);

			Iterator<String> keysItr = user.keys();
			while (keysItr.hasNext()) {
				String usrKey = keysItr.next();
				toReturn.put(usrKey, user.getString(usrKey));
			}
		} catch (JSONException e) {
		}

		return toReturn;
	}

	public String getDefaultTestPass() {
		loadTestData();
		try {
			return testData.getString("passwordForTestUsers");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}


	public HashMap<String, String> getAdminUser() {
		if (PROPERTIES.IS_MULTI_DOMAIN) {
			return getUser(DRoles.SUPER);
		}
		return getUser(DRoles.ADMIN);
	}


}
