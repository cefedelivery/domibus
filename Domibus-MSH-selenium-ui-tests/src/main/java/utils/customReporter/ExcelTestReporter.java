package utils.customReporter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import utils.PROPERTIES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelTestReporter implements ITestListener {

	private static final String[] headers = {"Type", "Test Suite Name", "Test Case ID", "Test Case Name", "Can be run on Bamboo", "TC is disabled", "Test Result", "Last Execution Started", "Execution time", "JIRA tickets", "Impact", "Comment"};
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static String filename;


	@Override
	/*Creates the report file, the sheet and writes the headers of the table with style as well*/
	public void onStart(ITestContext iTestContext) {
		
		
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		String dateStr = format.format(iTestContext.getStartDate());
		filename = PROPERTIES.REPORTS_FOLDER + "TestRunReport" + dateStr + ".xlsx";

		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Run Report");

		Row headerRow = sheet.createRow(0);
		XSSFCellStyle headerStyle = composeCellStyle((XSSFWorkbook) sheet.getWorkbook(), "Header");

		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellStyle(headerStyle);
			cell.setCellValue(headers[i]);
		}

		try{
			FileOutputStream os = new FileOutputStream(filename);
			workbook.write(os);
			workbook.close();
			os.close();
		} catch (Exception e){
			e.printStackTrace();
		}

	}

	@Override
	public void onFinish(ITestContext iTestContext) {

	}

	@Override
	public void onTestStart(ITestResult iTestResult) {
		System.out.println("---------------------------------------------------");
		System.out.println(iTestResult.getName());
		System.out.println("---------------------------------------------------");
	}

	/* Writes a row in the report file with the test id, name  and Pass as status */
	@Override
	public void onTestSuccess(ITestResult iTestResult) {
		try {
			writeRowToReportFile(iTestResult, "Pass");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Writes a row in the report file with the test id, name  and FAIL as status*/
	@Override
	public void onTestFailure(ITestResult iTestResult) {
		try {
			writeRowToReportFile(iTestResult, "FAIL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* Writes a row in the report file with the test id, name and Skipped as status */
	@Override
	public void onTestSkipped(ITestResult iTestResult) {
		try {
			writeRowToReportFile(iTestResult, "Skipped");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

	}


	/* depending on the type of cell returns the desired style. The supported type are "Header", "Fail", "Pass" */
	private XSSFCellStyle composeCellStyle(XSSFWorkbook workbook, String type){
		XSSFCellStyle style = workbook.createCellStyle();
		XSSFFont font = workbook.createFont();
		font.setBold(true);

		if(type.equalsIgnoreCase("Pass")){
			style.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());

		}else if (type.equalsIgnoreCase("Fail")){
			style.setFillBackgroundColor(IndexedColors.RED.getIndex());
			style.setFillForegroundColor(IndexedColors.RED.getIndex());
			style.setFont(font);

		}else if (type.equalsIgnoreCase("Skipped")){
			style.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
			style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
			style.setFont(font);

		}
		else if (type.equalsIgnoreCase("Header")){
			style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			style.setFont(font);
		}
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		return style;
	}

	/* Generic method to write a row in the report file with the test id, name and result */
	private void writeRowToReportFile(ITestResult iTestResult, String result) throws Exception {

		String qualifiedName = iTestResult.getMethod().getQualifiedName();
		String testType = "";
		if(qualifiedName.contains(".ui.")){testType = "UI";}
		if(qualifiedName.contains(".rest.")){testType = "REST";}
		
		File myFile = new File(filename);
		FileInputStream inputStream = new FileInputStream(myFile);
		XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

		Sheet reportSheet = workbook.getSheetAt(0);
		int rowNum = reportSheet.getLastRowNum()+1;
		Row curentRow = reportSheet.createRow(rowNum);

		curentRow.createCell(0).setCellValue(testType);
//		curentRow.createCell(0).setCellValue("UI");
		curentRow.createCell(1).setCellValue(iTestResult.getTestContext().getSuite().getName());
		curentRow.createCell(2).setCellValue(iTestResult.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class).description());
		curentRow.createCell(3).setCellValue(iTestResult.getName());
		curentRow.createCell(4).setCellValue("Yes");
		curentRow.createCell(5).setCellValue(!iTestResult.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class).enabled());
		Cell cell = curentRow.createCell(6);
		cell.setCellValue(result);
		cell.setCellStyle(composeCellStyle(workbook, result));
		curentRow.createCell(7).setCellValue(sdf.format(new Date(iTestResult.getStartMillis())));
		curentRow.createCell(8).setCellValue((iTestResult.getEndMillis()-iTestResult.getStartMillis())/1000);
		curentRow.createCell(9).setCellValue("");
		curentRow.createCell(10).setCellValue("");

		if(iTestResult.getThrowable() != null){
			curentRow.createCell(11).setCellValue(iTestResult.getThrowable().getMessage());
		}


		FileOutputStream os = new FileOutputStream(myFile);
		workbook.write(os);
		os.close();
		workbook.close();
		inputStream.close();

	}







}
