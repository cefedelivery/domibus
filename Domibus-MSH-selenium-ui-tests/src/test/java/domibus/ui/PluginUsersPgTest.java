package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DOMIBUS_PAGES;
import ddsl.enums.DRoles;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import pages.plugin_users.PluginUserModal;
import pages.plugin_users.PluginUsersPage;
import utils.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PluginUsersPgTest extends BaseTest {

	protected PluginUsersPage login(HashMap<String, String> user) throws Exception {
		LoginPage loginPage = new LoginPage(driver);
		loginPage.login(user);
		new DomibusPage(driver).getSidebar().getPageLnk(DOMIBUS_PAGES.PLUGIN_USERS).click();
		return new PluginUsersPage(driver);
	}

	@Test(description = "PU-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception{
		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page

		PluginUsersPage page = login(data.getAdminUser());
//		checks that all components of the page are present
		soft.assertTrue(page.isLoaded(), "page loaded");

		soft.assertAll();
	}

	@Test(description = "PU-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleclickRow() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		DGrid grid = page.grid();
		int index = grid.scrollTo("User Name", username);
		HashMap<String, String> row = grid.getRowInfo(index);

		grid.scrollToAndDoubleClick("User Name", username);


		PluginUserModal pum = new PluginUserModal(driver);

		soft.assertEquals(row.get("User Name"), pum.getUserNameInput().getText(), "Correct username is displayed");
		soft.assertEquals(row.get("Role"), pum.getRolesSelect().getSelectedValue(), "Correct role is displayed");
		soft.assertEquals(row.get("Original User"), pum.getOriginalUserInput().getText(), "Correct orig user is displayed");

		rest.deletePluginUser(username, null);
		soft.assertAll();

	}

	@Test(description = "PU-3", groups = {"multiTenancy", "singleTenancy"})
	public void newUserCancel() throws Exception{

		String username = Generator.randomAlphaNumeric(9);

		SoftAssert soft = new SoftAssert();
// 		login and go to users page
		PluginUsersPage page = login(data.getAdminUser());

		soft.assertTrue(page.isLoaded(), "Page is loaded!!");

		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

//		create new user
		page.newUser(username, DRoles.ADMIN, data.getDefaultTestPass(), data.getDefaultTestPass());

		page.grid().waitForRowsToLoad();

		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");

		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		int index = page.grid().scrollTo("User Name", username);
		soft.assertEquals(index, -1, "User not present in the list of users");

		soft.assertAll();
	}

	@Test(description = "PU-4", groups = {"multiTenancy", "singleTenancy"})
	public void newUserSave() throws Exception{

		String username = Generator.randomAlphaNumeric(9);

		SoftAssert soft = new SoftAssert();
// 		login and go to users page
		PluginUsersPage page = login(data.getAdminUser());

		soft.assertTrue(page.isLoaded(), "Page is loaded!!");

		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled on page load");

//		create new user
		page.newUser(username, DRoles.ADMIN, data.getDefaultTestPass(), data.getDefaultTestPass());

		page.grid().waitForRowsToLoad();
		soft.assertTrue(page.getCancelBtn().isEnabled(), "Cancel button is enabled after new user creation");
		soft.assertTrue(page.getSaveBtn().isEnabled(), "Save button is enabled after new user creation");

		page.getSaveBtn().click();
//		new Dialog(driver).confirm();

		DGrid grid = page.grid();
		grid.waitForRowsToLoad();

		int index = page.grid().scrollTo("User Name", username);
		soft.assertTrue(index > -1, "Created user present in grid");

		soft.assertTrue(!page.getCancelBtn().isEnabled(), "Cancel button is disabled after new user persisted");
		soft.assertTrue(!page.getSaveBtn().isEnabled(), "Save button is disabled after new user persisted");

		rest.deletePluginUser(username, null);

		soft.assertAll();
	}

	@Test(description = "PU-5", groups = {"multiTenancy", "singleTenancy"})
	public void editAndCancel() throws Exception{
		String toAdd = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		DGrid grid = page.grid();
		int index = grid.scrollTo("User Name", username);

		grid.scrollToAndDoubleClick("User Name", username);

		PluginUserModal pum = new PluginUserModal(driver);

		pum.getOriginalUserInput().fill("testEdit");
		soft.assertTrue(!pum.getOkBtn().isEnabled(), "Invalid value cannot be saved in the Original User field");

		pum.getOriginalUserInput().fill(toAdd);
		pum.clickOK();

		soft.assertTrue(grid.scrollTo("Original User", toAdd)>-1, "Edited value is visible in the grid");

		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		soft.assertTrue(grid.scrollTo("Original User", toAdd) == -1, "Edited value is NOT visible in the grid after Cancel");

		rest.deletePluginUser(username, null);
		soft.assertAll();

	}

	@Test(description = "PU-6", groups = {"multiTenancy", "singleTenancy"})
	public void editAndSave() throws Exception{
		String toAdd = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		DGrid grid = page.grid();
		int index = grid.scrollTo("User Name", username);
		HashMap<String, String> row = grid.getRowInfo(index);

		grid.scrollToAndDoubleClick("User Name", username);

		PluginUserModal pum = new PluginUserModal(driver);

		pum.getOriginalUserInput().fill("testEdit");
		soft.assertTrue(!pum.getOkBtn().isEnabled(), "Invalid value cannot be saved in the Original User field");

		pum.getOriginalUserInput().fill(toAdd);
		pum.clickOK();

		soft.assertTrue(grid.scrollTo("Original User", toAdd)>-1, "Edited value is visible in the grid");

		page.getSaveBtn().click();
//		there should be confirmation as in all the other pages :(
//		new Dialog(driver).confirm();

		soft.assertTrue(grid.scrollTo("Original User", toAdd) > -1, "Edited value is visible in the grid after Save");

		rest.deletePluginUser(username, null);
		soft.assertAll();

	}

	@Test(description = "PU-7", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndCancel() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		DGrid grid = page.grid();
		grid.scrollToAndSelect("User Name", username);
		page.getDeleteBtn().click();

		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is not present in the grid after delete");


		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		soft.assertTrue(grid.scrollTo("User Name", username) > -1, "Plugin user is present in the grid after Cancel");

		rest.deletePluginUser(username, null);
		soft.assertAll();
	}

	@Test(description = "PU-8", groups = {"multiTenancy", "singleTenancy"})
	public void deleteAndSave() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		DGrid grid = page.grid();
		grid.scrollToAndSelect("User Name", username);
		page.getDeleteBtn().click();

		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is not present in the grid after delete");


		page.getSaveBtn().click();

		soft.assertTrue(grid.scrollTo("User Name", username) == -1, "Plugin user is NOT present in the grid after Save");

		soft.assertAll();
	}

	@Test(description = "PU-9", groups = {"multiTenancy", "singleTenancy"})
	public void editUsername() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		page.grid().scrollToAndDoubleClick("User Name", username);

		PluginUserModal pum = new PluginUserModal(driver);

		soft.assertTrue(!pum.getUserNameInput().isEnabled(), "Username is disabled!");

		rest.deletePluginUser(username, null);
		soft.assertAll();

	}

	@Test(description = "PU-10", groups = {"multiTenancy", "singleTenancy"})
	public void editPassErrMess() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		page.grid().scrollToAndDoubleClick("User Name", username);

		PluginUserModal pum = new PluginUserModal(driver);

		soft.assertTrue(!pum.getUserNameInput().isEnabled(), "Username is disabled!");
		pum.getPasswordInput().fill("tst");

		String errMess = pum.getPassErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_POLICY_MESSAGE, "Password policy clearly displayed when wrong pass is entered");

		pum.getPasswordInput().fill(data.getDefaultTestPass());
		pum.getConfirmationInput().fill("lksjdlkfdskj");
		errMess = pum.getConfirmationErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_NO_MATCH_MESSAGE, "Password and confirmation should match");



		rest.deletePluginUser(username, null);
		soft.assertAll();

	}

	@Test(description = "PU-11", groups = {"multiTenancy", "singleTenancy"})
	public void createPluginUserFieldValidations() throws Exception{
		String username = Generator.randomAlphaNumeric(10);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		page.getNewBtn().click();

		PluginUserModal pum = new PluginUserModal(driver);

//		BUG HAS BEEN REPORTED

//		pum.getUserNameInput().fill("#@$%^");
//		String errMess = pum.getUsernameErrMess().getText();
//		soft.assertEquals(errMess, DMessages.USERNAME_VALIDATION_MESSAGE, "Username not valid 1 (invalid characters)");
//
//		pum.getUserNameInput().fill("a");
//		errMess = pum.getUsernameErrMess().getText();
//		soft.assertEquals(errMess, DMessages.USERNAME_VALIDATION_MESSAGE, "Username not valid 2 (too short)");

		pum.getUserNameInput().clear();
		String errMess = pum.getUsernameErrMess().getText();
		soft.assertEquals(errMess, DMessages.USERNAME_NO_EMPTY_MESSAGE, "Username should not be empty");

		pum.getUserNameInput().fill(username);

		pum.getOriginalUserInput().fill("kdsjflksjfsldjk");
		soft.assertEquals(pum.getOriginalUserErrMess().getText(), DMessages.ORIGINAL_USER_NOTVALID, "Original user is not valid");
		pum.getOriginalUserInput().clear();

		soft.assertEquals(pum.getRoleErrMess().getText(), DMessages.ROLE_NOTEMPTY, "Role cannot be empty");
		pum.getRolesSelect().selectOptionByIndex(0);


		errMess = pum.getPassErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_NO_EMPTY_MESSAGE, "Password should NOT empty");

		errMess = pum.getConfirmationErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_NO_EMPTY_MESSAGE, "Password should NOT empty");

		pum.getPasswordInput().fill("tst");

		errMess = pum.getPassErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_POLICY_MESSAGE, "Password policy clearly displayed when wrong pass is entered");

		pum.getPasswordInput().fill(data.getDefaultTestPass());
		pum.getConfirmationInput().fill("lksjdlkfdskj");
		errMess = pum.getConfirmationErrMess().getText();
		soft.assertEquals(errMess, DMessages.PASS_NO_MATCH_MESSAGE, "Password and confirmation should match");


		soft.assertAll();
	}

	@Test(description = "PU-12", groups = {"multiTenancy", "singleTenancy"})
	public void editRole() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		page.grid().scrollToAndDoubleClick("User Name", username);

		PluginUserModal pum = new PluginUserModal(driver);
		pum.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		pum.getOkBtn().click();

		soft.assertEquals(page.grid().getRowInfo("User Name", username).get("Role"),
				DRoles.ADMIN, "Grid shows new role for plugin user");

		page.getCancelBtn().click();
		new Dialog(driver).confirm();

		soft.assertEquals(page.grid().getRowInfo("User Name", username).get("Role"),
				DRoles.USER, "Grid shows old role for plugin user after cancel");

