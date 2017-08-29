package utility;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestDataRead {
	
	
	private XSSFWorkbook wb;
	public String[][] readtestdata() throws Throwable {

	
	XSSFCell cell;
	File fi = new File(System.getProperty("user.dir") + "/src/main/java/com/DomibusGUI/Data/TestData.xlsx");
	// File fi = new File("C:\\TestData.xlsx");
	FileInputStream fis = new FileInputStream(fi);
	wb = new XSSFWorkbook(fis);
	XSSFSheet sheet1 = wb.getSheetAt(0);
	int rowcount = sheet1.getLastRowNum() + 1;
	int colcount = sheet1.getRow(0).getLastCellNum();
	System.out.println("Total no of Rows" + rowcount);
	System.out.println("Total no of columns" + colcount);
	String[][] exData = new String[rowcount][colcount];

	DataFormatter df = new DataFormatter(); // For data convert to
											// String......

	for (int i = 0; i < rowcount; i++) {
		for (int j = 0; j < colcount; j++) {
			cell = sheet1.getRow(i).getCell(j);
			String value = df.formatCellValue(cell);

				if (cell != null) {
					value = df.formatCellValue(cell);
					exData[i][j] = value;

				}

				else
					continue;

			}
	
		}
	return exData;
	}
	
}
