package domibus.ui;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DOMIBUS_PAGES;
import ddsl.enums.DRoles;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.messages.MessageDetailsModal;
import pages.messages.MessagesPage;
import utils.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessagesLogPgTest extends BaseTest {

	private HashMap<String, String> createdPluginUsers = new HashMap<>();

	@Test(description = "MSG-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesPage() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		soft.assertTrue(page.isLoaded(), "Page elements are loaded");
		soft.assertTrue(page.getFilters().basicFiltersLoaded(), "Basic filters are present");
		soft.assertTrue(!page.getFilters().advancedFiltersLoaded(), "Advanced filters are NOT present");
		soft.assertTrue(!page.getDownloadButton().isEnabled(), "Download button is not enabled");
		soft.assertTrue(!page.getResendButton().isEnabled(), "Resend button is not enabled");

		soft.assertAll();
	}

	@Test(description = "MSG-2", groups = {"multiTenancy", "singleTenancy"})
	public void messageRowSelect() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmode-blue.xml", null);
		String messID = messageSender.sendMessage(user, data.getDefaultTestPass()).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

//		page.wait.forXMillis(5000);

		page.refreshPage();
		page.grid().scrollToAndSelect("Message Id", messID);

		soft.assertTrue(page.getDownloadButton().isEnabled(), "After a row is selected the Download button");

		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	@Test(description = "MSG-3", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickMessageRow() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmode-blue.xml", null);
		String messID = messageSender.sendMessage(user, data.getDefaultTestPass()).getMessageID().get(0);

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		DGrid grid = page.grid();
		int index = grid.scrollTo("Message Id", messID);

		HashMap<String, String> info = grid.getRowInfo(index);
		grid.doubleClickRow(index);

		MessageDetailsModal modal = new MessageDetailsModal(driver);

		for (String s : info.keySet()) {
			if(s.contains("Action")){ continue;}
			soft.assertEquals(modal.getValue(s), info.get(s), "Checking info in grid vs modal " + s );
		}
		rest.deletePluginUser(user, null);
		soft.assertAll();
	}

	@Test(description = "MSG-4", groups = {"multiTenancy", "singleTenancy"})
	public void filterMessages() throws Exception{
		SoftAssert soft = new SoftAssert();

		String user = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(),null);
		rest.uploadPMode("pmode-blue.xml", null);
		List<String> messageIDs = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			messageIDs.add( messageSender.sendMessage(user, data.getDefaultTestPass()).getMessageID().get(0));
		}

		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGES);
		MessagesPage page = new MessagesPage(driver);

		DGrid grid = page.grid();

		List<HashMap<String, String>> allRowInfo = grid.getAllRowInfo();

		page.getFilters().getMessageIDInput().fill(messageIDs.get(0));
		page.getFilters().getSearchButton().click();

		List<HashMap<String, String>> filteredRowInfo = grid.getAllRowInfo();

		List<HashMap<String, String>> expectedResult = allRowInfo.stream().filter(rowInfo -> rowInfo.get("Message Id").equals(messageIDs.get(0))).collect(Collectors.toList());

		soft.assertTrue(filteredRowInfo.size() == expectedResult.size(), "No of listed items in page matches expected");


		rest.deletePluginUser(user, null);
		soft.assertAll();
	}



}