//		-------------------
		page.grid().scrollToAndDoubleClick("User Name", username);

		pum = new PluginUserModal(driver);
		pum.getRolesSelect().selectOptionByText(DRoles.ADMIN);
		pum.getOkBtn().click();

		soft.assertEquals(page.grid().getRowInfo("User Name", username).get("Role"),
				DRoles.ADMIN, "Grid shows new role for plugin user");

		page.getSaveBtn().click();

		soft.assertEquals(page.grid().getRowInfo("User Name", username).get("Role"),
				DRoles.ADMIN, "Grid shows new role for plugin user after save");


		rest.deletePluginUser(username, null);
		soft.assertAll();

	}

	@Test(description = "PU-13", groups = {"multiTenancy"})
	public void domainVisibility() throws Exception{
		String username = Generator.randomAlphaNumeric(10);

		List<String> domains = rest.getDomainNames();
		String domain1 = "";
		for (String domain : domains) {
			if(!domain.equalsIgnoreCase("Default")){
				domain1 = domain;
				break;
			}
		}
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), domain1);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		soft.assertTrue(page.grid().scrollTo("User Name", username) == -1, "Plugin user is not visible on default domain");

		page.getDomainSelector().selectOptionByText(domain1);

		soft.assertTrue(page.grid().scrollTo("User Name", username) > -1, "Plugin user is visible on domain1");


		rest.deletePluginUser(username, domain1);
		soft.assertAll();
	}

	@Test(description = "PU-14", groups = {"multiTenancy", "singleTenancy"})
	public void duplicatePluginUsersSameDomain() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());
		page.newUser(username, DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
		page.getSaveBtn().click();

		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.DUPLICATE_PLUGINUSER_, username ),
				"Error message is shown");

		rest.deletePluginUser(username, null);
		soft.assertAll();
	}

	@Test(description = "PU-15", groups = {"multiTenancy"})
	public void duplicatePluginUsersDifferentDomain() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		List<String> domains = rest.getDomainNames();
		String domain1 = "";
		for (String domain : domains) {
			if(!domain.equalsIgnoreCase("Default")){
				domain1 = domain;
				break;
			}
		}
		rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), domain1);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());
		page.newUser(username, DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
		page.getSaveBtn().click();

		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.DUPLICATE_PLUGINUSER_, username ),
				"Error message is shown");

		rest.deletePluginUser(username, domain1);
		soft.assertAll();
	}

	@Test(description = "PU-16", groups = {"multiTenancy"})
	public void sameUsernameAsUserOnDifferentDomain() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		List<String> domains = rest.getDomainNames();
		String domain1 = "";
		for (String domain : domains) {
			if(!domain.equalsIgnoreCase("Default")){
				domain1 = domain;
				break;
			}
		}
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), domain1);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());
		page.newUser(username, DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
		page.getSaveBtn().click();

		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.DUPLICATE_PLUGINUSER_, username ),
				"Error message is shown");

		rest.deleteUser(username, domain1);
		soft.assertAll();
	}

	@Test(description = "PU-17", groups = {"multiTenancy", "singleTenancy"})
	public void sameUsernameAsUserOnSameDomain() throws Exception{
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());
		page.newUser(username, DRoles.USER, data.getDefaultTestPass(), data.getDefaultTestPass());
		page.getSaveBtn().click();

		soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");
		soft.assertEquals(page.getAlertArea().getAlertMessage(),
				String.format(DMessages.DUPLICATE_PLUGINUSER_, username ),
				"Error message is shown");

		rest.deleteUser(username, null);
		soft.assertAll();
	}

	@Test(description = "PU-18", groups = {"multiTenancy", "singleTenancy"})
	public void filterPluginUserList() throws Exception{
		List<String> usernames = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			String username = Generator.randomAlphaNumeric(10);
			rest.createPluginUser(username, DRoles.USER, data.getDefaultTestPass(), null);
			usernames.add(username);
		}

		SoftAssert soft = new SoftAssert();
//		login with Admin and go to plugin users page
		PluginUsersPage page = login(data.getAdminUser());

		page.getFilters().search(null, null, null, usernames.get(0));
		soft.assertEquals(page.grid().getRowInfo(0).get("User Name"),usernames.get(0), "Search by username return corect result");
		soft.assertEquals(page.grid().getRowsNo(), 1, "Search by username returnonly one result");

		page.getFilters().getUsernameInput().clear();
		page.getFilters().search(null, DRoles.USER, null, null);
		DGrid grid = page.grid();
		for (int i = 0; i < grid.getRowsNo(); i++) {
			soft.assertEquals(grid.getRowInfo(i).get("Role"), DRoles.USER, "Result has correct role");
		}

		for (int i = 0; i < usernames.size(); i++) {
			rest.deletePluginUser(usernames.get(i), null);
		}
		soft.assertAll();
	}

}
