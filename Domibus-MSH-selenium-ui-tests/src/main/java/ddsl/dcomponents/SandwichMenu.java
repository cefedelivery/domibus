package ddsl.dcomponents;

import ddsl.dobjects.DButton;
import ddsl.dobjects.DLink;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class SandwichMenu extends DComponent {

	private final int miniWaitTime = 200;

	public SandwichMenu(WebDriver driver) {
		super(driver);
		log.info("sandwich menu init");
	}

	By expandButton = By.id("settingsmenu_id");

	By menuContainer = By.cssSelector("div > div.mat-menu-content.ng-trigger.ng-trigger-fadeInItems");

	By currentUserID = By.cssSelector("button[role=\"menuitem\"]:nth-of-type(1) span");

	By logoutLnk = By.id("logout_id");


	public DButton getExpandButton() {
		wait.forXMillis(miniWaitTime);
		return new DButton(driver, driver.findElement(expandButton));
	}

	public DObject getCurrentUserID() {
		wait.forXMillis(miniWaitTime);
		return new DObject(driver, driver.findElement(currentUserID));
	}

	public DLink getLogoutLnk() {
		wait.forXMillis(miniWaitTime);
		return new DLink(driver, driver.findElement(logoutLnk));
	}

	private boolean isMenuExpanded() throws Exception {
		wait.forXMillis(miniWaitTime);
		try {
			wait.webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(menuContainer));
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	private void expandMenu() throws Exception {
		wait.forXMillis(miniWaitTime);
		if (isMenuExpanded()) return;
		getExpandButton().click();
		try {
			wait.webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(menuContainer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void contractMenu() throws Exception {
		wait.forXMillis(miniWaitTime);
		if (!isMenuExpanded()) return;
		clickVoidSpace();
	}

	public boolean isLoggedIn() throws Exception {

		wait.forXMillis(miniWaitTime);
		expandMenu();

		boolean toReturn = !getCurrentUserID().getText().equalsIgnoreCase("Not logged in");
		log.info("User login status is: " + toReturn);

		contractMenu();
		return toReturn;
	}

	public void logout() throws Exception {

		wait.forXMillis(miniWaitTime);

		expandMenu();
		log.info("Logging out...");
		getLogoutLnk().click();
		contractMenu();
	}


}
