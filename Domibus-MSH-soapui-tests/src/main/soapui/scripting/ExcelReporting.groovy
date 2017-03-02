/**
 * Created by kopecgr on 22/02/2017.
 */

import org.apache.poi.ss.usermodel.*
class ExcelReporting
{
	//Excel template report file columns indexes (0 base indexed) 
	static final def TC_TEST_SUITE_COLUMN = 1	// Test Suite Name 
	static final def TC_ID_COLUMN = 2			// Test Case ID (assuming  everything before first dash in test case name is test case ID)
	static final def TC_NAME_COLUMN = 3			// Full Test Case Name (with test case ID)
	static final def TC_DISABLED_COLUMN = 5		// Is Test Case Disabled in SoapUI project
	static final def TC_RESULT_COLUMN = 6		// Last Result of test case execution [see bellow REPORT_PASS_STRING/REPORT_FAIL_STRING]
	static final def TC_TIME_COLUMN = 7			// Time of last Execution was Started 
	static final def TC_EXEC_TIME_COLUMN = 8	// Test case execution time in seconds 
	static final def TC_COMMENT_COLUMN = 10		// Collected information about failed assertion, empty for passing TCs
		
	static final def newLine = System.getProperty("line.separator") 
	
	static final def REPORT_PASS_STRING = "PASS"
	static final def REPORT_FAIL_STRING = "FAIL"
	
	private static def showResultRow(row, log) {
		log.debug "TC_TEST_SUITE_COLUMN: " + row.getCell(TC_TEST_SUITE_COLUMN)
		log.debug "TC_ID_COLUMN: " + row.getCell(TC_ID_COLUMN).getStringCellValue()
		log.debug "TC_NAME_COLUMN: " + row.getCell(TC_NAME_COLUMN)
		log.debug "TC_DISABLED_COLUMN: " + row.getCell(TC_DISABLED_COLUMN)
		log.debug "TC_RESULT_COLUMN: " + row.getCell(TC_RESULT_COLUMN)
		log.debug "TC_TIME_COLUMN: " + row.getCell(TC_TIME_COLUMN)
		log.debug "TC_EXEC_TIME_COLUMN: " + row.getCell(TC_EXEC_TIME_COLUMN) 
		log.debug "TC_COMMENT_COLUMN: " + row.getCell(TC_COMMENT_COLUMN)
	}
	
	static def reportTestCase(testRunner, log) {
		// check update report property is not true or '1' 
		def updateReport = testRunner.getRunContext().expand( '${#Project#updateReport}' )
		if (updateReport == null || updateReport.trim().isEmpty() || !(updateReport.toLowerCase().equals('true') || updateReport == '1'))
		{
			log.warn "Reporting disabled, please refer to SoapUI Project level property updateReport"
			return
		}
		
		//check report file exist
		log.debug "check report file exist"
		def outputReportFilePath = testRunner.getRunContext().expand( '${#Project#reportFilePath}' ) as String
		
		def file = new File(outputReportFilePath)
		if (!file.exists() || file.isDirectory()) {
			log.error "Error: template report Excel file doesn't exist or is directory on path:" + outputReportFilePath
			return
		}
		file.finalize()

		// project name it should same as worksheet name
		def projectName = testRunner.testCase.testSuite.project.name
		def testSuiteName = testRunner.testCase.testSuite.getLabel()
		// extract test case ID 
		def tcName = testRunner.testCase.getLabel()
		def searchedID = tcName.split("-").getAt(0).trim()

		// File exist than read file
		log.debug "Try to open Workbook and specific Worksheet"
		InputStream inp = new FileInputStream(outputReportFilePath)
		Workbook wb = WorkbookFactory.create(inp)
		Sheet sheet = wb.getSheet(projectName)
		if (sheet == null) {
			log.error "Worksheet with name: |" + projectName + "| doesn't exist."
			return
		}
		
		// Find row with specific Test Case ID
		def searchedTestCaseRowNum = -1
		for (Row r : sheet) {
			def testCaseIdValue = r.getCell(TC_ID_COLUMN).getStringCellValue().trim()
			if (testCaseIdValue == searchedID) {
				log.debug "Found test case ID |" + searchedID + "| in template report file"
				searchedTestCaseRowNum = r.getRowNum()
				break
			}
		}
		if (!(searchedTestCaseRowNum>0)) {
			log.error "Test case ID: |" + searchedID + "| not found" 
			return
		}
		
		def row = sheet.getRow(searchedTestCaseRowNum) 
		//old values
		showResultRow(row, log) 
		
		//if some steps were executed put new values in report
		if (testRunner.getResults().size()>0) 
		{
		
			def tcStatus = testRunner.getStatus().toString().equals("FINISHED")? REPORT_PASS_STRING : REPORT_FAIL_STRING
			def startTime = new Date(testRunner.getStartTime())
			def timeTaken = testRunner.getTimeTaken()/1000 + "s"
			def comment = ""

			testRunner.getResults().each{ t->
				def stepStatus = t.status.toString()
				def stepName = t.getTestStep().getLabel()
				def stepNum = (testRunner.getResults().indexOf(t) as Integer) +1
				def executionStart = new Date(t.getTimeStamp()).format("dd-MM-yyyy HH:mm:ss")

				log.debug "Check status of step " + stepNum + " - " + stepName + " --> " + stepStatus

				if (!(stepStatus == "OK" || comment == "")) {
					comment += newLine
				}

				if (stepStatus == "FAILED")
				{
					log.debug "Found test step with FAILED status try extract error messages"
					def messages = ""
					t.getMessages().each() { msg -> messages += newLine + " |" + msg + "| " }

					comment += executionStart + ": Test case FAILED on step " + stepNum + ": " + stepName + "|| Returned error message[s]: " + messages
				}
				if (stepStatus == "CANCELED")
				{
					log.debug "Found test step with CANCELED status"
					comment += executionStart + ": Test case CANCELED on step " + stepNum + ": " + stepName
				}

			}
		//update values
		log.info "REPORTING Test case: \"" + tcName + "\" " +  tcStatus + "ED, details in the report file"
		row.getCell(TC_TEST_SUITE_COLUMN).setCellValue(testSuiteName)
		row.getCell(TC_NAME_COLUMN).setCellValue(tcName)
		row.getCell(TC_DISABLED_COLUMN).setCellValue(testRunner.testCase.isDisabled())
		row.getCell(TC_RESULT_COLUMN).setCellValue(tcStatus)
		row.getCell(TC_TIME_COLUMN).setCellValue(DateUtil.getExcelDate(startTime))
		row.getCell(TC_EXEC_TIME_COLUMN).setCellValue(timeTaken)
		row.getCell(TC_COMMENT_COLUMN).setCellValue(comment)
		}
		
		// new row values 
		showResultRow(row, log) 
		
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(outputReportFilePath)
		wb.write(fileOut)
		wb.finalize()
		fileOut.close()

	}
	
}    
