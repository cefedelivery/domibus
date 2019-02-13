package ddsl.dcomponents.grid;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.Select;
import ddsl.dobjects.DLink;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class Pagination extends DComponent {


	public Pagination(WebDriver driver) {
		super(driver);

		log.info("initiating pagination controls!");
		PageFactory.initElements( driver, this);

	}

	@FindBy(css = "li.pages")
	List<WebElement> pgLinks;

	@FindBy(css = "li.pages.active")
	WebElement activePageLnk;

	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-child(1)")
	WebElement skipFirstLnk;

	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-last-child(1)")
	WebElement skipLastLnk;

	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-last-child(2)")
	WebElement nextPageLnk;

	@FindBy(css = "datatable-footer > div > datatable-pager > ul > li:nth-child(2)")
	WebElement prevPageLnk;


	@FindBy(id = "pagesize_id")
	WebElement pageSizeSelectContainer;

	@FindBy(css = "datatable-footer > div > div.page-count")
	WebElement pageCount;

	public Select getPageSizeSelect() {
		return new Select(driver, pageSizeSelectContainer);
	}

	public boolean hasNextPage(){

		try {
			return !("disabled".equalsIgnoreCase(getNextPageLnk().getAttribute("class")));
		} catch (Exception e) {	}
		return false;
	}

	public int getExpectedNoOfPages() throws Exception{

		try {
			log.info("getting expected number of pages");

			int noOfItems = getTotalItems();
			int itemsPerPg = Integer.valueOf(getPageSizeSelect().getSelectedValue());

			return (int) Math.ceil((double)noOfItems/itemsPerPg);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getNoOfItemsOnLastPg()throws Exception{

		try {
			log.info("getting expected number of items on last page");

			int noOfItems = getTotalItems();
			int itemsPerPg = Integer.valueOf(getPageSizeSelect().getSelectedValue());

			return noOfItems%itemsPerPg;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean isPaginationPresent(){
		log.info("checking if pagination is present on page");
		return (getActivePageLnk().isPresent());
	}

	//	if pagination is not present we return 1 by default
	public Integer getActivePage() throws Exception{

		try {
			log.info("getting active page number");

			if(!getActivePageLnk().isPresent()){return 1;}
			return Integer.valueOf(getActivePageLnk().getLinkText());
		} catch (NumberFormatException e) {	}
		return -1;
	}

	public void goToPage(int pgNo)throws Exception{

		log.info("going to page .. " + pgNo);

		try {
			for (WebElement pgLink : pgLinks) {

				DLink pageLink = new DLink(driver, pgLink);
				if(Integer.valueOf(pageLink.getText()) == pgNo){
					pageLink.click();
					PageFactory.initElements(driver, this);
					return;
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}


	public void skipToFirstPage() throws Exception{
		log.info("skip to FIRST page of results");

		try {
			getSkipFirstLnk().click();
		} catch (Exception e) {	}
		PageFactory.initElements(driver, this);

	}

	public void skipToLastPage()throws Exception{
		log.info("skip to last page of results");
		getSkipLastLnk().click();
		PageFactory.initElements(driver, this);
	}

	public void goToNextPage() throws Exception{
		log.info("going to next page");
		getNextPageLnk().click();
		PageFactory.initElements(driver, this);
	}

	public void goToPrevPage() throws Exception{
			log.info("going to prev page");
			getPrevPageLnk().click();
			PageFactory.initElements(driver, this);
	}


	public int getTotalItems()throws Exception {

		try {
			log.info("getting total number of items to be displayed");

			String raw = pageCount.getText().trim();
			if(raw.contains("total")){
				String[] splits = raw.split("/");
				for (String split : splits) {
					if(split.contains("total")){
						String total = split.replaceAll("\\D", "");
						return Integer.valueOf(total);
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public Integer getNoOfSelectedItems() throws Exception{

		try {
			log.info("getting number of selected items in grid");

			String raw = pageCount.getText().trim();
			if(raw.contains("selected")){
				String[] splits = raw.split("/");
				for (String split : splits) {
					if(split.contains("selected")){
						String selected = split.replaceAll("\\D", "");
						return Integer.valueOf(selected);
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DLink getActivePageLnk() {
		return new DLink(driver, activePageLnk);
	}

	public DLink getSkipFirstLnk() {
		return new DLink(driver, skipFirstLnk);
	}

	public DLink getSkipLastLnk() {
		return new DLink(driver, skipLastLnk);
	}

	public DLink getNextPageLnk() {
		return new DLink(driver, nextPageLnk);
	}

	public DLink getPrevPageLnk() {
		return new DLink(driver, prevPageLnk);
	}

	public DObject getPageCount() {
		return new DObject(driver, pageCount);
	}
}
