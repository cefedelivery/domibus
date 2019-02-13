package domibus.ui;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.DMessages;
import ddsl.enums.DOMIBUS_PAGES;
import ddsl.enums.DRoles;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import pages.users.UserModal;
import pages.users.UsersPage;
import utils.Generator;
import utils.PROPERTIES;

import java.util.HashMap;

public class UsersPgTest extends BaseTest {

	private void loginAndGoToUsersPage(HashMap<String, String> user) throws Exception{
//		login with Admin and go to users page
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(user);
		loginPage.getSidebar().gGoToPage(DOMIBUS_PAGES.USERS);
	}

	@Test(description = "USR-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());

		UsersPage page = new UsersPage(driver);
		soft.assertTrue(page.isLoaded(), "page loaded");

//		at least one user is listed (the current user)
		soft.assertTrue(page.grid().getRowsNo() > 0, "existing users are displayed");

		soft.assertAll();
	}

	@Test(description = "USR-1.1", groups = {"multiTenancy", "singleTenancy"})
	public void openDoubleClickModal() throws Exception {
		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		UsersPage page = new UsersPage(driver);
		page.refreshPage();

//		DoubleClick on the row doesn't work as expected due to the fact that not the whole area is clickable
//		This is circumvented for the time being by clicking the Edit button
		page.grid().scrollToAndDoubleClick("Username", username);
		page.getEditBtn().click();

		UserModal um = new UserModal(driver);
		soft.assertTrue(um.isLoaded(), "Doubleclick opens modal");

		soft.assertEquals(username, um.getUserNameInput().getText(), "Usernames match");
		soft.assertEquals(DRoles.USER, um.getRoleSelect().getSelectedValue(), "Roles match");

		if (PROPERTIES.IS_MULTI_DOMAIN) {
			soft.assertEquals( um.getDomainSelect().getSelectedValue(), "Default", "Domain matches selected domain in page header");
		}
		soft.assertAll();
	}

	@Test(description = "USR-2", groups = {"multiTenancy", "singleTenancy"})
	public void newUserCancel() throws Exception {

		String username = Generator.randomAlphaNumeric(9);

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		UsersPage page = new UsersPage(driver);

		soft.assertTrue(page.isLoaded(), "Page is loaded!!");

		soft.assertTrue(page.grid().getRowsNo() > 0, "Grid lists existing users!!!");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

//		create new user
		page.newUser(username, "tuser@bnc.com", DRoles.ADMIN, data.getDefaultTestPass(), data.getDefaultTestPass());

		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");

		page.cancelAndConfirm();


		int index = page.grid().scrollTo("Username", username);
		soft.assertEquals(index, -1, "User not present in the list of users");

		soft.assertAll();
	}


	@Test(description = "USR-3", groups = {"multiTenancy", "singleTenancy"})
	public void newUserSave() throws Exception {
		String username = Generator.randomAlphaNumeric(9);

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());

		UsersPage page = new UsersPage(driver);

		soft.assertTrue(page.isLoaded(), "Page is loaded!!");

		soft.assertTrue(page.grid().getRowsNo() > 0, "Grid lists existing users!!!");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

		page.newUser(username, "tuser@bnc.com", DRoles.ADMIN, data.getDefaultTestPass(), data.getDefaultTestPass());

		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");

		page.saveAndConfirm();



		int index = page.grid().scrollTo("Username", username);
		soft.assertTrue(index > -1, "Created user present in users grid");

