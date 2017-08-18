/**
 * Created by ghouiah on 16/09/2016.
 */

import groovy.sql.Sql

import javax.swing.JOptionPane
import java.sql.SQLException

import static javax.swing.JOptionPane.showConfirmDialog
import groovy.util.AntBuilder


class Domibus
{
    def messageExchange=null
    def context=null
    def log=null
	// sleepDelay value is increased from 2000 to 20000 because of pull request take longer ...
    def sleepDelay=20000;
    def sqlBlue=null;
    def sqlRed=null;
	def sqlGreen=null;
	def thirdGateway = "false";


    // Short constructor of the Domibus Class
    Domibus(log, messageExchange, context) {
        this.log = log;
        this.messageExchange = messageExchange;
        this.context=context;
		thirdGateway = context.expand( '${#Project#thirdGateway}' )
    }

    // Class destructor
    void finalize() {
        log.info "Test finished."
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Connect to a schema
	def connectTo(String database, String driver, String url, String dbUser, String dbPassword){
		log.info("Open connection to DB: " + database + " Url: " + url);
		
		def sql = null;

        try{
            if (database.toLowerCase() == "mysql" || database.toLowerCase() == "oracle" ){
                sql = Sql.newInstance(url, dbUser, dbPassword, driver)
			}else{
                sql = Sql.newInstance(url, driver)
			}
			log.info "Connection opened with success";
			return sql;
        }
        catch (SQLException ex){
            assert 0,"SQLException occurred: " + ex;
        }
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII	
    // Open 3 DB connections
    def openConnection(){
        //log.debug "Open DB connections"
		
		sqlBlue=connectTo(context.expand( '${#Project#databaseBlue}' ),context.expand('${#Project#driverBlue}'),context.expand('${#Project#jdbcUrlBlue}'),context.expand( '${#Project#blueDbUser}' ),context.expand( '${#Project#blueDbPassword}' ));
		sqlRed=connectTo(context.expand( '${#Project#databaseRed}' ),context.expand('${#Project#driverRed}'),context.expand('${#Project#jdbcUrlRed}'),context.expand( '${#Project#redDbUser}' ),context.expand( '${#Project#redDbPassword}' ));
		if(thirdGateway.toLowerCase().trim()=="true"){
			sqlGreen=connectTo(context.expand( '${#Project#databaseGreen}' ),context.expand('${#Project#driverGreen}'),context.expand('${#Project#jdbcUrlGreen}'),context.expand( '${#Project#greenDbUser}' ),context.expand( '${#Project#greenDbPassword}' ));
		}
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Close the DB connection opened previously
    def closeConnection(){
        log.debug "DB connection would be closed"
        if(sqlBlue){
            sqlBlue.connection.close()
            sqlBlue = null
        }
        if(sqlRed){
            sqlRed.connection.close()
            sqlRed = null
        }
		if(sqlGreen){
            sqlGreen.connection.close()
            sqlGreen = null
        }
		
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Extract messageID from the request if it exists
    String findGivenMessageID(){
        def messageID = null
        def requestContent = messageExchange.getRequestContentAsXml()
        def requestFile = new XmlSlurper().parseText(requestContent)
        requestFile.depthFirst().each{
            if(it.name()== "MessageId"){
                messageID=it.text().toLowerCase()
            }
        }
        return(messageID)
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Extract messageID from the response
    String findReturnedMessageID(){
        def messageID=null
        def responseContent = messageExchange.getResponseContentAsXml()
        def responseFile = new XmlSlurper().parseText(responseContent)
        responseFile.depthFirst().each{
            if(it.name()== "messageID"){
                messageID=it.text()
            }
        }
        assert (messageID != null),locateTest(context)+"Error:findReturnedMessageID: The message ID is not found in the response"
        if((findGivenMessageID()!=null) && (findGivenMessageID().trim()!="")){
            //if(findGivenMessageID()!=null){
            assert (messageID.toLowerCase() == findGivenMessageID().toLowerCase()),locateTest(context)+"Error:findReturnedMessageID: The message ID returned is ("+messageID+"), the message ID provided is ("+findGivenMessageID()+")."
        }
        return(messageID.toLowerCase())
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Verification of message existence
    def verifyMessagePresence(int presence1,int presence2, String IDMes=null, int mapDoms = 3){
        def messageID=null;
		def sqlSender = null; def sqlReceiver = null;
        sleep(sleepDelay);
		
        if(IDMes!=null){
            messageID=IDMes
        }
        else{
            messageID=findReturnedMessageID()
        }
        def total=0
        openConnection();
		// Choose 2 Domibus between blue, red and green
		switch(mapDoms){
			case 3:
				//log.info "sqlSender = sqlBlue; sqlReceiver = sqlRed";
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
			case 5:
				//log.info "sqlSender = sqlBlue; sqlReceiver = sqlGreen";
				sqlSender = sqlBlue; sqlReceiver = sqlGreen;
				break;
			case 6:
				//log.info "sqlSender = sqlRed; sqlReceiver = sqlGreen";
				sqlSender = sqlRed; sqlReceiver = sqlGreen;
				break;
			default:
				//log.info "sqlSender = sqlBlue; sqlReceiver = sqlRed";
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
		}

        // Sender DB
        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        if(presence1==1){
            //log.info "total = "+total
            assert(total>0),locateTest(context)+"Error:verifyMessagePresence: Message with ID "+messageID+" is not found in sender side."
        }
        if(presence1==0){
            assert(total==0),locateTest(context)+"Error:verifyMessagePresence: Message with ID "+messageID+" is found in sender side."
        }

        // Receiver DB
        total=0
        sleep(sleepDelay)
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        if(presence2==1){
            assert(total>0),locateTest(context)+"Error:verifyMessagePresence: Message with ID "+messageID+" is not found in receiver side."
        }
        if(presence2==0){
            assert(total==0),locateTest(context)+"Error:verifyMessagePresence: Message with ID "+messageID+" is found in receiver side."
        }

        closeConnection();
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Verification of message unicity
    def verifyMessageUnicity(String IDMes=null, int mapDoms = 3){
        sleep(sleepDelay);
        def messageID;
        def total=0;
		def sqlSender = null; def sqlReceiver = null;
		
        if(IDMes!=null){
            messageID=IDMes
        }
        else{
            messageID=findReturnedMessageID()
        }
        openConnection();
		// Choose 2 Domibus between blue, red and green
		switch(mapDoms){
			case 3:
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
			case 5:
				sqlSender = sqlBlue; sqlReceiver = sqlGreen;
				break;
			case 6:
				sqlSender = sqlRed; sqlReceiver = sqlGreen;
				break;
			default:
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
		}
		
        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        assert(total==1),locateTest(context)+"Error:verifyMessageUnicity: Message found "+total+" times in sender side."
        sleep(sleepDelay)
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        assert(total==1),locateTest(context)+"Error:verifyMessageUnicity: Message found "+total+" times in receiver side."
        closeConnection()
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Wait until status or timer expire
    def waitForStatus(String SMSH=null,String RMSH=null,String IDMes=null,String bonusTimeForSender=null,String bonusTimeForReceiver=null, int mapDoms = 3){
        def MAX_WAIT_TIME=100_000; // Maximum time to wait to check the message status. 
		def STEP_WAIT_TIME=1000; // Time to wait before re-checking the message status.	
        def messageID=null;
        def numberAttempts=0;
        def maxNumberAttempts=4;
        def messageStatus="INIT";
        def wait=false;
		def sqlSender = null; def sqlReceiver = null;
		
        //log.info "waitForStatus params: messageID: " + messageID + " RMSH: " + RMSH + " IDMes: " + IDMes + " bonusTimeForSender: " + bonusTimeForSender + " bonusTimeForReceiver: " + bonusTimeForReceiver
        if(IDMes!=null){
            messageID=IDMes
        }
        else{
            messageID=findReturnedMessageID()
        }

		log.info "waitForStatus params: messageID: " + messageID +" SMSH: "+SMSH+ " RMSH: " + RMSH + " IDMes: " + IDMes + " bonusTimeForSender: " + bonusTimeForSender + " bonusTimeForReceiver: " + bonusTimeForReceiver
		
        if(bonusTimeForSender){
            log.info "Waiting time for Sender extended to 500 seconds"
            MAX_WAIT_TIME=500_000
        }

        openConnection();
		// Choose 2 Domibus between blue, red and green
		switch(mapDoms){
			case 3:
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
			case 5:
				sqlSender = sqlBlue; sqlReceiver = sqlGreen;
				break;
			case 6:
				sqlSender = sqlRed; sqlReceiver = sqlGreen;
				break;
			default:
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
		}
		
        if(SMSH){
            while(((messageStatus!=SMSH)&&(MAX_WAIT_TIME>0))||(wait)){
                sleep(STEP_WAIT_TIME)
                if(MAX_WAIT_TIME>0){
                    MAX_WAIT_TIME=MAX_WAIT_TIME-STEP_WAIT_TIME
                }
                log.info "maxNumberAttempts-numberAttempts: "+maxNumberAttempts+"-"+numberAttempts
                log.info "WAIT: "+MAX_WAIT_TIME
                sqlSender.eachRow("Select * from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
                    messageStatus=it.MESSAGE_STATUS
                    numberAttempts=it.SEND_ATTEMPTS
                }
                log.info "SENDER: Expected Message Status ="+ SMSH +"-- Current Message Status = "+messageStatus; 
                if((SMSH=="SEND_FAILURE")&&(messageStatus=="WAITING_FOR_RETRY")){
                    if(((maxNumberAttempts-numberAttempts)>0)&&(!wait)){
                        wait=true
                    }
                    if((maxNumberAttempts-numberAttempts)<=0){
                        wait=false
                    }
                }
            }
            log.info "finished checking sender, messageStatus: " + messageStatus + " MAX_WAIT_TIME: " + MAX_WAIT_TIME

            assert(messageStatus!="INIT"),locateTest(context)+"Error:waitForStatus: Message "+messageID+" is not present in the sender side."
            assert(messageStatus.toLowerCase()==SMSH.toLowerCase()),locateTest(context)+"Error:waitForStatus: Message in the sender side has status "+messageStatus+" instead of "+SMSH+"."
        }
        if (bonusTimeForReceiver)
        {
            log.info "Waiting time for Receiver extended to 500 seconds"
            MAX_WAIT_TIME=200_000
        }
        else
        {
            MAX_WAIT_TIME=10_000
        }
        messageStatus="INIT"
        if(RMSH){
            while((messageStatus!=RMSH)&&(MAX_WAIT_TIME>0)){
                sleep(STEP_WAIT_TIME)
                MAX_WAIT_TIME=MAX_WAIT_TIME-STEP_WAIT_TIME
                sqlReceiver.eachRow("Select * from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
                    messageStatus=it.MESSAGE_STATUS
                }
                log.info "W:" + MAX_WAIT_TIME + " M:" + messageStatus
            }
            log.info "finished checking receiver, messageStatus: " + messageStatus + " MAX_WAIT_TIME: " + MAX_WAIT_TIME
            assert(messageStatus!="INIT"),locateTest(context)+"Error:waitForStatus: Message "+messageID+" is not present in the receiver side."
            assert(messageStatus.toLowerCase()==RMSH.toLowerCase()),locateTest(context)+"Error:waitForStatus: Message in the receiver side has status "+messageStatus+" instead of "+RMSH+"."
        }
        closeConnection()
    }

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    def executeListOfQueriesOnAllDB(String[] sqlQueriesList) {
        executeListOfSqlQueries(sqlQueriesList,"BLUE");
        executeListOfSqlQueries(sqlQueriesList,"RED");
		if(thirdGateway.toLowerCase().trim()=="true"){
			executeListOfSqlQueries(sqlQueriesList,"GREEN");
		}
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    def executeListOfQueriesOnBlue(String[] sqlQueriesList) {
        log.info "Executing SQL queries on sender/Blue"
        executeListOfSqlQueries(sqlQueriesList,"BLUE")
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    def executeListOfQueriesOnRed(String[] sqlQueriesList) {
        log.info "Executing SQL queries on receiver/Red"
        executeListOfSqlQueries(sqlQueriesList,"RED")
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII	
	def executeListOfQueriesOnGreen(String[] sqlQueriesList) {
        log.info "Executing SQL queries on Third/Green"
        executeListOfSqlQueries(sqlQueriesList,"GREEN")
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    def executeListOfSqlQueries(String[] sqlQueriesList, String targetSchema) {
        def connectionOpenedInsideMethod = false
        def sqlDB
        if (!((sqlRed) && (sqlBlue) && (sqlGreen))) {
            log.debug "Method executed without connections open to the DB - try to open connection"
            openConnection()
            connectionOpenedInsideMethod = true
        }

        switch (targetSchema) {
            case "RED":
                sqlDB = this.sqlRed
                break
            case "BLUE":
                sqlDB = this.sqlBlue
                break
			case "GREEN":
                sqlDB = this.sqlGreen
                break
            default:
                log.error "Not supported schema type: " + targetSchema
                return
        }

        for (query in sqlQueriesList) {
            log.debug "Executing SQL query: " + query
            sqlDB.execute query
        }

        if (connectionOpenedInsideMethod) {
            log.debug "Connection to DB opened during method execution - close opened connection"
            closeConnection()
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Clean all the messages from the DB
    def cleanDatabaseAll(){
        log.info "Clean all message related information from DB"
        openConnection()


        def sqlQueriesList = [
				"delete from TB_RAWENVELOPE_LOG",
                "delete from TB_RECEIPT_DATA",
                "delete from TB_PROPERTY",
                "delete from TB_PART_INFO",
                "delete from TB_PARTY_ID",
                //"delete from TB_PARTY_ID",
                "delete from TB_MESSAGING",
                "delete from TB_ERROR",
                "delete from TB_USER_MESSAGE",
                "delete from TB_SIGNAL_MESSAGE",
                "delete from TB_RECEIPT",
                "delete from TB_MESSAGE_INFO",
                "delete from TB_MESSAGE_LOG"
        ] as String[]

        executeListOfQueriesOnAllDB(sqlQueriesList)

        closeConnection()
		log.info "Cleaning Done."
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Clean single message identified by messageID starting with provided value
    def cleanDBMessageIDStartsWith(String messageID){
        cleanDBMessageID(messageID, true)
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Clean single message identified by ID
    def cleanDBMessageID(String messageID, boolean  messgaeIDStartWithProvidedValue = false){
        log.info "Clean from DB information related to the message with ID: " + messageID
        openConnection()

        def messageIDCheck = "= '${messageID}'" //default comparison method use equal operator
        if (messgaeIDStartWithProvidedValue)
            messageIDCheck = "like '${messageID}%'" //if cleanDBMessageIDStartsWith method was called change method for comparison

        def select_ID_PK = "select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID ${messageIDCheck}" //extracted as common part of queries bellow
        def sqlQueriesList = [
                "delete from TB_RECEIPT_DATA where RECEIPT_ID IN (select ID_PK from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
                "delete from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_PROPERTY where MESSAGEPROPERTIES_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_PROPERTY where PARTPROPERTIES_ID IN (select ID_PK from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
                "delete from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_PARTY_ID where FROM_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_PARTY_ID where TO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_MESSAGING where (SIGNAL_MESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))) OR (USER_MESSAGE_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
                "delete from TB_ERROR where SIGNALMESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
				"delete from TB_RAWENVELOPE_LOG where USERMESSAGE_ID_FK IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
				"delete from TB_RAWENVELOPE_LOG where SIGNALMESSAGE_ID_FK IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + " OR REF_TO_MESSAGE_ID " + messageIDCheck + "))",
                "delete from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")",
                "delete from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + " OR REF_TO_MESSAGE_ID " + messageIDCheck + ")",
                "delete from TB_MESSAGE_INFO where MESSAGE_ID " + messageIDCheck + " OR REF_TO_MESSAGE_ID " + messageIDCheck + "",
                "delete from TB_MESSAGE_LOG where MESSAGE_ID " + messageIDCheck + ""
        ] as String[]

        executeListOfQueriesOnAllDB(sqlQueriesList)

        closeConnection()
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Static methods
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Returns: "--TestCase--testStep--"  
    static def String locateTest(context){
        return("--"+context.testCase.name+"--"+context.testCase.getTestStepAt(context.getCurrentStepIndex()).getLabel()+"--  ");
    }

    static def getStatusRetriveStatus(log, context, messageExchange) {
        def outStatus=null
        def responseContent = messageExchange.getResponseContentAsXml()
        def requestFile = new XmlSlurper().parseText(responseContent)
        requestFile.depthFirst().each{
            if(it.name()== "getMessageStatusResponse"){
                outStatus=it.text()
            }
        }
        assert (outStatus != null),locateTest(context)+"Error:getStatusRetriveStatus: Not able to return status from response message"
        return outStatus
    }

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def showPopUpForManualCheck(messagePrefix, log, testRunner) {
        def message = messagePrefix + """

		After the manual check of the expected result:
		- Click 'Yes' when result is correct.
		- Click 'No' when result is incorrect. 
		- Click 'Cancel' to skip this check."""

        def result=showConfirmDialog( null,message)
        if(result == JOptionPane.YES_OPTION)
        {
            log.info "PASS MANUAL TEST STEP: Result as expected, continuing the test."
        }
        else if(result == JOptionPane.NO_OPTION)
        {
            log.info "FAIL MANUAL TEST STEP: Manual check unsuccessful."
            testRunner.fail("Manual check indicated as unsuccessful by user.")
        }
        else if (result == JOptionPane.CANCEL_OPTION)
        {
            log.info "SKIP MANUAL TEST STEP: Check skipped bu user."
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def showPopUpForManualConfigurationChange(messagePrefix, log, testRunner) {
        def message = messagePrefix + """

		Did configuration was changed?
		- Click 'Yes' when configuration was changed.
		- Click 'No' when configuration was not changed, this test step would be marked as failed.
		- Click 'Cancel' to skip this configuration change, the test would be continue from next test step."""

        def result=showConfirmDialog( null,message)
        if(result == JOptionPane.YES_OPTION)
        {
            log.info "User indicated configuration was changed as described in test step, continuing the test."
        }
        else if(result == JOptionPane.NO_OPTION)
        {
            log.info "User indicated configuration wasn't changed, this test step would be marked as failed."
            testRunner.fail("User indicated configuration wasn't changed, this test step would be marked as failed.")
        }
        else if (result == JOptionPane.CANCEL_OPTION)
        {
            log.info "This configuration changed was skipped, continue with next test step."
        }
    }

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Start several gateways
    static def startSetMSHs(int dom1,int dom2,int dom3, context, log){
 
		if(dom1>0){
			startMSH("sender", context, log);
		}
		if(dom2>0){
			startMSH("receiver", context, log);
		}
		if(dom3>0){
			startMSH("receivergreen", context, log);
		}		
	} 

    // Start Gateway
    static def startMSH(String side, context, log){
		def MAX_WAIT_TIME=100000; // Maximum time to wait for the domibus to start. 
		def STEP_WAIT_TIME=2000; // Time to wait before re-checking the domibus status.
		def confirmation = 0;
        def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def pathS=context.expand( '${#Project#pathExeSender}' );
        def pathR=context.expand( '${#Project#pathExeReceiver}' );
		def pathRG=context.expand( '${#Project#pathExeGreen}' );
        def proc=null;
		def passedDuration=0;
 
		// In case of ping failure try 2 times: from experience, sometimes domibus is running and for some reason the ping fails (trying 2 times could reduce the error occurence).
		while(confirmation<=1){ 		
			if(pingMSH(side,context,log).equals("200")){
				log.info side.toUpperCase()+" is already running!";
				confirmation++;
			}
			else{
				if(confirmation>0){
					log.info "Trying to start the " + side.toUpperCase()
					if(side.toLowerCase()=="sender"){
						proc="cmd /c cd ${pathS} && startup.bat".execute()
					}
					else{
						if(side.toLowerCase()=="receiver"){
							proc="cmd /c cd ${pathR} && startup.bat".execute()
						}
						else{
							if((side.toLowerCase()=="receivergreen")){
								proc="cmd /c cd ${pathRG} && startup.bat".execute()
							}
							else{
								assert (false) , "Incorrect side"
							}
						}
					}		
					if(proc!=null){
						proc.consumeProcessOutput(outputCatcher, errorCatcher)
						proc.waitFor()
					}
					assert((!errorCatcher)&&(proc!=null)), locateTest(context)+"Error:startMSH: Error while trying to start the MSH."
					while((!pingMSH(side,context,log).equals("200"))&&(passedDuration<MAX_WAIT_TIME)){
						passedDuration=passedDuration+STEP_WAIT_TIME
						sleep(STEP_WAIT_TIME)
					}
					assert(pingMSH(side,context,log).equals("200")),locateTest(context)+"Error:startMSH: Error while trying to start the MSH."
					log.info "--- DONE - " + side.toUpperCase() + " started ---"
				}			
			}
			sleep(STEP_WAIT_TIME);
			confirmation++;
		}
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Stop several gateways
    static def stopSetMSHs(int dom1,int dom2,int dom3, context, log){
 
		if(dom1>0){
			stopMSH("sender", context, log);
		}
		if(dom2>0){
			stopMSH("receiver", context, log);
		}
		if(dom3>0){
			stopMSH("receivergreen", context, log);
		}		
	}
	
	// Stop Gateway
    static def stopMSH(String side, context, log){
		def MAX_WAIT_TIME=5000; // Maximum time to wait for the domibus to stop.
		def STEP_WAIT_TIME=500; // Time to wait before re-checking the domibus status.
        def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def proc=null;
        def pathS=context.expand( '${#Project#pathExeSender}' );
        def pathR=context.expand( '${#Project#pathExeReceiver}' );
		def pathRG=context.expand( '${#Project#pathExeGreen}' );
		def passedDuration=0;

		if(!pingMSH(side,context,log).equals("200")){
			log.info side.toUpperCase()+" is not running!"
		}
		else{
			log.info "Trying to stop the " + side.toUpperCase();
			switch(side.toLowerCase()){
				case "sender":
					proc="cmd /c cd ${pathS} && shutdown.bat".execute();
					break;
				case "receiver":
					proc="cmd /c cd ${pathR} && shutdown.bat".execute();
					break;
				case "receivergreen":
					proc="cmd /c cd ${pathRG} && shutdown.bat".execute();
					break;
				default:
					assert (false) , "Unknown side.";
			}
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
			assert((!errorCatcher)&&(proc!=null)),locateTest(context)+"Error:stopMSH: Error while trying to stop the MSH."
			while((pingMSH(side,context,log).equals("200"))&&(passedDuration<MAX_WAIT_TIME)){
				passedDuration=passedDuration+STEP_WAIT_TIME;
				sleep(STEP_WAIT_TIME);
			}
			assert(!pingMSH(side,context,log).equals("200")),locateTest(context)+"Error:startMSH: Error while trying to stop the MSH."
			log.info "--- DONE - " + side.toUpperCase() + " stopped ---"
		}
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Switch the jdbcURLs
	static def swapJdbcUrls(String msh1,String msh2, testRunner, log){
		def name1 = "jdbcUrl"+msh1;
		def name2 = "jdbcUrl"+msh2;
		def jdbcUrlMsh1 = testRunner.testCase.testSuite.project.getPropertyValue(name1);
		def jdbcUrlMsh2 = testRunner.testCase.testSuite.project.getPropertyValue(name2);
		
		testRunner.testCase.testSuite.project.setPropertyValue(name1,jdbcUrlMsh2);
		testRunner.testCase.testSuite.project.setPropertyValue(name2,jdbcUrlMsh1);
		
		log.info "jdbcURLs swap done between "+msh1+" and "+msh2;

	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def uploadPmode(String side, String baseFilePath, String extFilePath,context,log, String outcome = "successfully"){
		log.info "Start upload PMode for Domibus \""+side+"\".";
	    def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def proc=null; def commandString = null; 
		
		def String output = fetchCookieHeader(side,context,log);		
		def XXSRFTOKEN = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=","").replace(";","");
		def String pmodeFile = computePathRessources(baseFilePath,extFilePath,context);
		
		
		switch(side.toLowerCase()){
			case "sender":
				commandString="curl "+context.expand( '${#Project#localUrl}' )+"/rest/pmode -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F  file=@"+pmodeFile;
				break;
			case "receiver":
				commandString="curl "+context.expand( '${#Project#remoteUrl}' )+"/rest/pmode -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F  file=@"+pmodeFile;
				break;
			case "receivergreen":
				commandString="curl "+context.expand( '${#Project#greenUrl}' )+"/rest/pmode -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F  file=@"+pmodeFile;
				break;
			case "testEnv":
				commandString="curl "+context.expand( '${#Project#testEnvUrl}' )+"/rest/pmode -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F  file=@"+pmodeFile;
				break;
			default:
				assert (false) , "Unknown side."
		}

		//log.info commandString
		if(commandString){
			proc = commandString.execute();
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
		}
		//log.info errorCatcher.toString();
		assert(outputCatcher.toString().contains(outcome)),"Error:uploadPmode: Error while trying to connect to domibus."
		if(outcome.toLowerCase()=="successfully"){
			log.info outputCatcher.toString()+" Domibus: \""+side+"\".";
		}
		else{
			log.info "Upload PMode was not done for Domibus: \""+side+"\".";
		}

	} 
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def uploadTruststore(String side, String baseFilePath, String extFilePath,context,log,String tsPassword="test123"){
		log.info "Start upload truststore for Domibus \""+side+"\".";
	    def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def proc=null; def commandString = null; 
		
		def String output = fetchCookieHeader(side,context,log);		
		def XXSRFTOKEN = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=","").replace(";","");
		def String truststoreFile = computePathRessources(baseFilePath,extFilePath,context);
				
		switch(side.toLowerCase()){
			case "sender":
				commandString="curl "+context.expand( '${#Project#localUrl}' )+"/rest/truststore -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F \"password="+tsPassword+"\" -F  truststore=@"+truststoreFile;
				break;
			case "receiver":
				commandString="curl "+context.expand( '${#Project#remoteUrl}' )+"/rest/truststore -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F \"password="+tsPassword+"\" -F  truststore=@"+truststoreFile;
				break;
			case "receivergreen":
				commandString="curl "+context.expand( '${#Project#greenUrl}' )+"/rest/truststore -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F \"password="+tsPassword+"\" -F  truststore=@"+truststoreFile;
				break;
			case "testEnv":
				commandString="curl "+context.expand( '${#Project#testEnvUrl}' )+"/rest/truststore -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -F \"password="+tsPassword+"\" -F  truststore=@"+truststoreFile;
				break;
			default:
				assert (false) , "Unknown side."
		}

		//log.info commandString
		if(commandString){
			proc = commandString.execute();
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
		}
		assert(outputCatcher.toString().contains("successfully")),"Error:uploadTruststore: Error while trying to connect to domibus."
		log.info outputCatcher.toString()+" Domibus: \""+side+"\".";
		

	} 
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def setMessageFilters(String side, String filterChoice,context,log){
		log.info "Start update message filters for Domibus \""+side+"\".";
	    def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def proc=null; def commandString = null;
		def firstBck = "backendWebservice"; def secondBck = "Jms";
		def firstEntId = "1"; def SecondEntId = "2";
		
		def String output = fetchCookieHeader(side,context,log);		
		def XXSRFTOKEN = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=","").replace(";","");
		
		if(filterChoice.toLowerCase()!="ws"){
			firstBck="Jms";firstEntId="2";secondBck="backendWebservice";SecondEntId="1";
		}
				
		switch(side.toLowerCase()){
			case "sender":
				commandString="curl "+context.expand( '${#Project#localUrl}' )+"/rest/messagefilters -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -X PUT -d [\"{\"\"\"entityId\"\"\":"+firstEntId+",\"\"\"index\"\"\":0,\"\"\"backendName\"\"\":\"\"\""+firstBck+"\"\"\",\"\"\"routingCriterias\"\"\":[],\"\"\"from\"\"\":null,\"\"\"to\"\"\":null,\"\"\"action\"\"\":null,\"\"\"sevice\"\"\":null,\"\"\"\$\$index\"\"\":0},{\"\"\"entityId\"\"\":"+SecondEntId+",\"\"\"index\"\"\":1,\"\"\"backendName\"\"\":\"\"\""+secondBck+"\"\"\",\"\"\"routingCriterias\"\"\":[],\"\"\"from\"\"\":null,\"\"\"to\"\"\":null,\"\"\"action\"\"\":null,\"\"\"sevice\"\"\":null,\"\"\"\$\$index\"\"\":1}\"] ";
				break;
			case "receiver":
				commandString="curl "+context.expand( '${#Project#remoteUrl}' )+"/rest/messagefilters -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -X PUT -d [\"{\"\"\"entityId\"\"\":"+firstEntId+",\"\"\"index\"\"\":0,\"\"\"backendName\"\"\":\"\"\""+firstBck+"\"\"\",\"\"\"routingCriterias\"\"\":[],\"\"\"from\"\"\":null,\"\"\"to\"\"\":null,\"\"\"action\"\"\":null,\"\"\"sevice\"\"\":null,\"\"\"\$\$index\"\"\":0},{\"\"\"entityId\"\"\":"+SecondEntId+",\"\"\"index\"\"\":1,\"\"\"backendName\"\"\":\"\"\""+secondBck+"\"\"\",\"\"\"routingCriterias\"\"\":[],\"\"\"from\"\"\":null,\"\"\"to\"\"\":null,\"\"\"action\"\"\":null,\"\"\"sevice\"\"\":null,\"\"\"\$\$index\"\"\":1}\"] ";
				break;
			case "receivergreen":
				commandString="curl "+context.expand( '${#Project#greenUrl}' )+"/rest/messagefilters -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+XXSRFTOKEN+"\" -X PUT -d [\"{\"\"\"entityId\"\"\":"+firstEntId+",\"\"\"index\"\"\":0,\"\"\"backendName\"\"\":\"\"\""+firstBck+"\"\"\",\"\"\"routingCriterias\"\"\":[],\"\"\"from\"\"\":null,\"\"\"to\"\"\":null,\"\"\"action\"\"\":null,\"\"\"sevice\"\"\":null,\"\"\"\$\$index\"\"\":0},{\"\"\"entityId\"\"\":"+SecondEntId+",\"\"\"index\"\"\":1,\"\"\"backendName\"\"\":\"\"\""+secondBck+"\"\"\",\"\"\"routingCriterias\"\"\":[],\"\"\"from\"\"\":null,\"\"\"to\"\"\":null,\"\"\"action\"\"\":null,\"\"\"sevice\"\"\":null,\"\"\"\$\$index\"\"\":1}\"] ";
				break;
			default:
				assert (false) , "Unknown side."
		}

		//log.info commandString
		if(commandString){
			proc = commandString.execute();
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
		}
		assert(errorCatcher.toString().contains("200 OK")||outputCatcher.toString().contains("successfully")),"Error:setMessageFilter: Error while trying to connect to domibus."
		
		log.info "Message filters update done successfully for Domibus: \""+side+"\".";

	} 
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def fetchCookieHeader(String side,context,log){
		def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def proc=null; def commandString = null; 
		//def JSESSIONID = null; def XSRFTOKEN = null;def XXSRFTOKEN = null;
		
		
		switch(side.toLowerCase()){
			case "sender":
				commandString = "curl "+context.expand( '${#Project#localUrl}' )+"/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\"admin\"\"\",\"\"\"password\"\"\":\"\"\"123456\"\"\"}\" -c "+context.expand( '${projectDir}')+"\\cookie.txt";
				break;
			case "receiver":
				commandString = "curl "+context.expand( '${#Project#remoteUrl}' )+"/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\"admin\"\"\",\"\"\"password\"\"\":\"\"\"123456\"\"\"}\" -c "+context.expand( '${projectDir}')+"\\cookie.txt";
				break;
			case "receivergreen":
				commandString = "curl "+context.expand( '${#Project#greenUrl}' )+"/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\"admin\"\"\",\"\"\"password\"\"\":\"\"\"123456\"\"\"}\" -c "+context.expand( '${projectDir}')+"\\cookie.txt";
				break;
			case "testEnv":
				commandString = "curl "+context.expand( '${#Project#testEnvUrl}' )+"/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\"admin\"\"\",\"\"\"password\"\"\":\"\"\"123456\"\"\"}\" -c "+context.expand( '${projectDir}')+"\\cookie.txt";
				break;
			default:
				assert (false) , "Unknown side."
		}
		
		//log.info commandString;
		if(commandString){
			proc = commandString.execute();
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
		}

		assert(outputCatcher.toString().contains("200 OK")),"Error:Authenticating user: Error while trying to connect to domibus."
		return outputCatcher.toString();
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def computePathRessources(String type,String extension,context){
		def returnPath = null;
		if(type.toLowerCase()=="special"){
			returnPath = (context.expand('${#Project#specialPModesPath}')+extension).replace("\\\\","\\").replace("\\","\\\\");			
		}else{
			returnPath = (context.expand('${#Project#defaultPModesPath}')+extension).replace("\\\\","\\").replace("\\","\\\\");
		}
		return returnPath.toString();
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Ping Gateway
    static def String pingMSH(String side, context, log){
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def proc=null
		def commandString = null;

		switch(side.toLowerCase()){
			case "sender":
				commandString = "curl -s -o /dev/null -w \"%{http_code}\" --noproxy localhost "+context.expand( '${#Project#localUrl}' )+"/services";
				break;
			case "receiver":
				commandString = "curl -s -o /dev/null -w \"%{http_code}\" --noproxy localhost "+context.expand( '${#Project#remoteUrl}' )+"/services";
				break;
			case "receivergreen":
				commandString = "curl -s -o /dev/null -w \"%{http_code}\" --noproxy localhost "+context.expand( '${#Project#greenUrl}' )+"/services";
				break;
			case "testEnv":
				commandString = "curl -s -o /dev/null -w \"%{http_code}\" --noproxy localhost "+context.expand( '${#Project#testEnvUrl}' )+"/services"
				break;
			default:
				assert (false) , "Unknown side."
		}
		
		if(commandString){
			proc = commandString.execute();
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
		}
		return outputCatcher.toString().trim();
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Copy file from source to destination
    static def void copyFile(String source, String destination,log){
		// Check that destination folder exists.
		//def destFolder = new File("${destination}");
		//assert destFolder.exists(), "Error while trying to copy file to folder "+destination+": Destination folder doesn't exist.";
		
		def builder = new AntBuilder();
		try{
			builder.sequential {
				copy(tofile: destination, file:source, overwrite:true)
			}
			log.info "File was successfuly copied."
		}
		catch(Exception ex){
			log.error "Error while trying to copy files: "+ex;
			assert 0;
		}
    }

}