package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DOMIBUS_PAGES;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import pages.msgFilter.MessageFilterModal;
import pages.msgFilter.MessageFilterPage;
import utils.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageFilterPgTest extends BaseTest {

	Logger logger = Logger.getLogger(this.getClass());

	@BeforeMethod(alwaysRun = true)
	private void login() throws Exception{
		new LoginPage(driver)
				.login(data.getAdminUser());
		new DomibusPage(driver).getSidebar().gGoToPage(DOMIBUS_PAGES.MESSAGE_FILTER);
	}

	@Test(description = "MSGF-1", groups = {"multiTenancy", "singleTenancy"})
	public void openMessagesFilterPage()throws Exception{
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);

		soft.assertTrue(page.isLoaded(), "All elements are loaded!!");
		soft.assertAll();

	}

	@Test(description = "MSGF-2", groups = {"multiTenancy", "singleTenancy"})
	public void newFilterSave() throws Exception{
		String actionName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		soft.assertTrue(page.isLoaded(), "All elements are loaded!!");

		page.getNewBtn().click();
		MessageFilterModal popup = new MessageFilterModal(driver);
		popup.getPluginSelect().selectOptionByIndex(0);
		popup.actionInput.sendKeys(actionName);
		popup.clickOK();


		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after new Message Filter was created");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is active after new Message Filter was created");

		page.saveAndConfirmChanges();

		soft.assertTrue(page.grid().scrollTo("Action", actionName) >-1, "New filter is present in the grid");

		soft.assertAll();
	}

	@Test(description = "MSGF-3", groups = {"multiTenancy", "singleTenancy"})
	public void cancelNewFilter() throws Exception{
		String actionName = Generator.randomAlphaNumeric(5);
		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		soft.assertTrue(page.isLoaded(), "All elements are loaded!!");

		page.getNewBtn().click();
		MessageFilterModal popup = new MessageFilterModal(driver);
		popup.getPluginSelect().selectOptionByIndex(0);
		popup.actionInput.sendKeys(actionName);
		popup.clickOK();


		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is active after new Message Filter was created");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is active after new Message Filter was created");

		page.cancelChangesAndConfirm();
		soft.assertTrue(page.grid().scrollTo("Action", actionName) ==-1, "New filter is NOT present in the grid");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled after changes are canceled");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled after changes are canceled");

		soft.assertAll();
	}
	@Test(description = "MSGF-5", groups = {"multiTenancy", "singleTenancy"})
	public void shuffleAndCancel() throws Exception{
		List<String> actionNames = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String actionName = Generator.randomAlphaNumeric(5);
			rest.createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();
		soft.assertTrue(page.isLoaded(), "All elements are loaded!!");

		page.grid().selectRow(0);
		soft.assertFalse(page.getMoveUpBtn().isEnabled(), "Button Move Up is not enabled if selected filter is already first!");

		page.grid().selectRow(1);
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Button Move Up is enabled for the second row");

		HashMap<String, String> row1 = page.grid().getRowInfo(1);
		HashMap<String, String> row0 = page.grid().getRowInfo(0);
		page.getMoveUpBtn().click();
		HashMap<String, String> newRow0 = page.grid().getRowInfo(0);
		soft.assertEquals(row1.get("Action"),newRow0.get("Action"), "The row that was previously on position 1 is now on first position!");

		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled");

		page.cancelChangesAndConfirm();
		HashMap<String, String> oldRow0 = page.grid().getRowInfo(0);

		soft.assertEquals(row0.get("Action"),oldRow0.get("Action"),
				"The row that was previously on position 0 is now on first position again after Cancel!");

		for (int i = 0; i < actionNames.size(); i++) {
			rest.deleteMessageFilter(actionNames.get(i), null);
		}
		soft.assertAll();
	}

	@Test(description = "MSGF-6", groups = {"multiTenancy", "singleTenancy"})
	public void shuffleAndSave() throws Exception{
		List<String> actionNames = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String actionName = Generator.randomAlphaNumeric(5);
			rest.createMessageFilter(actionName, null);
			actionNames.add(actionName);
		}

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();
		soft.assertTrue(page.isLoaded(), "All elements are loaded!!");

		page.grid().selectRow(0);
		soft.assertFalse(page.getMoveUpBtn().isEnabled(), "Button Move Up is not enabled if selected filter is already first!");

		page.grid().selectRow(1);
		soft.assertTrue(page.getMoveUpBtn().isEnabled(), "Button Move Up is enabled for the second row");

		HashMap<String, String> row1 = page.grid().getRowInfo(1);
		page.getMoveUpBtn().click();
		HashMap<String, String> newRow0 = page.grid().getRowInfo(0);
		soft.assertEquals(row1.get("Action"),newRow0.get("Action"), "The row that was previously on position 1 is now on first position!");

		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled");

		page.saveAndConfirmChanges();
		HashMap<String, String> oldRow0 = page.grid().getRowInfo(0);

		soft.assertEquals(oldRow0.get("Action"),row1.get("Action"),
				"The row that was previously on position 0 is now on first position again after Save!");

		for (int i = 0; i < actionNames.size(); i++) {
			rest.deleteMessageFilter(actionNames.get(i), null);
		}
		soft.assertAll();
	}


	@Test(description = "MSGF-7", groups = {"multiTenancy", "singleTenancy"})
	public void editAndCancel() throws Exception{
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		HashMap<String, String> row0 = page.grid().getRowInfo(0);
		page.grid().selectRow(0);

		page.getEditBtn().click();
		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill("newActionValue");
		modal.clickOK();

		page.cancelChangesAndConfirm();

		HashMap<String, String> newRow0 = page.grid().getRowInfo(0);

		soft.assertEquals(row0.get("Action"), newRow0.get("Action"), "Edited values are reset after canceling changes");

//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-8", groups = {"multiTenancy", "singleTenancy"})
	public void editAndSave() throws Exception{
		//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();
		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		page.grid().selectRow(index);
		page.getEditBtn().click();

		MessageFilterModal modal = new MessageFilterModal(driver);
		modal.getActionInput().fill("newActionValue");
		modal.clickOK();

		page.saveAndConfirmChanges();

		HashMap<String, String> row = page.grid().getRowInfo(index);
		soft.assertEquals(row.get("Action"), "newActionValue", "Edited values are saved");

//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-9", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndCancel() throws Exception{
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		if(index<0){throw new RuntimeException("Could not find created filter");}

		page.grid().selectRow(index);
		page.getDeleteBtn().click();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index==-1, "Filter not found in grid after delete");
		
		page.cancelChangesAndConfirm();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index>-1, "Filter found in grid after Cancel");