		rest.deleteUser(username, null);
		soft.assertAll();
	}


	@Test(description = "USR-4", groups = {"multiTenancy", "singleTenancy"})
	public void newUserDelete() throws Exception {

		String username = Generator.randomAlphaNumeric(9);

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		UsersPage page = new UsersPage(driver);

		soft.assertTrue(page.isLoaded(), "Page is loaded!!");

		soft.assertTrue(page.grid().getRowsNo() > 0, "Grid lists existing users!!!");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");


		page.newUser(username, "tuser@bnc.com", DRoles.ADMIN,  data.getDefaultTestPass(), data.getDefaultTestPass());

		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");

		page.saveAndConfirm();

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		page.grid().scrollTo("Username", username);
		int index = page.grid().scrollTo("Username", username);
		soft.assertTrue(index > -1, "Created user present in users grid");
		if (index > -1) {
			page.grid().scrollToAndSelect("Username", username);
			page.getDeleteBtn().click();
			page.saveAndConfirm();
			page.grid().waitForRowsToLoad();
		}

		soft.assertAll();
	}

	@Test(description = "USR-5", groups = {"multiTenancy", "singleTenancy"})
	public void editUserAndCancel() throws Exception {

		String username = Generator.randomAlphaNumeric(9);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());

		UsersPage page = new UsersPage(driver);
		page.grid().scrollToAndSelect("Username", username);
		soft.assertTrue(page.getEditBtn().isEnabled(), "Edit button is enabled for selected user");

		page.getEditBtn().click();
		UserModal um = new UserModal(driver);

		soft.assertTrue(!um.getUserNameInput().isEnabled(), "Username cannot be edited");

		String newEmail = Generator.randomAlphaNumeric(5) + "@email.com";
		um.getEmailInput().fill(newEmail);
		um.clickOK();
		page.cancelAndConfirm();

		page.grid().scrollToAndSelect("Username", username);
		page.getEditBtn().click();
		um = new UserModal(driver);

		soft.assertTrue(um.getEmailInput().getText().isEmpty(), "Email has not changed after edit + click cancel");

		rest.deleteUser(username, null);
		soft.assertAll();
	}


	@Test(description = "USR-6", groups = {"multiTenancy", "singleTenancy"})
	public void editUserAndSave() throws Exception {

		String username = Generator.randomAlphaNumeric(9);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		UsersPage page = new UsersPage(driver);

		int index  = page.grid().scrollTo("Username", username);

		page.grid().scrollToAndSelect("Username", username);

		page.getEditBtn().click();
		UserModal um = new UserModal(driver);

		soft.assertTrue(!um.getUserNameInput().isEnabled(), "Username cannot be edited");
		soft.assertEquals(um.getUserNameInput().getText(), username, "Correct username is displayed in the um!");



		String email = um.getEmailInput().getText();

		String newEmail = Generator.randomAlphaNumeric(5) + "@email.com";
		um.getEmailInput().fill(newEmail);
		um.getRoleSelect().selectOptionByText(DRoles.ADMIN);

		um.clickOK();
		page.saveAndConfirm();

		index = page.grid().scrollTo("Username", username);

		page.grid().scrollToAndSelect("Username", username);
		page.getEditBtn().click();
		um = new UserModal(driver);

		soft.assertEquals(um.getEmailInput().getText(), newEmail, "Old email has changed after edit");
		soft.assertEquals(um.getRoleSelect().getSelectedValue(), DRoles.ADMIN, "Old role was changed after edit");
		soft.assertEquals(um.getUserNameInput().getText(), username, "Old username has not changed after edit + click cancel");

		soft.assertAll();
	}

	@Test(description = "USR-7", groups = {"multiTenancy", "singleTenancy"})
	public void deleteUserAndCancel() throws Exception {

		String username = Generator.randomAlphaNumeric(9);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());

		UsersPage page = new UsersPage(driver);

		int index  = page.grid().scrollTo("Username", username);
		page.grid().scrollToAndSelect("Username", username);

		page.getDeleteBtn().click();

		soft.assertTrue(page.getSaveBtn().isEnabled(), "After pressing delete the Save button is active");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "After pressing delete the Cancel button is active");

		page.cancelAndConfirm();

		index  = page.grid().scrollTo("Username", username);
		soft.assertTrue(!page.getUsersGrid().isDeleted(username), "User not marked as deleted");

		soft.assertAll();
	}

	@Test(description = "USR-8", groups = {"multiTenancy", "singleTenancy"})
	public void deleteUserAndSave() throws Exception {

		String username = Generator.randomAlphaNumeric(9);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());

		UsersPage page = new UsersPage(driver);

		int index  = page.grid().scrollTo("Username", username);

		page.grid().scrollToAndSelect("Username", username);

		page.getDeleteBtn().click();

		soft.assertTrue(page.getSaveBtn().isEnabled(), "After pressing delete the Save button is active");
		soft.assertTrue(page.getCancelBtn().isEnabled(), "After pressing delete the Cancel button is active");

		page.saveAndConfirm();

		soft.assertTrue(page.getUsersGrid().isDeleted(username), "User presented as deleted in the grid");

		rest.deleteUser(username, null);
		soft.assertAll();
	}

	@Test(description = "USR-9", groups = {"multiTenancy", "singleTenancy"})
	public void newUserPopupValidations() throws Exception {

		String username = Generator.randomAlphaNumeric(9);

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		UsersPage page = new UsersPage(driver);

		soft.assertTrue(page.isLoaded(), "Page is loaded!!");

		soft.assertTrue(page.grid().getRowsNo() > 0, "Grid lists existing users!!!");
		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

//		create new user
		page.getNewBtn().click();
		UserModal um = new UserModal(driver);
		um.fillData("", "asdsa", "", "assd", "asdaa");

		soft.assertEquals(um.getUsernameErrMess().getText(), DMessages.USERNAME_NO_EMPTY_MESSAGE, "Correct username err message appears");
		soft.assertEquals(um.getEmailErrMess().getText(), DMessages.EMAIL_INVALID_MESSAGE, "Correct email err message appears");
		soft.assertEquals(um.getPassErrMess().getText(), DMessages.PASS_POLICY_MESSAGE, "Correct pass err message appears");
		soft.assertEquals(um.getConfirmationErrMess().getText(), DMessages.PASS_NO_MATCH_MESSAGE, "Correct pass err message appears");

		soft.assertTrue(!um.isOKBtnEnabled(), "OK button is not enabled for invalid input");


		soft.assertAll();
	}

	@Test(description = "USR-10", groups = {"multiTenancy", "singleTenancy"})
	public void inactiveUserLogin() throws Exception {

		String username = Generator.randomAlphaNumeric(9);

		SoftAssert soft = new SoftAssert();
		loginAndGoToUsersPage(data.getAdminUser());
		UsersPage page = new UsersPage(driver);

//		create new user
		page.getNewBtn().click();
		UserModal um = new UserModal(driver);
		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
		um.getActiveChk().uncheck();

		um.clickOK();

		page.saveAndConfirm();
		page.wait.forXMillis(500);

		page.getSandwichMenu().logout();
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(username, data.getDefaultTestPass());

		soft.assertTrue(!loginPage.getSandwichMenu().isLoggedIn(), "User is not logged in");
		soft.assertTrue(loginPage.getAlertArea().isError(), "Error message shown!");
		soft.assertEquals(loginPage.getAlertArea().getAlertMessage(), DMessages.MSG_2_2, "Correct error message is shown");

		page.wait.forXMillis(500);

		loginPage.login(data.getAdminUser());
		loginPage.getSidebar().gGoToPage(DOMIBUS_PAGES.USERS);

		page.grid().scrollToAndSelect("Username", username);
		page.getEditBtn().click();

		um = new UserModal(driver);
		um.getActiveChk().check();
		um.clickOK();
		page.saveAndConfirm();
		page.wait.forXMillis(500);

		page.getSandwichMenu().logout();
		loginPage.login(username, data.getDefaultTestPass());

		soft.assertTrue(loginPage.getSandwichMenu().isLoggedIn(), "Active user is logged in");

		soft.assertAll();
	}
