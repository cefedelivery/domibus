package domibus.ui;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.enums.DOMIBUS_PAGES;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.errorLog.ErrorLogPage;
import pages.login.LoginPage;

import java.util.HashMap;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class ErrorLogPgTest extends BaseTest {


	@BeforeMethod(alwaysRun = true)
	private void login() throws Exception {
		new LoginPage(driver)
				.login(data.getAdminUser());
		new DomibusPage(driver).getSidebar().getPageLnk(DOMIBUS_PAGES.ERROR_LOG).click();
	}


	@Test(description = "ERRLOG-1")
	public void openErrorLogPage() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);

		soft.assertTrue(errorLogPage.isLoaded(), "Expected elements appear in the page");
		soft.assertAll();
	}


	@Test(description = "ERRLOG-2")
	public void filterErrorLog() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage errorLogPage = new ErrorLogPage(driver);

		soft.assertTrue(errorLogPage.isLoaded());

		DGrid grid = errorLogPage.getGrid();

		if (grid.getRowsNo() < 3) {
			throw new SkipException("Not enough rows to test filtering");
		}

		HashMap<String, String> row = grid.getRowInfo(0);

		errorLogPage.basicSearch(null, row.get("Message Id"), null, null);

		HashMap<String, String> row2 = errorLogPage.getGrid().getRowInfo(0);

		soft.assertTrue(row2.equals(row), "Errors for correct message id is displayed");

		soft.assertAll();
	}


	@Test(description = "ERRLOG-3")
	public void paginationTest() throws Exception {
		SoftAssert soft = new SoftAssert();
		ErrorLogPage page = new ErrorLogPage(driver);

		soft.assertTrue(page.isLoaded());

		Pagination pgCtrl = page.getGrid().getPagination();

		int noOfErrors = pgCtrl.getTotalItems();
		if (noOfErrors < 11) {
			throw new SkipException("Cannot test pagination because with so little errors");
		}

		if (!pgCtrl.isPaginationPresent() && pgCtrl.getExpectedNoOfPages() > 1) {
			soft.fail("Pagination controls are not present although expected number of pages is bigger than 1");
		}

		soft.assertEquals(pgCtrl.getPageSizeSelect().getSelectedValue(), "10", "Default page size is 10");

		pgCtrl.skipToLastPage();
		soft.assertTrue(pgCtrl.getActivePage() == pgCtrl.getExpectedNoOfPages(), "Skipped to last page");

		soft.assertEquals(page.getGrid().getRowsNo(), pgCtrl.getNoOfItemsOnLastPg(), "Number of items on the last page");

		pgCtrl.skipToFirstPage();
		soft.assertTrue(pgCtrl.getActivePage() == 1, "Skipped to first page");

		pgCtrl.goToNextPage();
		soft.assertTrue(pgCtrl.getActivePage() == 2, "Page 2");

		pgCtrl.goToPrevPage();
		soft.assertTrue(pgCtrl.getActivePage() == 1, "Previous page is 1");

		pgCtrl.goToPage(2);
		soft.assertTrue(pgCtrl.getActivePage() == 2, "Next page is 2");

		page.getGrid().sortBy("Error Code");
		soft.assertTrue(pgCtrl.getActivePage() == 1, "After sorting the active page is 1");
		pgCtrl.goToPage(2);

		pgCtrl.getPageSizeSelect().selectOptionByText("25");
		pgCtrl.skipToLastPage();
		soft.assertTrue(pgCtrl.getActivePage() == pgCtrl.getExpectedNoOfPages());

		soft.assertAll();
	}


}
