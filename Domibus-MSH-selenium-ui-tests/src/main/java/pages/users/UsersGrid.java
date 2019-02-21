package pages.users;

import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class UsersGrid extends DGrid {
	public UsersGrid(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	public boolean isDeleted(String username) throws Exception {
		int index = scrollTo("Username", username);
		WebElement row = gridRows.get(index);
		int colIndex = getColumnNames().indexOf("Username");
		WebElement cell = row.findElements(cellSelector).get(colIndex);
		String classes = cell.findElement(By.tagName("span")).getAttribute("class");

		return classes.contains("user-deleted");
	}


}