//
//	@Test(description = "USR-11", groups = {"multiTenancy", "singleTenancy"})
//	public void editUserRoleAndCheckPrivileges() throws Exception  {
//
//		String username = Generator.randomAlphaNumeric(9);
//		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), "default");
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//		int index  = page.grid().scrollTo("Username", username);
//		if(index>-1){
//			page.grid().scrollToAndSelect("Username", username);
//			UserModal um = page.getEditBtn().click();
//			um.getRoleSelect().selectOptionByText(DRoles.USER);
//
//			um.clickOK();
//			page.saveAndConfirm();
//			page.waitForXMillis(500);
//
//			page.pageHeader.sandwichMenu.logout();
//			LoginPage loginPage = new LoginPage(driver);
//			loginPage.login(username, data.getDefaultTestPass());
//
//			soft.assertEquals(loginPage.getSidebar().availableOptions().size(), 2, "User has only 2 options available in sidebar");
//
//		}
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-12", groups = {"multiTenancy", "singleTenancy"})
//	public void duplicateUsername() throws Exception  {
//
//		String username = Generator.randomAlphaNumeric(9);
//		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), "default");
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//
//		int index  = page.grid().scrollTo("Username", username);
//		if(index>-1){
//
//			UserModal um = page.getNewBtn().click();
//			um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//
//			um.clickOK();
//			page.clickSave();
//			page.waitForXMillis(500);
//
//			soft.assertEquals(page.alertArea.getAlertMessage().isError(), true, "Error message displayed");
//			soft.assertEquals(page.alertArea.getAlertMessage().getMessage(), "Duplicate user name for user: "+username+".", "Correct message displayed");
//
//		}
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-13", groups = {"multiTenancy"})
//	public void duplicateUsernameOnAnotherDomain() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		List<String> domains = rest.getDomainNames();
//		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), domains.get(1));
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//		page.pageHeader.getDomainSelector().selectOptionByText(domains.get(0));
//
//		UserModal um = page.getNewBtn().click();
//		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//
//		um.clickOK();
//		page.clickSave();
//		page.waitForXMillis(500);
//
//		soft.assertEquals(page.alertArea.getAlertMessage().isError(), true, "Error message displayed");
//		soft.assertEquals(page.alertArea.getAlertMessage().getMessage(), "Duplicate user name for user: "+username+".", "Correct message displayed");
//
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-14", groups = {"multiTenancy", "singleTenancy"})
//	public void duplicateUserVSPluginUser() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		rest.createPluginUser(username, DRoles.ADMIN, data.getDefaultTestPass(),null);
//
//		SoftAssert soft = new SoftAssert();
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//		UserModal um = page.getNewBtn().click();
//		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//
//		um.clickOK();
//		page.saveAndConfirm();
//		page.waitForXMillis(500);
//
//		soft.assertEquals(page.alertArea.getAlertMessage().isError(), true, "Error message displayed");
//		soft.assertEquals(page.alertArea.getAlertMessage().getMessage(), "Duplicate user name for user: "+username+".", "Correct message displayed");
//
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-15", groups = {"multiTenancy"})
//	public void duplicateUserVSPluginUserOtherDomain() throws Exception {
//
//		String username = Generator.randomAlphaNumeric(9);
//		List<String> domains = rest.getDomainNames();
//		rest.createPluginUser(username, DRoles.ADMIN, data.getDefaultTestPass(),domains.get(1));
//
//		SoftAssert soft = new SoftAssert();
//
//		loginAndGoToUsersPage(data.getAdminUser());
//		UsersPage page = new UsersPage(driver);
//
//		page.pageHeader.getDomainSelector().selectOptionByText(domains.get(0));
//		page.waitForXMillis(500);
//
//		UserModal um = page.getNewBtn().click();
//		um.fillData(username, "", DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
//
//		um.clickOK();
//		page.saveAndConfirm();
//		page.waitForXMillis(500);
//
//		soft.assertEquals(page.alertArea.getAlertMessage().isError(), true, "Error message displayed");
//		soft.assertEquals(page.alertArea.getAlertMessage().getMessage(), "Duplicate user name for user: "+username+".", "Correct message displayed");
//
//
//		soft.assertAll();
//	}
//
//	@Test(description = "USR-16", groups = {"multiTenancy", "singleTenancy"})
//	public void downloadUserList() throws Exception {
//		throw new SkipException("Im plementation of test not finished");
////		List<String> domains = new ArrayList<>();
////		if(PROPERTIES.IS_MULTI_DOMAIN){
////			domains = rest.getDomainNames();
////		}
////
////		SoftAssert soft = new SoftAssert();
////
////		LoginPage loginPage = new LoginPage(driver);
////		loginPage.login(data.getAdminUser());
////		loginPage.sidebar.goToPage(UsersPage.class);
////
////		int i=0;
////		do {
////			UsersPage page = new UsersPage(driver);
////			page.waitForXMillis(500);
////			if(PROPERTIES.IS_MULTI_DOMAIN){
////				page.pageHeader.getDomainSelector().selectOptionByText(domains.get(i));
////			}
////
//////			TODO: fill in the gap ... get the file and compare with all elements in list
////
////
////			i++;
////		}while (i<domains.size());
////
////		soft.assertAll();
//	}


}
