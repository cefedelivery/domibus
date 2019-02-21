package domibus.ui;


import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.login.LoginPage;
import rest.DomibusRestClient;
import utils.Generator;

import java.util.HashMap;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class LoginPgTest extends BaseTest {

	/**Checks whether login as system admin works*/
	@Test(description = "LGN-1", groups = {"multiTenancy"})
	public void loginSuccessfulSuperAdminTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		LoginPage loginPage = new LoginPage(driver);
		soft.assertTrue(loginPage.isLoaded());

		loginPage.login(data.getAdminUser());

		DomibusPage page = new DomibusPage(driver);
		page.clickVoidSpace();

		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");

		page.getSandwichMenu().logout();
		soft.assertAll();
	}


	/**Checks whether login as admin works*/
	@Test(description = "LGN-2", groups = {"multiTenancy", "singleTenancy"})
	public void loginSuccessfulAdminTest() throws Exception {
		SoftAssert soft = new SoftAssert();

		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.ADMIN, data.getDefaultTestPass(), null);

		LoginPage loginPage = new LoginPage(driver);

		soft.assertTrue(loginPage.isLoaded());
		loginPage.login(username, data.getDefaultTestPass());

		DomibusPage page = new DomibusPage(driver);
		page.clickVoidSpace();

		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");

		rest.deleteUser(username, null);
		soft.assertAll();
	}

	/**Checks whether login as simple user works*/
	@Test(description = "LGN-3", groups = {"multiTenancy", "singleTenancy"})
	public void loginSuccessfulUserTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = Generator.randomAlphaNumeric(10);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), null);

		LoginPage loginPage = new LoginPage(driver);

		soft.assertTrue(loginPage.isLoaded());
		loginPage.login(username, data.getDefaultTestPass());

		DomibusPage page = new DomibusPage(driver);
		page.clickVoidSpace();

		soft.assertTrue(page.getSandwichMenu().isLoggedIn(), "User logged in");

		rest.deleteUser(username, null);
		soft.assertAll();
	}

	/**Checks whether login doesn't work with a invalid user and proper error message appears*/
	@Test(description = "LGN-4", groups = {"multiTenancy", "singleTenancy"})
	public void loginWithErrorTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		LoginPage loginPage = new LoginPage(driver);

		soft.assertTrue(loginPage.isLoaded());

		loginPage.login("invalidTest", "invalidTest");

		soft.assertFalse(loginPage.getSandwichMenu().isLoggedIn(), "User not logged in");
		soft.assertTrue(loginPage.isLoaded(), "User is still on Login page");

		soft.assertTrue(loginPage.getAlertArea().isError(), "Error message is displayed");
		soft.assertEquals(loginPage.getAlertArea().getAlertMessage(), DMessages.MSG_1, "Displayed message is correct");

		soft.assertAll();
	}

	/**Checks whether repeated unsuccessful attempts to login block the account*/
	@Test(description = "LGN-5", groups = {"multiTenancy", "singleTenancy"})
	public void blockUserAccountTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		String username = "testBlockAcc_" + Generator.randomAlphaNumeric(3);
		rest.createUser(username, DRoles.USER, data.getDefaultTestPass(), "Default");

		LoginPage page = new LoginPage(driver);

		for (int i = 0; i < 5; i++) {
			page.login(username, "password So Wrong");

			soft.assertFalse(page.getSandwichMenu().isLoggedIn(), "User not logged in");
			soft.assertTrue(page.isLoaded(), "User is still on Login page");

			soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");

			if (i <= 4) {
				soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MSG_1, "Displayed message is correct");
			} else {
				soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MSG_2, "Account blocked message displayed as expected");
			}
		}

		page.login(username, data.getDefaultTestPass());
		soft.assertTrue(page.isLoaded(), "User is still on Login page");
		soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed (2)");
		soft.assertEquals(page.getAlertArea().getAlertMessage(), DMessages.MSG_2_1, "Displayed message is correct (2)");

		HashMap<String, String> toUpdate = new HashMap<>();
		toUpdate.put("active", "true");
		new DomibusRestClient().updateUser(username, toUpdate);

		page.wait.forXMillis(300);
		page.login(username, data.getDefaultTestPass());
		soft.assertTrue(new DomibusPage(driver).getSandwichMenu().isLoggedIn(), "User is on Messages page, account is unblocked");

		soft.assertAll();
	}


}





