package ddsl.dcomponents;

import ddsl.dobjects.DButton;
import ddsl.dobjects.DLink;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SandwichMenu extends DComponent {
	public SandwichMenu(WebDriver driver) {
		super(driver);
		log.info("sandwich menu init");
	}

//	@FindBy(id = "settingsmenu_id")
//	WebElement expandButton;
//
//	@FindBy(css = "div.mat-menu-content.ng-trigger.ng-trigger-fadeInItems")
//	WebElement menuContainer;
//
//	@FindBy(css = "button[role=\"menuitem\"] span")
//	WebElement currentUserID;
//
//	@FindBy(id = "logout_id")
//	WebElement logoutLnk;
//

	By expandButton = By.id("settingsmenu_id");

	By menuContainer = By.cssSelector("div.mat-menu-content.ng-trigger.ng-trigger-fadeInItems");

	By currentUserID = By.cssSelector("button[role=\"menuitem\"]:nth-of-type(1) span");

	By logoutLnk = By.id("logout_id");












	public DButton getExpandButton() {
		return new DButton(driver, driver.findElement(expandButton));
	}

	public DObject getCurrentUserID() {
		return new DObject(driver, driver.findElement(currentUserID));
	}

	public DLink getLogoutLnk() {
		return new DLink(driver, driver.findElement(logoutLnk));
	}

	private boolean isMenuExpanded() throws Exception{

		try {		driver.findElement(menuContainer);
			return true;
		} catch (Exception e) {	}
		return false;
	}
	private void expandMenu() throws Exception{
		if(isMenuExpanded()) return;
		getExpandButton().click();
		wait.forElementToBeVisible(driver.findElement(currentUserID));
	}

	private void contractMenu() throws Exception{
		if(!isMenuExpanded()) return;
		clickVoidSpace();
	}

	public boolean isLoggedIn() throws Exception {
		expandMenu();

		boolean toReturn = !getCurrentUserID().getText().equalsIgnoreCase("Not logged in");
		log.info("User login status is: " + toReturn);

		contractMenu();
		return toReturn;
	}

	public void logout() throws Exception {
		expandMenu();
		log.info("Logging out...");
		getLogoutLnk().click();
		contractMenu();
	}




//	public DButton getExpandButton() {
//		return new DButton(driver, expandButton);
//	}
//
//	public DObject getCurrentUserID() {
//		return new DObject(driver, currentUserID);
//	}
//
//	public DLink getLogoutLnk() {
//		return new DLink(driver, logoutLnk);
//	}
//
//	private boolean isMenuExpanded() throws Exception{
//		DObject menuContainerObj = new DObject(driver, menuContainer);
//		try {		return menuContainerObj.isPresent();	} catch (Exception e) {	}
//		return false;
//	}
//	private void expandMenu() throws Exception{
//		if(isMenuExpanded()) return;
//		getExpandButton().click();
//	}
//
//	private void contractMenu() throws Exception{
//		if(!isMenuExpanded()) return;
//		clickVoidSpace();
//	}
//
//	public boolean isLoggedIn() throws Exception {
//		expandMenu();
//
//		boolean toReturn = !currentUserID.getText().equalsIgnoreCase("Not logged in");
//		log.info("User login status is: " + toReturn);
//
//		contractMenu();
//		return toReturn;
//	}
//
//	public void logout() throws Exception {
//		expandMenu();
//		log.info("Logging out...");
//		getLogoutLnk().click();
//		contractMenu();
//	}


}
