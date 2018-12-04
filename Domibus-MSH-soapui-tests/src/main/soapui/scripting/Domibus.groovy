/**
 * Created by testTeam on 16/09/2016.
 */

import groovy.sql.Sql

import javax.swing.JOptionPane
import java.sql.SQLException

import static javax.swing.JOptionPane.showConfirmDialog
import groovy.util.AntBuilder
import com.eviware.soapui.support.GroovyUtils
import groovy.json.JsonSlurper 
import groovy.json.JsonOutput


class Domibus
{
    def messageExchange=null;
    def context=null;
    def log=null;
	// sleepDelay value is increased from 2000 to 6000 because of pull request take longer ...
    def sleepDelay=6000
    def sqlBlue=null; def sqlC2Dom1=null; def sqlC2Dom2=null; def sqlC2Dom3=null;
    def sqlRed=null; def sqlC3Dom1=null; def sqlC3Dom2=null; def sqlC3Dom3=null;
	def sqlRefrences=[["sqlBlue",null],["sqlRed",null],["sqlGreen",null],["sqlC2Dom1",null],["sqlC2Dom2",null],["sqlC2Dom3",null],["sqlC3Dom1",null],["sqlC3Dom2",null],["sqlC3Dom3",null],["sqlGeneralC2",null],["sqlGeneralC3",null]];
	def sqlGeneralC2=null;
	def sqlGeneralC3=null;
	def sqlGreen=null;
	def thirdGateway = "false"; def multitenancyModeC2=0; def multitenancyModeC3=0;
	static def backup_file_sufix = "_backup_for_soapui_tests";
	static def DEFAULT_LOG_LEVEL = 0
	static def DEFAULT_PASSWORD = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
	static def SUPER_USER="super";
	static def SUPER_USER_PWD="123456";
	static def DEFAULT_ADMIN_USER="admin";
	static def DEFAULT_ADMIN_USER_PWD="123456";
	static def TRUSTSTORE_PASSWORD="test123";
	static def MAX_LOGIN_BEFORE_LOCK = 5;
	static def XSFRTOKEN_C2=null;
	static def XSFRTOKEN_C3=null;
	static def XSFRTOKEN_C_Other=null;
	static def CLEAR_CACHE_COMMAND_TOMCAT = $/rmdir /S /Q ..\work & rmdir /S /Q ..\logs & del /S /Q ..\temp\* & FOR /D %p IN ("..\temp\*.*") DO rmdir /s /q "%p"  & rmdir /S /Q ..\webapps\domibus & rmdir /S /Q ..\conf\domibus\work/$;

    // Short constructor of the Domibus Class
    Domibus(log, messageExchange, context) {
        this.log = log;
        this.messageExchange = messageExchange;
        this.context=context;
		thirdGateway = context.expand( '${#Project#thirdGateway}' );
		multitenancyModeC2=getMultitenancyMode(context.expand( '${#Project#multitenancyModeC2}' ));
		multitenancyModeC3=getMultitenancyMode(context.expand( '${#Project#multitenancyModeC3}' ));
    }

