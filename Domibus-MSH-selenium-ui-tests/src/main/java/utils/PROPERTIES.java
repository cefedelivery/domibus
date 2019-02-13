package utils;

public class PROPERTIES {

	public static final String UI_BASE_URL = System.getProperty("UI_BASE_URL");
	public static final int TIMEOUT = Integer.valueOf(System.getProperty("SHORT_TIMEOUT"));
	public static final int LONG_WAIT = Integer.valueOf(System.getProperty("LONG_TIMEOUT"));

	public static final String REST_BASE_URL = System.getProperty("REST_BASE_URL");
	public static final String DB_URL = System.getProperty("DB_URL");

	public static final String DATA_FILE = System.getProperty("test_data.file");
	public static final String REPORTS_FOLDER = System.getProperty("reports.folder");
	public static final boolean IS_MULTI_DOMAIN = Boolean.valueOf(System.getProperty("isMultiDomain"));


	public static final String adminUser = "";
	public static final String adminPass = "";

}