//		Delete created filter
		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();
	}

	@Test(description = "MSGF-10", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndSave() throws Exception{
//		Create a filter to edit
		String actionName = Generator.randomAlphaNumeric(5);
		rest.createMessageFilter(actionName, null);

		SoftAssert soft = new SoftAssert();

		MessageFilterPage page = new MessageFilterPage(driver);
		page.refreshPage();

		int index = page.grid().scrollTo("Action", actionName);
		if(index<0){throw new RuntimeException("Could not find created filter");}

		page.grid().selectRow(index);
		page.getDeleteBtn().click();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index==-1, "Filter not found in grid after delete");

		page.saveAndConfirmChanges();

		index = page.grid().scrollTo("Action", actionName);
		soft.assertTrue(index==-1, "Filter found in grid after Save");

//		Delete created filter
//		rest.deleteMessageFilter(actionName, null);

		soft.assertAll();

	}


//	@Test(description = "MSGF-11", groups = {"multiTenancy"})
//	public void filtersNotVisibleOnWrongDomains() throws Exception {
////		Create a filter to check on Default domain
//		String actionName = Generator.randomAlphaNumeric(5);
//		rest.createMessageFilter(actionName, null);
//
//
//		SoftAssert soft = new SoftAssert();
//
//		MessageFilterPage page = new MessageFilterPage(driver);
//		page.refreshPage();
//
//		int index = scrollToFilterByAction(actionName);
//		if(index<0){throw new RuntimeException("Could not find created filter");}
//
////		select whatever domain is on second position in the list
//		page.pageHeader.getDomainSelector().selectOptionByIndex(1);
//
//		index = scrollToFilterByAction(actionName);
//		soft.assertTrue(index<0, "Check if filter is still present in the grid");
//
////		select default domain
//		page.pageHeader.getDomainSelector().selectOptionByText("Default");
//
//		index = scrollToFilterByAction(actionName);
//		soft.assertTrue(!(index<0), "Check if filter is still present in the grid");
//
////		Delete the created filter
//		rest.deleteMessageFilter(actionName, null);
//
//		soft.assertAll();
//	}
//
//	private int scrollToFilterByAction(String action){
//		MessageFilterPage page = new MessageFilterPage(driver);
//
//		int index = -1;
//
//		ArrayList<MessageFilterRow> filterRows = page.getMessageFilterGrid().filterRows;
//		for (int i = 0; i < filterRows.size(); i++) {
//			MessageFilterRow filterRow = filterRows.get(i);
//			if(filterRow.getAction().equalsIgnoreCase(action)){index = i;}
//		}
//		return index;
//	}


}