    // Class destructor
    void finalize() {
        log.info "Test finished."
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
   // Log information wrapper 
   static def void debugLog(logMsg, log,  logLevel = DEFAULT_LOG_LEVEL) {
	if (logLevel.toString()=="1" || logLevel.toString() == "true") 
		log.info (logMsg)
  }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  DB Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Connect to a schema
	def connectTo(String database, String driver, String url, String dbUser, String dbPassword){
		debugLog("Open connection to || DB: " + database + " || Url: " + url+ " || Driver: "+ driver+" ||",log);
		def sql = null;

        try{
            switch (database.toLowerCase()) {
			case  "mysql": 
				GroovyUtils.registerJdbcDriver( "com.mysql.jdbc.Driver" )
				sql = Sql.newInstance(url, dbUser, dbPassword, driver)
				break
			case "oracle":
				GroovyUtils.registerJdbcDriver( "oracle.jdbc.driver.OracleDriver" ) 			
				sql = Sql.newInstance(url, dbUser, dbPassword, driver)
				break
			default:
				log.warn "Unknown type of DB"
                sql = Sql.newInstance(url, driver)
			}
			debugLog("Connection opened with success",log);
			return sql;
        }
        catch (SQLException ex){
			log.error "Connection failed";
            assert 0,"SQLException occurred: " + ex;
        }
	}
//---------------------------------------------------------------------------------------------------------------------------------	
    // Open DB connections
    def openConnection(){
		def i=0;
		sqlBlue=connectTo(context.expand( '${#Project#databaseBlue}' ),context.expand('${#Project#driverBlue}'),context.expand('${#Project#jdbcUrlBlue}'),context.expand( '${#Project#blueDbUser}' ),context.expand( '${#Project#blueDbPassword}' ));
		sqlRed=connectTo(context.expand( '${#Project#databaseRed}' ),context.expand('${#Project#driverRed}'),context.expand('${#Project#jdbcUrlRed}'),context.expand( '${#Project#redDbUser}' ),context.expand( '${#Project#redDbPassword}' ));
		sqlRefrences[0][1]=sqlBlue;
		sqlRefrences[1][1]=sqlRed;
		sqlRefrences[2][1]=sqlGreen;
		if(thirdGateway.toLowerCase().trim()=="true"){
			sqlGreen=connectTo(context.expand( '${#Project#databaseGreen}' ),context.expand('${#Project#driverGreen}'),context.expand('${#Project#jdbcUrlGreen}'),context.expand( '${#Project#greenDbUser}' ),context.expand( '${#Project#greenDbPassword}' ));
		}
		while(i<multitenancyModeC2){
			sqlRefrences[i+3][1]=connectTo(context.expand( '${#Project#databaseC2Dom'+(i+1)+'}' ),context.expand('${#Project#driverC2Dom'+(i+1)+'}'),context.expand('${#Project#jdbcUrlC2Dom'+(i+1)+'}'),context.expand( '${#Project#C2Dom'+(i+1)+'DbUser}' ),context.expand( '${#Project#C2Dom'+(i+1)+'DbPassword}' ));
			i++;
		}
		i=0;
		while(i<multitenancyModeC3){
			sqlRefrences[i+6][1]=connectTo(context.expand( '${#Project#databaseC3Dom'+(i+1)+'}' ),context.expand('${#Project#driverC3Dom'+(i+1)+'}'),context.expand('${#Project#jdbcUrlC3Dom'+(i+1)+'}'),context.expand( '${#Project#C3Dom'+(i+1)+'DbUser}' ),context.expand( '${#Project#C3Dom'+(i+1)+'DbPassword}' ));
			i++;
		}
		if(multitenancyModeC2>0){
			sqlRefrences[9][1]=connectTo(context.expand( '${#Project#databaseGeneralC2}' ),context.expand('${#Project#driverGeneralC2}'),context.expand('${#Project#jdbcUrlGeneralC2}'),context.expand( '${#Project#generalDbUserC2}' ),context.expand( '${#Project#generalDbPasswordC2}' ));
		}
		if(multitenancyModeC3>0){
			sqlRefrences[10][1]=connectTo(context.expand( '${#Project#databaseGeneralC3}' ),context.expand('${#Project#driverGeneralC3}'),context.expand('${#Project#jdbcUrlGeneralC3}'),context.expand( '${#Project#generalDbUserC3}' ),context.expand( '${#Project#generalDbPasswordC3}' ));
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------
    // Close the DB connections opened previously
    def closeConnection(){
        debugLog("DB connection would be closed",log)
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
		def i=3;
		while(i<=10){
			if(sqlRefrences[i][1]){
				sqlRefrences[i][1].connection.close();
				sqlRefrences[i][1] = null;	
			}
			i++;
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def executeListOfQueriesOnAllDB(String[] sqlQueriesList) {
	    // Keep for backward compatibilty. To be removed
        executeListOfSqlQueries(sqlQueriesList,"BLUE");
        executeListOfSqlQueries(sqlQueriesList,"RED");
		if(thirdGateway.toLowerCase().trim()=="true"){
			executeListOfSqlQueries(sqlQueriesList,"GREEN");
		}
		
		def i=3;
		while(i<9){
			if(sqlRefrences[i][1]){
				executeListOfSqlQueries(sqlQueriesList,sqlRefrences[i][0]);
			}
			i++;
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def executeListOfQueriesOnBlue(String[] sqlQueriesList) {
        log.info "Executing SQL queries on sender/Blue"
        executeListOfSqlQueries(sqlQueriesList,"BLUE")
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def executeListOfQueriesOnRed(String[] sqlQueriesList) {
        log.info "Executing SQL queries on receiver/Red"
        executeListOfSqlQueries(sqlQueriesList,"RED")
    }
//---------------------------------------------------------------------------------------------------------------------------------	
	def executeListOfQueriesOnGreen(String[] sqlQueriesList) {
        log.info "Executing SQL queries on Third/Green"
        executeListOfSqlQueries(sqlQueriesList,"GREEN")
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def executeListOfSqlQueries(String[] sqlQueriesList, String targetSchema) {
        def connectionOpenedInsideMethod = false
        def sqlDB
        if (!((sqlRed) || (sqlBlue) || (sqlGreen))) {
            debugLog("Method executed without connections open to the DB - try to open connection", log)
            openConnection()
            connectionOpenedInsideMethod = true
        }
		sqlDB=retrieveRefFromSchemaName(targetSchema);

        for (query in sqlQueriesList) {
            debugLog("Executing SQL query: " + query, log)
			try{
				sqlDB.execute query
			}
			catch (SQLException ex){
				closeConnection();
				assert 0,"SQLException occured: " + ex;
			}
        }

        if (connectionOpenedInsideMethod) {
            debugLog("Connection to DB opened during method execution - close opened connection", log)
            closeConnection()
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
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
                "delete from TB_MESSAGING",
                "delete from TB_ERROR",
                "delete from TB_USER_MESSAGE",
                "delete from TB_SIGNAL_MESSAGE",
                "delete from TB_RECEIPT",
                "delete from TB_MESSAGE_INFO",
				"delete from TB_ERROR_LOG",
				"delete from TB_SEND_ATTEMPT",
				"delete from TB_MESSAGE_ACKNW_PROP",
				"delete from TB_MESSAGE_ACKNW",
				"delete from TB_MESSAGING_LOCK",
                "delete from TB_MESSAGE_LOG",
				"delete from TB_MESSAGE_UI"
        ] as String[]

        executeListOfQueriesOnAllDB(sqlQueriesList)

        closeConnection();
		log.info "Cleaning Done.";
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Clean single message identified by messageID starting with provided value
    def cleanDBMessageIDStartsWith(String messageID){
        cleanDBMessageID(messageID, true)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Clean single message identified by ID
    def cleanDBMessageID(String messageID, boolean  messgaeIDStartWithProvidedValue = false){
        log.info "Clean from DB information related to the message with ID: " + messageID
        openConnection()

        def messageIDCheck = "= '${messageID}'" //default comparison method use equal operator
        if (messgaeIDStartWithProvidedValue)
            messageIDCheck = "like '${messageID}%'" //if cleanDBMessageIDStartsWith method was called change method for comparison

        def select_ID_PK = "select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID ${messageIDCheck}" //extracted as common part of queries bellow
        def sqlQueriesList = [
				"delete from TB_RAWENVELOPE_LOG where USERMESSAGE_ID_FK IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
				"delete from TB_RAWENVELOPE_LOG where SIGNALMESSAGE_ID_FK IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + " OR REF_TO_MESSAGE_ID " + messageIDCheck + "))",
				"delete from TB_RECEIPT_DATA where RECEIPT_ID IN (select ID_PK from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
				"delete from TB_PROPERTY where MESSAGEPROPERTIES_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_PROPERTY where PARTPROPERTIES_ID IN (select ID_PK from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
				"delete from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
				"delete from TB_PARTY_ID where FROM_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_PARTY_ID where TO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
				"delete from TB_MESSAGING where (SIGNAL_MESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))) OR (USER_MESSAGE_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
				"delete from TB_ERROR where SIGNALMESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
				"delete from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")",
				"delete from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + " OR REF_TO_MESSAGE_ID " + messageIDCheck + ")",				
                "delete from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_MESSAGE_INFO where MESSAGE_ID " + messageIDCheck + " OR REF_TO_MESSAGE_ID " + messageIDCheck + "",				
				"delete from TB_SEND_ATTEMPT where MESSAGE_ID " + messageIDCheck + "",
				"delete from TB_MESSAGE_ACKNW_PROP where FK_MSG_ACKNOWLEDGE IN (select ID_PK from TB_MESSAGE_ACKNW where MESSAGE_ID " + messageIDCheck + ")",
				"delete from TB_MESSAGE_ACKNW where MESSAGE_ID " + messageIDCheck + "",			
                "delete from TB_MESSAGING_LOCK where MESSAGE_ID " + messageIDCheck + "",				
                "delete from TB_MESSAGE_LOG where MESSAGE_ID " + messageIDCheck + "",
				"delete from TB_MESSAGE_UI where MESSAGE_ID " + messageIDCheck + ""
				] as String[]

        executeListOfQueriesOnAllDB(sqlQueriesList)

        closeConnection()
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Messages Info Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Extract messageID from the request if it exists
    String findGivenMessageID(){
        def messageID = null
        def requestContent = messageExchange.getRequestContentAsXml()
        def requestFile = new XmlSlurper().parseText(requestContent)
        requestFile.depthFirst().each{
            if(it.name()== "MessageId"){
                messageID=it.text().toLowerCase().trim()
            }
        }
        return(messageID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
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
        return(messageID.toLowerCase().trim())
    }
//---------------------------------------------------------------------------------------------------------------------------------
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
		(sqlSender,sqlReceiver)=selectCornersReferences(mapDoms);
		
        // Sender DB
        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')"){
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
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')"){
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
//---------------------------------------------------------------------------------------------------------------------------------
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
		(sqlSender,sqlReceiver)=selectCornersReferences(mapDoms);
		
        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')"){
            total=it.lignes
        }
        assert(total==1),locateTest(context)+"Error:verifyMessageUnicity: Message found "+total+" times in sender side."
        sleep(sleepDelay)
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')"){
            total=it.lignes
        }
        assert(total==1),locateTest(context)+"Error:verifyMessageUnicity: Message found "+total+" times in receiver side."
        closeConnection()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Wait until status or timer expire
    def waitForStatus(String SMSH=null,String RMSH=null,String IDMes=null,String bonusTimeForSender=null,String bonusTimeForReceiver=null, int mapDoms = 3){
		def MAX_WAIT_TIME=80_000; // Maximum time to wait to check the message status. 
		def STEP_WAIT_TIME=1000; // Time to wait before re-checking the message status.	
        def messageID=null;
        def numberAttempts=0;
        def maxNumberAttempts=5;
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
		debugLog("--- mapDoms="+mapDoms,log);
		(sqlSender,sqlReceiver)=selectCornersReferences(mapDoms);
		
        if(SMSH){
            while(((messageStatus!=SMSH)&&(MAX_WAIT_TIME>0))||(wait)){
                sleep(STEP_WAIT_TIME)
                if(MAX_WAIT_TIME>0){
                    MAX_WAIT_TIME=MAX_WAIT_TIME-STEP_WAIT_TIME
                }
                //log.info "maxNumberAttempts: "+maxNumberAttempts+ "- numberAttempts: "+numberAttempts+"-- 
				log.info "WAIT: "+MAX_WAIT_TIME
                sqlSender.eachRow("Select * from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')"){
                    messageStatus=it.MESSAGE_STATUS
                    numberAttempts=it.SEND_ATTEMPTS
                }
                log.info "|MSG_ID: "+messageID+" | SENDER: Expected MSG Status ="+ SMSH +"-- Current MSG Status = "+messageStatus+" | maxNumbAttempts: "+maxNumberAttempts+"-- numbAttempts: "+numberAttempts; 
                if(SMSH=="SEND_FAILURE"){
					if(messageStatus=="WAITING_FOR_RETRY"){
						if(((maxNumberAttempts-numberAttempts)>0)&&(!wait)){
							wait=true
						}
						if((maxNumberAttempts-numberAttempts)<=0){
							wait=false
						}
					}else{
						if(messageStatus==SMSH){
							wait=false;
						}
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
            MAX_WAIT_TIME=100_000
        }
        else
        {
            MAX_WAIT_TIME=30_000
        }
        messageStatus="INIT"
        if(RMSH){
            while((messageStatus!=RMSH)&&(MAX_WAIT_TIME>0)){
                sleep(STEP_WAIT_TIME)
                MAX_WAIT_TIME=MAX_WAIT_TIME-STEP_WAIT_TIME
                sqlReceiver.eachRow("Select * from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')"){
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
//---------------------------------------------------------------------------------------------------------------------------------
	// Check that an entry is created in the table TB_SEND_ATTEMPT
	def checkSendAttempt(String messageID, String targetSchema="BLUE"){
		def MAX_WAIT_TIME=10_000;
		def STEP_WAIT_TIME=1000;
		def sqlSender = null;
		int total = 0;
		openConnection();
		
		sqlSender=retrieveRefFromSchemaName(targetSchema);

		while((MAX_WAIT_TIME>0)&&(total==0)){
			sqlSender.eachRow("Select count(*) lignes from tb_send_attempt where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')"){
				total=it.lignes
			}
			log.info "W: "+MAX_WAIT_TIME;
			sleep(STEP_WAIT_TIME);
			MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME;
		}
		assert(total>0),locateTest(context)+"Error: Message "+messageID+" is not present in the table tb_send_attempt."
		closeConnection();
	}
//---------------------------------------------------------------------------------------------------------------------------------
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
//---------------------------------------------------------------------------------------------------------------------------------
	// Compare payloads order
	static def checkPayloadOrder(submitRequest, log, context, messageExchange){
		def requestAtts = [];
		def responseAtts = [];
		def i = 0;
		//def requestContent = messageExchange.getRequestContentAsXml();
		def requestContent = submitRequest;
		def responseContent = messageExchange.getResponseContentAsXml();;
		assert (requestContent != null),locateTest(context)+"Error: request is empty.";
		assert (responseContent != null),locateTest(context)+"Error: response is empty.";
		def parserFile = new XmlSlurper().parseText(requestContent);
		debugLog("===========================================",log);
		debugLog("Attachments in request: ",log);
		parserFile.depthFirst().each{
            if(it.name()== "PartInfo"){
                requestAtts[i]=it.@href.text();
				debugLog("Attachment: "+requestAtts[i]+" in position "+(i+1)+".",log);
				i++;
            }
        }
		debugLog("===========================================",log);
		debugLog("Attachments in response: ",log);
		i = 0;
		parserFile = new XmlSlurper().parseText(responseContent);
		parserFile.depthFirst().each{
            if(it.name()== "PartInfo"){
                responseAtts[i]=it.@href.text();
				debugLog("Attachment: "+responseAtts[i]+" in position "+(i+1)+".",log);
				i++;
            }
        }
		debugLog("===========================================",log);
		assert (requestAtts.size() == responseAtts.size()),locateTest(context)+"Error: request has "+requestAtts.size()+" attachements wheras response has "+responseAtts.size()+" attachements.";
		for(i=0;i<requestAtts.size();i++){
			assert (requestAtts[i] == responseAtts[i]),locateTest(context)+"Error: in position "+(i+1)+" request has attachment "+requestAtts[i]+" wheras response has attachment "+responseAtts[i]+".";
		}
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  PopUP Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
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
//---------------------------------------------------------------------------------------------------------------------------------
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
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Domibus Administration Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Ping Gateway
    static def String pingMSH(String side, context, log){
		def commandString = null;
		def commandResult = null;
		
		commandString = "curl -s -o /dev/null -w \"%{http_code}\" --noproxy localhost "+urlToDomibus(side, log, context)+"/services";
		commandResult = runCurlCommand(commandString,log); 
		return commandResult[0].trim();
    }
//---------------------------------------------------------------------------------------------------------------------------------
	// Clear domibus cache
    static def clearCache(String side, context, log, String server = "tomcat"){
		log.info "Cleaning cache for domibus "+side+" ...";
		def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
		def proc=null;
		def pathS=context.expand( '${#Project#pathExeSender}' );
        def pathR=context.expand( '${#Project#pathExeReceiver}' );
		def pathRG=context.expand( '${#Project#pathExeGreen}' );
		def commandToRun =null;
		switch(server.toLowerCase()){
			case "tomcat":
				switch(side.toLowerCase()){
					case "sender":
						log.info "PATH = "+pathS;
						commandToRun = "cmd /c cd ${pathS} && "+CLEAR_CACHE_COMMAND_TOMCAT;
						break;
					case "receiver":
						log.info "PATH = "+pathR;
						commandToRun = "cmd /c cd ${pathR} && "+CLEAR_CACHE_COMMAND_TOMCAT;
						break;
					case "receivergreen":
						log.info "PATH = "+pathRG;
						commandToRun = "cmd /c cd ${pathRG} && "+CLEAR_CACHE_COMMAND_TOMCAT;
						break;
					default:
						assert (false) , "Unknown side.";
				}
				break;
			case "weblogic":
				log.info "I don't know how to clean in weblogic yet.";
				break;
			case "wildfly":
				log.info "I don't know how to clean in wildfly yet.";
				break;
			default:
				assert (false) , "Unknown server.";
		}
		if(commandToRun){
			proc = commandToRun.execute();
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
			debugLog("commandToRun = "+commandToRun, log);
			debugLog("outputCatcher = "+outputCatcher, log);
			debugLog("errorCatcher = "+errorCatcher, log);
			log.info "Cleaning should be done."
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------
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
//---------------------------------------------------------------------------------------------------------------------------------
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
//---------------------------------------------------------------------------------------------------------------------------------
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
//---------------------------------------------------------------------------------------------------------------------------------	
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
//---------------------------------------------------------------------------------------------------------------------------------
	static def uploadPmode(String side, String baseFilePath, String extFilePath,context,log, String domainValue="Default", String outcome = "successfully", String message =null, String authUser=null,authPwd=null){
		log.info "Start upload PMode for Domibus \""+side+"\".";
	    def commandString = null;
		def commandResult = null;
		def pmDescription = "SoapUI sample test description for PMode upload";		
		def multitenancyOn=false;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		def String pmodeFile = computePathRessources(baseFilePath,extFilePath,context);
		
		log.info "PMODE FILE PATH: "+pmodeFile;
		
		try{
			multitenancyOn=getMultitenancyFromSide(side,context,log);
			if(multitenancyOn){
				log.info("uploadPmode for Domibus $side and domain $domainValue.");
				debugLog("First, set domain to $domainValue.",log);
				setDomain(side,context,log, domainValue);
				// If authentication details are not fully provided, use default values 
				if((authUser==null)||(authPwd==null)){
					authenticationUser=SUPER_USER;
					authenticationPwd=SUPER_USER_PWD;
				}
			}
			else{
				log.info("uploadPmode for Domibus $side.");
				// If authentication details are not fully provided, use default values
				if((authUser==null)||(authPwd==null)){
					authenticationUser=DEFAULT_ADMIN_USER;
					authenticationPwd=DEFAULT_ADMIN_USER_PWD;
				}
			}
		
			commandString="curl "+urlToDomibus(side, log, context)+"/rest/pmode -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -F \"description="+pmDescription+"\" -F  file=@"+pmodeFile ;
			commandResult = runCurlCommand(commandString,log);
			assert(commandResult[0].contains(outcome)),"Error:uploadPmode: Error while trying to upload the PMode: response doesn't contain the expected outcome \""+outcome+"\"."
			if(outcome.toLowerCase()=="successfully"){
				log.info commandResult[0]+" Domibus: \""+side+"\".";
				if(message!=null){
					assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload done but expected message \""+message+"\" was not returned."
				}
			}
			else{
				log.info "Upload PMode was not done for Domibus: \""+side+"\".";
				if(message!=null){
					assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload was not done but expected message \""+message+"\" was not returned."
				}
			}
		}
		finally{
			resetAuthTokens();
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------	
	static def uploadPmodeWithoutToken(String side, String baseFilePath, String extFilePath,context,log, String outcome = "successfully", String message =null, String userLogin=null,passwordLogin=null){
		log.info "Start upload PMode for Domibus \""+side+"\".";
	    def commandString = null;
		def commandResult = null;
		def pmDescription = "Dummy";
		
		def String output = fetchCookieHeader(side,context,log,userLogin,passwordLogin);		
		def XXSRFTOKEN = null;
		def String pmodeFile = computePathRessources(baseFilePath,extFilePath,context);
		
		commandString="curl " + urlToDomibus(side, log, context) + "/rest/pmode -v -F \"description=" + pmDescription + "\" -F  file=@" + pmodeFile ;
		commandResult = runCurlCommand(commandString,log);
		assert(commandResult[0].contains(outcome)),"Error:uploadPmode: Error while trying to connect to domibus."
		if(outcome.toLowerCase()=="successfully"){
			log.info commandResult[0]+" Domibus: \""+side+"\".";
			if(message!=null){
				assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload done but expected message \""+message+"\" was not returned."
			}
		}
		else{
			log.info "Upload PMode was not done for Domibus: \""+side+"\".";
			if(message!=null){
				assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload was not done but expected message \""+message+"\" was not returned."
			}
		}
	} 
//---------------------------------------------------------------------------------------------------------------------------------
	static def uploadTruststore(String side, String baseFilePath, String extFilePath,context,log, String domainValue="Default",String outcome="successfully",String tsPassword=TRUSTSTORE_PASSWORD, String authUser=null,authPwd=null){
		log.info "Start upload truststore for Domibus \""+side+"\".";
	    def commandString = null;
		def commandResult = null;	
		def multitenancyOn=false;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		def String truststoreFile=null;
		
		try{
			debugLog("Fetch multitenancy mode on domibus $side.",log);
			multitenancyOn=getMultitenancyFromSide(side,context,log);
			debugLog("Multitenancy mode on domibus $side = $multitenancyOn.",log);
			if(multitenancyOn){
				log.info("uploadTruststore for Domibus $side and domain $domainValue.");
				debugLog("First, set domain to $domainValue.",log);
				setDomain(side,context,log, domainValue);
				// If authentication details are not fully provided, use default values 
				if((authUser==null)||(authPwd==null)){
					authenticationUser=SUPER_USER;
					authenticationPwd=SUPER_USER_PWD;
				}
			}
			else{
				log.info("uploadTruststore for Domibus $side.");
				// If authentication details are not fully provided, use default values
				if((authUser==null)||(authPwd==null)){
					authenticationUser=DEFAULT_ADMIN_USER;
					authenticationPwd=DEFAULT_ADMIN_USER_PWD;
				}
			}	
		
			truststoreFile=computePathRessources(baseFilePath,extFilePath,context);				
			commandString="curl "+ urlToDomibus(side, log, context) +"/rest/truststore/save -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -F \"password="+tsPassword+"\" -F  truststore=@"+truststoreFile;
			commandResult = runCurlCommand(commandString,log);
			assert(commandResult[0].contains(outcome)),"Error:uploadTruststore: Error while trying to upload the truststore to domibus."
			log.info commandResult[0]+" Domibus: \""+side+"\".";
		}
		finally{
			resetAuthTokens();
		}
	} 
//---------------------------------------------------------------------------------------------------------------------------------
   // Change Domibus configuration file 
    static def void changeDomibusProperties(color, propValueDict, log, context, testRunner){
		// Check that properties file exist and if yes create backup_file
		// For all properties name and new value pairs change value in file
		// to restore configuration use method restoreDomibusPropertiesFromBackup(domibusPath,  log, context, testRunner)
		def pathToPropertyFile = pathToDomibus(color, log, context) + context.expand('${#Project#subPathToDomibusProperties}')

		// Check file exists
		def testFile = new File(pathToPropertyFile)
		if (!testFile.exists()) {
			testRunner.fail("File [${pathToPropertyFile}] does not exist. Can't change value.")
			return null
			}
		else log.info "File [${pathToPropertyFile}] exists."

		// Create backup file if already not created
		def backupFileName = "${pathToPropertyFile}${backup_file_sufix}"
		def backupFile = new File(backupFileName)
		if (backupFile.exists()) {
			log.info "File [${backupFileName}] already exists and would not be overwrite - old backup file would be preserved."
		}
		else  {		
			copyFile(pathToPropertyFile, backupFileName, log)
			log.info "Backup copy of config file created: [${backupFile}]"
		}
		
		def fileContent = testFile.text
		//run in loop for all properties key values pairs 
		for (item in propValueDict) {
			def propertyToChangeName = item.key
			def newValueToAssign = item.value
			
		 	// Check that property exist in config file
			def found = 0
			def foundInCommentedRow = 0 
			testFile.eachLine{line, n ->
			    n++
			  if (line =~ /^\s*${propertyToChangeName}=/) {
			     log.info "In line $n searched property was found. Line value is: $line"
			     found++
			  }
			  if (line =~ ~/#+\s*${propertyToChangeName}=.*/) {
			     log.info "In line $n commented searched property was found. Line value is: $line"
			     foundInCommentedRow++
			  }
			}
			
			if (found > 1) {
				testRunner.fail("The search string ($propertyToChangeName=) was found ${found} times in file [${pathToPropertyFile}]. Expect only one assigment - check if configuration file is not corrupted.") 
				return null
			}
			// If property is present in file change it value
			if (found) 
				fileContent = fileContent.replaceAll(/(?m)^\s*(${propertyToChangeName}=)(.*)/){ all, paramName, value -> "${paramName}${newValueToAssign}"} 
			else 
				if (foundInCommentedRow)  
					fileContent = fileContent.replaceFirst(/(?m)^#+\s*(${propertyToChangeName}=)(.*)/){ all, paramName, value -> "${paramName}${newValueToAssign}"} 
				else {
					testRunner.fail("The search string ($propertyToChangeName) was not found in file [${pathToPropertyFile}]. No changes would be applied - properties file restored.") 
					return null
				}
		    log.info "In [${pathToPropertyFile}] file property ${propertyToChangeName} was changed to value ${newValueToAssign}"
		 } //loop end 
		
		 // Store new content of properties file after all changes
		  testFile.text=fileContent
		  log.info "Property file [${pathToPropertyFile}] amended" 
    }
//---------------------------------------------------------------------------------------------------------------------------------
   // Restor Domibus configuration file 
    static def void restoreDomibusPropertiesFromBackup(color, log, context, testRunner){
		// Restore from backup file domibus.properties file
		def pathToPropertyFile = pathToDomibus(color, log, context) + context.expand('${#Project#subPathToDomibusProperties}')
		def backupFile = "${pathToPropertyFile}${backup_file_sufix}"
		
		// Check backup file exists
		def backupFileHandler = new File(backupFile)
		if (!backupFileHandler.exists()) {
			testRunner.fail("CRITICAL ERROR: File [${backupFile}] does not exist.")
			return null
		}
		else {	
			log.info "Restore properties file from existing backup"
			copyFile(backupFile, pathToPropertyFile, log)
			if (backupFileHandler.delete()) {
			   log.info "Successufuly restory configuration from backup file and backup file was removed" 
			}
			else {
			   testRunner.fail "Not able to delete configuration backup file" 
			   return null
			}
		}
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Domain Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def getDomainName(String domainInfo,log){
		assert((domainInfo!=null)&&(domainInfo!="")),"Error:getDomainName: provided domain info are empty.";
		debugLog("Get domain name from domain info: $domainInfo.",log);
		def jsonSlurper = new JsonSlurper();
		def domainMap=jsonSlurper.parseText(domainInfo);
		assert(domainMap.name!=null),"Error:getDomain: Domain informations are corrupted: $domainInfo.";
		return domainMap.name;
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def getDomain(String side,context,log, String userLogin=SUPER_USER, String passwordLogin=SUPER_USER_PWD){
		assert(userLogin==SUPER_USER),"Error:getDomains: To manipulate domains, login must be done with user: \"$SUPER_USER\"."
		log.info "Get current domain for Domibus $side.";
	    def commandString = null;		
		def commandResult = null;
		
		commandString="curl "+urlToDomibus(side, log, context)+"/rest/security/user/domain -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,userLogin,passwordLogin) +"\" GET ";
		commandResult = runCurlCommand(commandString,log);	
		assert(commandResult[1].contains("200 OK")),"Error:getDomain: Error while trying to connect to domibus."
		return commandResult[0].substring(5);
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def setDomain(String side,context,log, String domainValue, String userLogin=SUPER_USER, String passwordLogin=SUPER_USER_PWD){
		def commandString = null;		
		def commandResult = null;

		assert(userLogin==SUPER_USER),"Error:getDomains: To manipulate domains, login must be done with user: \"$SUPER_USER\"."
		debugLog("Set domain for Domibus $side.",log);
		if(domainValue == getDomainName(getDomain(side,context,log),log)){
			debugLog("Requested domain is equal to the current value: no action needed",log);
		}
		else{
			commandString="curl "+urlToDomibus(side, log, context)+"/rest/security/user/domain -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,userLogin,passwordLogin) +"\" -X PUT -d \"\"\""+domainValue+"\"\"\"";
			commandResult = runCurlCommand(commandString,log);	
			assert(commandResult[1].contains("200 OK")||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:setDomain: Error while trying to set the domain: verify that domain $domainValue is correctly configured."
			debugLog("Domain set to $domainValue.",log);
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------
	// Return number of domains
	static def getMultitenancyMode(String inputValue){
		if((inputValue==null)||(inputValue=="")){
			return 0;
		}
		if (inputValue.trim().isInteger()) {
			return (inputValue as Integer);
		}
		else{
			return 0;
		}
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Users Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def getAdminConsoleUsers(String side,context,log, String authUser=null, String authPwd=null){
		debugLog("Get users for Domibus $side.",log);
	    def commandString = null;		
		def commandResult = null;
		def multitenancyOn=false;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		
		multitenancyOn=getMultitenancyFromSide(side,context,log);
		if(multitenancyOn){
			// If authentication details are not fully provided, use default values 
			if((authUser==null)||(authPwd==null)){
				authenticationUser=SUPER_USER;
				authenticationPwd=SUPER_USER_PWD;
			}
		}
		else{
			// If authentication details are not fully provided, use default values
			if((authUser==null)||(authPwd==null)){
				authenticationUser=DEFAULT_ADMIN_USER;
				authenticationPwd=DEFAULT_ADMIN_USER_PWD;
			}
		}
		
		commandString="curl "+urlToDomibus(side, log, context)+"/rest/user/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET ";
		commandResult = runCurlCommand(commandString,log);	
		assert(commandResult[1].contains("200 OK")||commandResult[1].contains("successfully")),"Error:getAdminConsoleUsers: Error while trying to connect to domibus.";
		return commandResult[0].substring(5);
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def addAdminConsoleUser(String side,context,log, String domainValue="Default", String userRole="ROLE_ADMIN", String userAC, String passwordAC="Domibus-123", String authUser=null, String authPwd=null){
		def usersMap=null;
		def mapElement=null;
		def multitenancyOn=false;
		def commandString = null;		
		def commandResult = null;
		def jsonSlurper = new JsonSlurper();
		def curlParams=null;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		
		try{
			multitenancyOn=getMultitenancyFromSide(side,context,log);
			if(multitenancyOn){
				log.info("Add admin console user for Domibus $side and domain $domainValue.");
				debugLog("First, set domain to $domainValue.",log);
				setDomain(side,context,log, domainValue);
				// If authentication details are not fully provided, use default values 
				if((authUser==null)||(authPwd==null)){
					authenticationUser=SUPER_USER;
					authenticationPwd=SUPER_USER_PWD;
				}
			}
			else{
				log.info("Add admin console user for Domibus $side.");
				// If authentication details are not fully provided, use default values
				if((authUser==null)||(authPwd==null)){
					authenticationUser=DEFAULT_ADMIN_USER;
					authenticationPwd=DEFAULT_ADMIN_USER_PWD;
				}
			}
			debugLog("Fetch users list and verify that user $userAC doesn't already exist.",log);		
			usersMap=jsonSlurper.parseText(getAdminConsoleUsers(side,context,log));
			if(userExists(usersMap,userAC,log)){
				log.error "Error:addAdminConsoleUser: admin console user $userAC already exist: usernames must be unique.";
			}
			else{
				//assert(!userExists(usersMap,userAC,log)),"Error:addAdminConsoleUser: admin console user $userAC already exist: usernames must be unique.";
				debugLog("Users list before the update: "+usersMap,log);
				debugLog("Prepare user $userAC details to be added.",log);
				mapElement=[password:passwordAC, roles:userRole, domain:usersMap[0].domain, active:true, userName:userAC, email:null, authorities:[userRole], suspended:false, status:'NEW']
				debugLog("Update users list.",log);
				usersMap<<mapElement;
				debugLog("Users list after the update: "+usersMap,log);
				debugLog("Insert users list.",log);
				curlParams=JsonOutput.toJson(usersMap).toString();
				commandString="curl "+urlToDomibus(side, log, context)+"/rest/user/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X PUT -d "+formatJsonForCurl(curlParams,log);
				commandResult = runCurlCommand(commandString,log);	
				assert(commandResult[1].contains("200 OK")),"Error:addAdminConsoleUser: Error while trying to add user $userAC.";
				log.info "User $userAC added."
			}
		}
		finally{
			resetAuthTokens();
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def removeAdminConsoleUser(String side,context,log, String domainValue="Default", String userAC, String authUser=null, String authPwd=null){
		def usersMap=null;
		def multitenancyOn=false;
		def commandString = null;		
		def commandResult = null;
		def jsonSlurper = new JsonSlurper();
		def curlParams=null;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		def indexUser=-1;
		def i=0;
		
		try{
			multitenancyOn=getMultitenancyFromSide(side,context,log);
			if(multitenancyOn){
				log.info("Remove admin console user for Domibus $side and domain $domainValue.");
				debugLog("First, set domain to $domainValue.",log);
				setDomain(side,context,log, domainValue);
				// If authentication details are not fully provided, use default values 
				if((authUser==null)||(authPwd==null)){
					authenticationUser=SUPER_USER;
					authenticationPwd=SUPER_USER_PWD;
				}
			}
			else{
				log.info("Remove admin console user for Domibus $side.");
				// If authentication details are not fully provided, use default values
				if((authUser==null)||(authPwd==null)){
					authenticationUser=DEFAULT_ADMIN_USER;
					authenticationPwd=DEFAULT_ADMIN_USER_PWD;
				}
			}
			debugLog("Fetch users list and verify that user $userAC exists.",log);		
			usersMap=jsonSlurper.parseText(getAdminConsoleUsers(side,context,log));
			if(!userExists(usersMap,userAC,log)){
				log.info "Admin console user $userAC doesn't exist. No action needed.";
			}
			else{
				while((i<usersMap.size())&&(indexUser<0)){
					assert(usersMap[i]!=null),"Error:removeAdminConsoleUser: Error while parsing the list of admin console users.";
					if(usersMap[i].userName==userAC){
						indexUser=i;
					}
					i++;
				}
				assert(indexUser>=0),"Error:removeAdminConsoleUser: Error while fetching the index of user $userAC.";
				usersMap.remove(indexUser);
				assert(!userExists(usersMap,userAC,log)),"Error:removeAdminConsoleUser: admin console user $userAC is still in the list.";
				curlParams=JsonOutput.toJson(usersMap).toString();
				commandString="curl "+urlToDomibus(side, log, context)+"/rest/user/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X PUT -d "+formatJsonForCurl(curlParams,log);
				commandResult = runCurlCommand(commandString,log);	
				assert(commandResult[1].contains("200 OK")),"Error:removeAdminConsoleUser: Error while trying to remove user $userAC.";
				log.info "User $userAC Removed."
			}
		}
		finally{
			resetAuthTokens();
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def getPluginUsers(String side,context,log, String authUser=null, String authPwd=null){
		debugLog("Get users for Domibus $side.",log);
	    def commandString = null;		
		def commandResult = null;
		def multitenancyOn=false;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		
		multitenancyOn=getMultitenancyFromSide(side,context,log);
		if(multitenancyOn){
			// If authentication details are not fully provided, use default values 
			if((authUser==null)||(authPwd==null)){
				authenticationUser=SUPER_USER;
				authenticationPwd=SUPER_USER_PWD;
			}
		}
		else{
			// If authentication details are not fully provided, use default values
			if((authUser==null)||(authPwd==null)){
				authenticationUser=DEFAULT_ADMIN_USER;
				authenticationPwd=DEFAULT_ADMIN_USER_PWD;
			}
		}
		
		commandString="curl "+urlToDomibus(side, log, context)+"/rest/plugin/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET ";
		commandResult = runCurlCommand(commandString,log);	
		assert(commandResult[1].contains("200 OK")||commandResult[1].contains("successfully")),"Error:getPluginUsers: Error while trying to connect to domibus.";
		return commandResult[0].substring(5);
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def addPluginUser(String side,context,log, String domainValue="Default", String userRole="ROLE_ADMIN", String userPl, String passwordPl="Domibus-123", String originalUser="urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", String authUser=null, String authPwd=null){
		def usersMap=null;
		def mapElement=null;
		def multitenancyOn=false;
		def commandString = null;		
		def commandResult = null;
		def jsonSlurper = new JsonSlurper();
		def curlParams=null;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		
		try{
			multitenancyOn=getMultitenancyFromSide(side,context,log);
			if(multitenancyOn){
				log.info("Add plugin user for Domibus $side and domain $domainValue.");
				debugLog("First, set domain to $domainValue.",log);
				setDomain(side,context,log, domainValue);
				// If authentication details are not fully provided, use default values 
				if((authUser==null)||(authPwd==null)){
					authenticationUser=SUPER_USER;
					authenticationPwd=SUPER_USER_PWD;
				}
			}
			else{
				log.info("Add plugin user for Domibus $side.");
				// If authentication details are not fully provided, use default values
				if((authUser==null)||(authPwd==null)){
					authenticationUser=DEFAULT_ADMIN_USER;
					authenticationPwd=DEFAULT_ADMIN_USER_PWD;
				}
			}	
			debugLog("Fetch users list and verify that user $userPl doesn't already exist.",log);		
			usersMap=jsonSlurper.parseText(getPluginUsers(side,context,log));
			if(userExists(usersMap,userPl,log,true)){
				log.error "Error:addPluginUser: plugin user $userPl already exist: usernames must be unique.";
			}
			else{
				//assert(!userExists(usersMap,userPl,log,true)),"Error:addPluginUser: plugin user $userPl already exist: usernames must be unique.";
				debugLog("Users list before the update: "+usersMap,log);
				debugLog("Prepare user $userPl details to be added.",log);
				curlParams="[\n  {\n    \"status\": \"NEW\",\n    \"username\": \"$userPl\",\n    \"authenticationType\": \"BASIC\",\n    \"originalUser\": \"$originalUser\",\n    \"authRoles\": \"$userRole\",\n    \"passwd\": \"$passwordPl\"\n  }\n]";
				debugLog("Inser user in list.",log);
				commandString="curl "+urlToDomibus(side, log, context)+"/rest/plugin/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X PUT -d "+formatJsonForCurl(curlParams,log);
				commandResult = runCurlCommand(commandString,log);	
				assert(commandResult[1].contains("200 OK")||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:addPluginUser: Error while trying to add a user.";
				log.info "Plugin user $userPl added.";
			}
		}
		finally{
			resetAuthTokens();
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def removePluginUser(String side,context,log, String domainValue="Default", String userRole="ROLE_ADMIN", String userPl, String passwordPl="Domibus-123", String originalUser="urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", String authUser=null, String authPwd=null){
		def usersMap=null;
		def mapElement=null;
		def multitenancyOn=false;
		def commandString = null;		
		def commandResult = null;
		def jsonSlurper = new JsonSlurper();
		def curlParams=null;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		
		try{
			multitenancyOn=getMultitenancyFromSide(side,context,log);
			if(multitenancyOn){
				log.info("Remove plugin user for Domibus $side and domain $domainValue.");
				debugLog("First, set domain to $domainValue.",log);
				setDomain(side,context,log, domainValue);
				// If authentication details are not fully provided, use default values 
				if((authUser==null)||(authPwd==null)){
					authenticationUser=SUPER_USER;
					authenticationPwd=SUPER_USER_PWD;
				}
			}
			else{
				log.info("Add admin console user for Domibus $side.");
				// If authentication details are not fully provided, use default values
				if((authUser==null)||(authPwd==null)){
					authenticationUser=DEFAULT_ADMIN_USER;
					authenticationPwd=DEFAULT_ADMIN_USER_PWD;
				}
			}
			debugLog("Fetch users list and verify that user $userPl exists.",log);		
			usersMap=jsonSlurper.parseText(getPluginUsers(side,context,log));
			debugLog(usersMap,log);
			if(!userExists(usersMap,userPl,log,true)){
				log.info "Plugin user $userPl doesn't exist. No action needed.";
			}
			else{
				curlParams="[\n  {\n    \"status\": \"REMOVED\",\n    \"username\": \"$userPl\",\n    \"authenticationType\": \"BASIC\",\n    \"originalUser\": \"$originalUser\",\n    \"authRoles\": \"$userRole\",\n    \"passwd\": \"$passwordPl\"\n  }\n]";
				commandString="curl "+urlToDomibus(side, log, context)+"/rest/plugin/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X PUT -d "+formatJsonForCurl(curlParams,log);
				commandResult = runCurlCommand(commandString,log);	
				assert(commandResult[1].contains("200 OK")||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:removePluginUser: Error while trying to remove user $userPl.";
				log.info "Plugin user $userPl removed.";
			}
		}
		finally{
			resetAuthTokens();
		}
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def userExists(usersMap,String targetedUser,log,boolean plugin=false){
		def i=0;
		def userFound=false;
		
		if(plugin){
			assert(usersMap.entries!=null),"Error:userExists: Error while parsing the list of plugin users.";
			while((i<usersMap.entries.size())&&(userFound==false)){
				assert(usersMap.entries[i]!=null),"Error:userExists: Error while parsing the list of plugin users.";
				debugLog("- Iteration $i: comparing --$targetedUser--and--"+usersMap.entries[i].username+"--.",log);
				if(usersMap.entries[i].username==targetedUser){
					userFound=true;
				}
				i++;
			}
		}
		else{
			assert(usersMap!=null),"Error:userExists: Error while parsing the list of admin console users.";
			while((i<usersMap.size())&&(userFound==false)){
				assert(usersMap[i]!=null),"Error:userExists: Error while parsing the list of admin console users.";
				if(usersMap[i].userName==targetedUser){
					userFound=true;
				}
				i++;
			}
		}
		
		return userFound;
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def resetAuthTokens(){
		XSFRTOKEN_C2=null;
		XSFRTOKEN_C3=null;
		XSFRTOKEN_C_Other=null;
	}
//---------------------------------------------------------------------------------------------------------------------------------
   //!!! To be updated/deleted with new functions to add users that uses curl only !!!
   // Add user 
    def void addUser(color,username,idUser=12345,password='$2a$10$HApapHvDStTEwjjneMCvxuqUKVyycXZRfXMwjU0rRmaWMsjWQp/Zu'){
		debugLog("Trying to add user (" +username+","+password+")", log);
		if(searchUser(color,username)!=0){
			log.info "User \""+username+"\" already exists. If you want to insert it again, please use method deleteUser to delete it."
		}
		else{
			def sqlQueriesList = ["INSERT INTO TB_USER (ID_PK, USER_NAME, USER_PASSWORD, USER_ENABLED) VALUES ('"+idUser+"','"+username+"','"+password+"', 1)","INSERT INTO TB_USER_ROLES (USER_ID, ROLE_ID) VALUES ("+idUser+", '2')"] as String[];
			executeListOfSqlQueries(sqlQueriesList,color);
			log.info "User \""+username+"\" created."
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
   //!!! To be updated/deleted with new functions to add users that uses curl only !!!
   // Delete user 
    def void deleteUser(color,username){
		debugLog("Trying to delete user " +username, log);
		def idUser=searchUser(color,username);
		if(idUser==0){
			log.info "User \""+username+"\" doesn't exist. Nothing to do !"
		}
		else{	
			def sqlQueriesList = ["DELETE FROM TB_USER_ROLES WHERE USER_ID = "+idUser,"DELETE FROM TB_USER WHERE (ID_PK = "+idUser+")"] as String[];
			executeListOfSqlQueries(sqlQueriesList,color);
			log.info "User \""+username+"\" deleted." 
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
	//!!! To be updated/deleted with new functions to add users that uses curl only !!!
	// search for user 
    def searchUser(color,username){
		debugLog("Searching for user \"" +username+"\" in the DB", log);
		def idFound=null;
		def sql=null;
		// Connect to database
		openConnection();
		switch(color.toLowerCase()){
			case "blue":
				sql=sqlBlue;
				break;
			case "red":
				sql=sqlRed;
				break;
			case "green":
				sql=sqlGreen;
				break;
			default:
				assert (false) , "Unknown side color. Supported values: BLUE, RED, GREEN"
		}
		try{
			sql.eachRow ("select ID_PK FROM TB_USER WHERE (USER_NAME = ${username})"){
					idFound = it.ID_PK;
			}
		}
		catch (SQLException ex){
			closeConnection();
			assert 0,"SQLException occured during searching: " + ex;
		}
		// Close DB connection
		closeConnection();
		if(!idFound){
			return 0;
		}
		else{
			return idFound; 
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
	//!!! To be updated/deleted with new functions to add users that uses curl only !!!
	// Insert wrong password 
    def insertWrongPassword(side,color,username,password){
		debugLog("Insert wrong password for user " +username, log);
		if(searchUser(color,username)==0){
			log.info "User "+username+" doesn't exist. Please insert it first. You can use method addUser."
		}
		else{
			def commandResult = null;
			def commandString = null; 
			commandString = "curl "+urlToDomibus(side, log, context)+"/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\""+username+"\"\"\",\"\"\"password\"\"\":\"\"\""+password+"\"\"\"}\" -c "+context.expand( '${projectDir}')+"\\cookie.txt";
			commandResult = runCurlCommand(commandString,log);
			assert((commandResult[0].contains("Bad credentials"))||(commandResult[0].contains("Suspended"))),"Error:Authenticating user: Error while trying to connect to domibus."
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
	//!!! To be updated/deleted with new functions to add users that uses curl only !!!
	// lock user  
    def lockUser(side,color,username,password){
		debugLog("Trying to lock user " +username, log);
		if(searchUser(color,username)==0){
			log.info "User "+username+" doesn't exist. Please insert it first. You can use method addUser."
		}
		else{
			def commandResult = null;
			def commandString = null;
			commandString = "curl "+urlToDomibus(side, log, context)+"/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\""+username+"\"\"\",\"\"\"password\"\"\":\"\"\""+password+"\"\"\"}\" -c "+context.expand( '${projectDir}')+"\\cookie.txt";
			for(def i=0;i<=MAX_LOGIN_BEFORE_LOCK;i++){
				commandResult = runCurlCommand(commandString,log);
			}
			assert(commandResult[0].contains("Suspended")),"Error:blocking user: Error while trying to block the user."
			log.info "User \""+username+"\" should be locked."
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
	// !!! To be removed !!!
    // Add quote when value is not null - for sql queries or return String "null"
    static def String addQuotationWhenNotNull(value) {
		if (value != null) 
			return "'" + value +"'"
		
		return "null"
	}
	
//---------------------------------------------------------------------------------------------------------------------------------
	// !!! To be removed !!!
    // Add authentication record if it's not present already in table
    def addAuthenticationRecord(targetSchema, userName, authRoles, originaUser, passwd = DEFAULT_PASSWORD){
        log.info "Add authentication record, if exist delete first, to: ${targetSchema} with user name: ${userName} and original user value: ${originaUser}"

        def sqlQuery = [
				"delete from TB_AUTHENTICATION_ENTRY where username in ('" + userName + "')",
                "INSERT INTO tb_authentication_entry (USERNAME, PASSWD, AUTH_ROLES, ORIGINAL_USER) values (" + addQuotationWhenNotNull(userName) +", "+ 
				addQuotationWhenNotNull(passwd) +", " + addQuotationWhenNotNull(authRoles) + ", " + addQuotationWhenNotNull(originaUser) +")" ] as String[]

        executeListOfSqlQueries(sqlQuery, targetSchema)
    }
//---------------------------------------------------------------------------------------------------------------------------------
	// !!! To be removed !!!
    // Remove authentication Record from TB_AUTHENTICATION_ENTRY table
    def delAuthenticationRecord(targetSchema, userNameList){
		userNameList = [] + userNameList
        log.info "Remove authentication record from TB_AUTHENTICATION_ENTRY table for user(s) name: ${userNameList}"

        def sqlQuery = ["delete from TB_AUTHENTICATION_ENTRY where username in ('" + userNameList.join("','") + "')" ] as String[]

        executeListOfSqlQueries(sqlQuery, targetSchema)
    }

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Message filter Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def getMessageFilters(String side,context,log, String authUser=null,authPwd=null){
		log.info "Get message filters for Domibus \""+side+"\".";
	    def commandString = null;		
		def commandResult = null;
		def multitenancyOn=false;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		
		multitenancyOn=getMultitenancyFromSide(side,context,log);
		if(multitenancyOn){
			// If authentication details are not fully provided, use default values 
			if((authUser==null)||(authPwd==null)){
				authenticationUser=SUPER_USER;
				authenticationPwd=SUPER_USER_PWD;
			}
		}
		else{
			// If authentication details are not fully provided, use default values
			if((authUser==null)||(authPwd==null)){
				authenticationUser=DEFAULT_ADMIN_USER;
				authenticationPwd=DEFAULT_ADMIN_USER_PWD;
			}
		}
		
		commandString="curl "+urlToDomibus(side, log, context)+"/rest/messagefilters -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET ";
		commandResult = runCurlCommand(commandString,log);	
		assert(commandResult[1].contains("200 OK")||commandResult[1].contains("successfully")),"Error:getMessageFilter: Error while trying to connect to domibus."
		return commandResult[0].substring(5);
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def formatFilters(filtersMap,String filterChoice,context,log, String extraCriteria=null){
		log.info "Analysing backends filters order ..."
		def swapBck = null;
		def i=0;
		assert(filtersMap!=null),"Error:formatFilters: Not able to get the backend details.";
		debugLog("FILTERS:" + filtersMap, log);
		
		// Single backend: no action needed
		if(filtersMap.messageFilterEntries.size()==1){
			return "ok";
		}
		debugLog("Loop over :" +filtersMap.messageFilterEntries.size()+" backend filters.", log);
		while(i<filtersMap.messageFilterEntries.size()){
			assert(filtersMap.messageFilterEntries[i]!=null),"Error:formatFilters: Error while parsing filter details.";
			if(filtersMap.messageFilterEntries[i].backendName.toLowerCase()==filterChoice.toLowerCase()){
				debugLog("Comparing --"+filtersMap.messageFilterEntries[i].backendName+"-- and --"+filterChoice+"--", log);
				if((extraCriteria==null)||((extraCriteria!=null)&&filtersMap.messageFilterEntries[i].toString().contains(extraCriteria))){
					if(i==0){
						return "correct";
					}
					debugLog("switch $i element", log);
					swapBck=filtersMap.messageFilterEntries[0];
					filtersMap.messageFilterEntries[0]=filtersMap.messageFilterEntries[i];
					filtersMap.messageFilterEntries[i]=swapBck;
					return filtersMap;
				}
			}
			i++;
		}		
		return "ko";
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def setMessageFilters(String side, String filterChoice,context,log,domainValue="Default",String extraCriteria=null,String authUser=null,authPwd=null){
		log.info "Start setting message filters for Domibus \""+side+"\".";
		def String output=null;
	    def commandString=null;
		def commandResult=null;
		def curlParams=null;
		def filtersMap=null;
		def multitenancyOn=false;
		def authenticationUser=authUser;
		def authenticationPwd=authPwd;
		def jsonSlurper=new JsonSlurper();
		
		try{
			multitenancyOn=getMultitenancyFromSide(side,context,log);
			if(multitenancyOn){
				log.info("setMessageFilters for Domibus $side and domain $domainValue.");
				debugLog("First, set domain to $domainValue.",log);
				setDomain(side,context,log, domainValue);
				// If authentication details are not fully provided, use default values 
				if((authUser==null)||(authPwd==null)){
					authenticationUser=SUPER_USER;
					authenticationPwd=SUPER_USER_PWD;
				}
			}
			else{
				log.info("Add admin console user for Domibus $side.");
				// If authentication details are not fully provided, use default values
				if((authUser==null)||(authPwd==null)){
					authenticationUser=DEFAULT_ADMIN_USER;
					authenticationPwd=DEFAULT_ADMIN_USER_PWD;
				}
			}
		
			filtersMap=jsonSlurper.parseText(getMessageFilters(side,context,log));
			debugLog("filtersMap:" + filtersMap, log);
			assert(filtersMap!=null),"Error:setMessageFilter: Not able to get the backend details.";
			assert(filtersMap.toString().toLowerCase().contains(filterChoice.toLowerCase())),"Error:setMessageFilter: The backend you want to set is not installed.";
			filtersMap=formatFilters(filtersMap,filterChoice,context,log,extraCriteria);
			assert(filtersMap!="ko"),"Error:setMessageFilter: The backend you want to set is not installed."
			debugLog("Backend filters order analyse done.", log);
			if(filtersMap.equals("ok")){
				log.info "Only one backend installed: Nothing to do.";
			}
			else{
				if(filtersMap.equals("correct")){
					log.info "The requested backend is already selected: Nothing to do.";
				}
				else{
					curlParams=JsonOutput.toJson(filtersMap).toString();
					commandString="curl "+urlToDomibus(side, log, context)+"/rest/messagefilters -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X PUT -d "+formatJsonForCurl(curlParams,log);
					commandResult = runCurlCommand(commandString,log);
					assert(commandResult[1].contains("200 OK")||commandResult[1].contains("successfully")),"Error:setMessageFilter: Error while trying to connect to domibus.";
					log.info "Message filters update done successfully for Domibus: \""+side+"\".";
				}
			}
		}
		finally{
			resetAuthTokens();
		}
	} 
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Curl related Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static def fetchCookieHeader(String side,context,log,String userLogin=SUPER_USER,passwordLogin=SUPER_USER_PWD){
		def commandString = null;
		def commandResult = null;
		
		commandString = "curl "+urlToDomibus(side, log, context)+"/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\"$userLogin\"\"\",\"\"\"password\"\"\":\"\"\"$passwordLogin\"\"\"}\" -c "+context.expand( '${projectDir}')+"\\cookie.txt";
		commandResult = runCurlCommand(commandString,log);
		assert(commandResult[0].contains("200 OK")),"Error:Authenticating user: Error while trying to connect to domibus."
		return commandResult[0];
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def String returnXsfrToken(String side,context,log,String userLogin=SUPER_USER,passwordLogin=SUPER_USER_PWD){
		debugLog("Call returnXsfrToken with values: --side=$side--XSFRTOKEN_C2=$XSFRTOKEN_C2--XSFRTOKEN_C3=$XSFRTOKEN_C3.",log);
		def String output=null;

		switch(side.toLowerCase()){
			case "c2":
			case "blue":
			case "sender":
				if(XSFRTOKEN_C2==null){
					output = fetchCookieHeader(side,context,log,userLogin,passwordLogin);
					XSFRTOKEN_C2=output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=","").replace(";","");
				}
				return XSFRTOKEN_C2;
				break;
			case "c3":
			case "red":
			case "receiver":
				if(XSFRTOKEN_C3==null){
					output = fetchCookieHeader(side,context,log,userLogin,passwordLogin);
					XSFRTOKEN_C3=output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=","").replace(";","");
				}
				return XSFRTOKEN_C3;
				break;
			case "receivergreen":
				if(XSFRTOKEN_C_Other==null){
					output = fetchCookieHeader(side,context,log,userLogin,passwordLogin);
					XSFRTOKEN_C_Other=output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=","").replace(";","");
				}
				return XSFRTOKEN_C_Other;
				break;
			default:
				assert (false) , "returnXsfrToken: Unknown side. Supported values: sender, receiver, receivergreen ...";
		}		
		assert (false) , "returnXsfrToken: Error while retrieving XSFRTOKEN ..."
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def formatJsonForCurl(String input,log){
		def intermediate=null;
		assert(input!=null),"Error:formatJsonForCurl: input string is null.";
		assert(input.contains("[")&&input.contains("]")),"Error:formatJsonForCurl: input string is corrupted is null.";
		intermediate=input.substring(input.indexOf("[")+1,input.lastIndexOf("]")).replace("\"","\"\"\"");
		return "[\""+intermediate+"\"]";
	}
//---------------------------------------------------------------------------------------------------------------------------------
	static def computePathRessources(String type,String extension,context){
		def returnPath = null;
		if(type.toLowerCase()=="special"){
			returnPath = (context.expand('${#Project#specialPModesPath}')+extension).replace("\\\\","\\").replace("\\","\\\\");			
		}else{
			returnPath = (context.expand('${#Project#defaultPModesPath}')+extension).replace("\\\\","\\").replace("\\","\\\\");
		}
		return returnPath.toString();
	}
//---------------------------------------------------------------------------------------------------------------------------------	
	// Run curl command
	static def runCurlCommand(String inputCommand,log){
		def proc=null;
		def outputCatcher = new StringBuffer();
		def errorCatcher = new StringBuffer();
		debugLog("Run curl command: "+inputCommand,log);
		if(inputCommand){
			proc = inputCommand.execute();
			if(proc!=null){
				proc.consumeProcessOutput(outputCatcher, errorCatcher)
				proc.waitFor()
			}
		}
		debugLog("outputCatcher: "+outputCatcher.toString(),log);
		debugLog("errorCatcher: "+errorCatcher.toString(),log);
		return([outputCatcher.toString(),errorCatcher.toString()]);
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Multitenancy Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	// Return multitenancy mode
	static def getMultitenancyFromSide(String side,context,log){
		def mode=0;
		switch(side.toUpperCase()){
			case "C2":
			case "BLUE":
			case "SENDER":
				//mode=multitenancyModeC2;
				mode=getMultitenancyMode(context.expand( '${#Project#multitenancyModeC2}' ));
				debugLog("mode on domibus $side set to $mode.",log);
				break;
			case "C3":
			case "RED":
			case "RECEIVER":
				//mode=multitenancyModeC3;
				mode=getMultitenancyMode(context.expand( '${#Project#multitenancyModeC3}' ));
				debugLog("mode on domibus $side set to $mode.",log);
				break;
			default:
				log.error "ERROR:getMultitenancyFromSide: dominus $side not found.";
		}
		if(mode>0){
			return true;
		}
		else{
			return false;
		}
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Utilities Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Returns: "--TestCase--testStep--"  
    static def String locateTest(context){
        return("--"+context.testCase.name+"--"+context.testCase.getTestStepAt(context.getCurrentStepIndex()).getLabel()+"--  ");
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Copy file from source to destination
    static def void copyFile(String source, String destination, log, overwriteOpt=true){
		// Check that destination folder exists.
		//def destFolder = new File("${destination}");
		//assert destFolder.exists(), "Error while trying to copy file to folder "+destination+": Destination folder doesn't exist.";
		
		def builder = new AntBuilder();
		try{
			builder.sequential {
				copy(tofile: destination, file:source, overwrite:overwriteOpt)
			}
			log.info "File ${source} was successfuly copied to ${destination}"
		}
		catch(Exception ex){
			log.error "Error while trying to copy files: "+ex;
			assert 0;
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // replace slashes in project custom properties values
    static def String formatPathSlashes(String source){
		if((source!=null)&&(source!="")){
			return source.replaceAll("/","\\\\");
		}
    }
//---------------------------------------------------------------------------------------------------------------------------------
// Return path to domibus folder
static def String pathToDomibus(color, log, context){
	// Return path to domibus folder base on the "color"
		def propName = ""
		switch(color.toLowerCase()){
			case "blue":
				propName =  "pathBlue"
				break;
			case "red":
				propName = "pathRed"
				break;
			case "green":
				propName  = "pathGreen"
				break;
			default:
				assert (false) , "Unknown side color. Supported values: BLUE, RED, GREEN"
		}

		return context.expand("\${#Project#${propName}}")
}
//---------------------------------------------------------------------------------------------------------------------------------
   // Select C2 and C3 DB references  
    def selectCornersReferences(int selector){
		debugLog("--- SelectCornersReferences, selector="+selector,log);
		def sqlSender=null; def sqlReceiver=null;
		// Currently, we support only 3 domains on each corner: to be improved later in case more are needed
		// For Backward compatibilty: to be removed afterwords
		switch(selector){
			case 3:
				debugLog("sqlSender = sqlBlue; sqlReceiver = sqlRed",log);
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
			case 5:
				debugLog("sqlSender = sqlBlue; sqlReceiver = sqlGreen",log);
				sqlSender = sqlBlue; sqlReceiver = sqlGreen;
				break;
			case 6:
				debugLog("sqlSender = sqlRed; sqlReceiver = sqlGreen",log);
				sqlSender = sqlRed; sqlReceiver = sqlGreen;
				break;
			default:
				debugLog("sqlSender = sqlBlue; sqlReceiver = sqlRed",log);
				sqlSender = sqlBlue; sqlReceiver = sqlRed;
				break;
		}
		if((selector>10)&&(selector<100)){
			debugLog("sqlSender: "+sqlRefrences[selector.intdiv(10)][0],log);
			sqlSender=sqlRefrences[selector.intdiv(10)][1];
			debugLog("sqlReceiver: "+sqlRefrences[selector % 10][0],log);
			sqlReceiver=sqlRefrences[selector % 10][1];
		}
		assert((sqlSender!=null)&&(sqlReceiver!=null)),"Error: Selecting sql references: Null values found (sqlSender=$sqlSender,sqlReceiver=$sqlReceiver). Verify that you have already opened the correct DB connections.";
		return[sqlSender,sqlReceiver]
    }
//---------------------------------------------------------------------------------------------------------------------------------
   // Retrieve sql reference from schema name  
    def retrieveRefFromSchemaName(String inputName){
		def i=0;
		def sqlDB=null;
		// For Backward compatibilty: to be removed afterwords
		switch (inputName.toUpperCase()) {
			case "C3":
            case "RED":
                sqlDB = this.sqlRed
                break
			case "C2":
            case "BLUE":
                sqlDB = this.sqlBlue
                break
			case "GREEN":
				assert(thirdGateway.toLowerCase().trim()=="true"), "\"GREEN\" schema is not actif. Please set soapui project custom property \"thirdGateway\" to true."
                sqlDB = this.sqlGreen
                break
            default:
                //log.error "Not supported schema type: " + targetSchema
                break
        }
		while(i<=10){
		 if(sqlRefrences[i][0].toUpperCase()==inputName.toUpperCase()){
			sqlDB=sqlRefrences[i][1];
			i=10;
		 }
		 i++;
		}
		assert(sqlDB!=null),"Error: Selecting sql reference for $inputName : Null value found. Verify that you have already opened the correct DB connections.";
		return(sqlDB);
    }
//---------------------------------------------------------------------------------------------------------------------------------
	// Return url to specific domibus
	static def String urlToDomibus(side, log, context){
		// Return url to specific domibus base on the "side"
		def propName = ""
		switch(side.toLowerCase()){
			case "c2":
			case "blue":
			case "sender":
				propName =  "localUrl"
				break;
			case "c3":
			case "red":
			case "receiver":
				propName = "remoteUrl"
				break;
			case "receivergreen":
				propName  = "greenUrl"
				break;
			case "testEnv":
				propName  = "testEnvUrl"
				break;
			default:
				assert (false) , "Unknown side. Supported values: sender, receiver, receivergreen and testEnv"
		}

		return context.expand("\${#Project#${propName}}")
	}
//---------------------------------------------------------------------------------------------------------------------------------
	// !!! To be removed !!!
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
}