package utils;

import org.json.JSONException;
import org.json.JSONObject;
import utils.enums.DRoles;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

public class TestDataProvider {

	private String TESTDATAFILE = System.getProperty("data.folder") + PROPERTIES.DATA_FILE;
	private JSONObject testData = null;


	public TestDataProvider() {
		loadTestData();
	}

	private void loadTestData(){
		if(testData != null){
			return;
		}
		String content = null;
		try {
			content = new String(Files.readAllBytes(Paths.get(TESTDATAFILE)));
			testData = new JSONObject(content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, String> getUser(String role){

		HashMap<String, String> toReturn = new HashMap<>();

		try {
			JSONObject user = null;
			user = testData.getJSONObject("loginUsers").getJSONObject(role);

			Iterator<String> keysItr = user.keys();
			while(keysItr.hasNext()) {
				String usrKey = keysItr.next();
				toReturn.put(usrKey, user.getString(usrKey));
			}
		} catch (JSONException e) {	}

		return toReturn;
	}

	public String getDefaultTestPass(){
		loadTestData();
		try {
			return testData.getString("passwordForTestUsers");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}


	public HashMap<String, String> getAdminUser(){
		if(PROPERTIES.IS_MULTI_DOMAIN){
			return getUser(DRoles.SUPER);
		}
		return getUser(DRoles.ADMIN);
	}




}
