/**
 * Created by ghouiah on 16/09/2016.
 */

import groovy.sql.Sql

import javax.swing.JOptionPane
import java.sql.SQLException

import static javax.swing.JOptionPane.showConfirmDialog

class Domibus
{
    def messageExchange=null
    def context=null
    def log=null
    def sleepDelay=2000
    def sqlBlue
    def sqlRed


    // Short constructor of the Domibus Class
    Domibus(log, messageExchange, context) {
        this.log = log
        this.messageExchange = messageExchange
        this.context=context
    }

    // Class destructor
    void finalize() {
        log.info "Test finished."
    }

    // Simply open DB connection
    def openConnection(){
        log.debug "Open DB connections"

        def databaseBlue=context.expand( '${#Project#databaseBlue}' )
        def driverBlue=context.expand('${#Project#driverBlue}')
        def urlBlue=context.expand('${#Project#jdbcUrlBlue}')
        def blueDbUser=context.expand( '${#Project#blueDbUser}' )
        def blueDbPassword=context.expand( '${#Project#blueDbPassword}' )

        def databaseRed=context.expand( '${#Project#databaseRed}' )
        def driverRed=context.expand('${#Project#driverRed}')
        def urlRed=context.expand('${#Project#jdbcUrlRed}')
        def redDbUser=context.expand( '${#Project#redDbUser}' )
        def redDbPassword=context.expand( '${#Project#redDbPassword}' )

        log.info("Open connection to DB: " + databaseBlue + " Url: " + urlBlue)

        try{
            if (databaseBlue.toLowerCase() == "mysql" || databaseBlue.toLowerCase() == "oracle" )
                sqlBlue = Sql.newInstance(urlBlue, blueDbUser, blueDbPassword, driverBlue)
            else
                sqlBlue = Sql.newInstance(urlBlue, driverBlue)
        }
        catch (SQLException ex){
            assert 0,"SQLException occurred: " + ex
        }


        log.info("Open connection to DB: " + databaseRed + " Url: " + urlRed)

        try{
            if (databaseRed.toLowerCase() == "mysql" || databaseRed.toLowerCase() == "oracle" )
                sqlRed = Sql.newInstance(urlRed, redDbUser, redDbPassword, driverRed)
            else
                sqlRed = Sql.newInstance(urlRed, driverRed)
        }
        catch (SQLException ex){
            assert 0,"SQLException occured: " + ex
        }

    }

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
        assert (messageID != null),"Error:findReturnedMessageID: The message ID is not found in the response"
        if((findGivenMessageID()!=null) && (findGivenMessageID().trim()!="")){
            //if(findGivenMessageID()!=null){
            assert (messageID.toLowerCase() == findGivenMessageID().toLowerCase()),"Error:findReturnedMessageID: The message ID returned is ("+messageID+"), the message ID provided is ("+findGivenMessageID()+")."
        }
        return(messageID.toLowerCase())
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Verification of message existence
    def verifyMessagePresence(int presence1,int presence2, String IDMes=null){
        def messageID=null
        sleep(sleepDelay)
        if(IDMes!=null){
            messageID=IDMes
        }
        else{
            messageID=findReturnedMessageID()
        }
        def total=0
        openConnection()

        // Sender DB
        sqlBlue.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        if(presence1==1){
            //log.info "total = "+total
            assert(total>0),"Error:verifyMessagePresence: Message with ID "+messageID+" is not found in sender side."
        }
        if(presence1==0){
            assert(total==0),"Error:verifyMessagePresence: Message with ID "+messageID+" is found in sender side."
        }

        // Receiver DB
        total=0
        sleep(sleepDelay)
        sqlRed.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        if(presence2==1){
            assert(total>0),"Error:verifyMessagePresence: Message with ID "+messageID+" is not found in receiver side."
        }
        if(presence2==0){
            assert(total==0),"Error:verifyMessagePresence: Message with ID "+messageID+" is found in receiver side."
        }

        closeConnection()
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Verification of message unicity
    def verifyMessageUnicity(String IDMes=null){
        sleep(sleepDelay)
        def messageID
        def total=0
        if(IDMes!=null){
            messageID=IDMes
        }
        else{
            messageID=findReturnedMessageID()
        }
        openConnection()
        sqlBlue.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        assert(total==1),"Error:verifyMessageUnicity: Message found "+total+" times in sender side."
        sleep(sleepDelay)
        sqlBlue.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes
        }
        assert(total==1),"Error:verifyMessageUnicity: Message found "+total+" times in receiver side."
        closeConnection()
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Wait until status or timer expire
    def waitForStatus(String SMSH=null,String RMSH=null,String IDMes=null,String bonusTimeForSender=null,String bonusTimeForReceiver=null){
        def messageID=null
        def waitMax=10_000
        def numberAttempts=0
        def maxNumberAttempts=4
        def interval=1000
        def messageStatus="INIT"
        def wait=false
        log.info "waitForStatus params: messageID: " + messageID + " RMSH: " + RMSH + " IDMes: " + IDMes + " bonusTimeForSender: " + bonusTimeForSender + " bonusTimeForReceiver: " + bonusTimeForReceiver
        if(IDMes!=null){
            messageID=IDMes
        }
        else{
            messageID=findReturnedMessageID()
        }

        if(bonusTimeForSender){
            log.info "Waiting time for Sender extended to 500 seconds"
            waitMax=500_000
        }
        openConnection()
        if(SMSH){
            while(((messageStatus!=SMSH)&&(waitMax>0))||(wait)){
                sleep(interval)
                if(waitMax>0){
                    waitMax=waitMax-interval
                }
                log.info "maxNumberAttempts-numberAttempts: "+maxNumberAttempts+"-"+numberAttempts
                log.info "WAIT: "+waitMax
                sqlBlue.eachRow("Select * from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
                    messageStatus=it.MESSAGE_STATUS
                    numberAttempts=it.SEND_ATTEMPTS
                }
                log.info "messageStatus="+messageStatus + " SMSH="+ SMSH
                if((SMSH=="SEND_FAILURE")&&(messageStatus=="WAITING_FOR_RETRY")){
                    if(((maxNumberAttempts-numberAttempts)>0)&&(!wait)){
                        wait=true
                    }
                    if((maxNumberAttempts-numberAttempts)<=0){
                        wait=false
                    }
                }
            }
            log.info "finished checking sender, messageStatus: " + messageStatus + " waitMax: " + waitMax

            assert(messageStatus!="INIT"),"Error:waitForStatus: Message "+messageID+" is not present in the sender side."
            assert(messageStatus.toLowerCase()==SMSH.toLowerCase()),"Error:waitForStatus: Message in the sender side has status "+messageStatus+" instead of "+SMSH+"."
        }
        if (bonusTimeForReceiver)
        {
            log.info "Waiting time for Receiver extended to 500 seconds"
            waitMax=500_000
        }
        else
        {
            waitMax=10_000
        }
        messageStatus="INIT"
        if(RMSH){
            while((messageStatus!=RMSH)&&(waitMax>0)){
                sleep(interval)
                waitMax=waitMax-interval
                sqlRed.eachRow("Select * from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
                    messageStatus=it.MESSAGE_STATUS
                }
                log.info "W:" + waitMax + " M:" + messageStatus
            }
            log.info "finished checking receiver, messageStatus: " + messageStatus + " waitMax: " + waitMax
            assert(messageStatus!="INIT"),"Error:waitForStatus: Message "+messageID+" is not present in the receiver side."
            assert(messageStatus.toLowerCase()==RMSH.toLowerCase()),"Error:waitForStatus: Message in the receiver side has status "+messageStatus+" instead of "+RMSH+"."
        }
        closeConnection()
    }

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII

    def executeListOfQueriesOnAllDB(String[] sqlQueriesList) {
        executeListOfSqlQueries(sqlQueriesList,"BLUE")
        executeListOfSqlQueries(sqlQueriesList,"RED")
    }

    def executeListOfQueriesOnBlue(String[] sqlQueriesList) {
        log.info "Executing SQL queries on sender/Blue"
        executeListOfSqlQueries(sqlQueriesList,"BLUE")
    }

    def executeListOfQueriesOnRed(String[] sqlQueriesList) {
        log.info "Executing SQL queries on receiver/Red"
        executeListOfSqlQueries(sqlQueriesList,"RED")
    }

    def executeListOfSqlQueries(String[] sqlQueriesList, String targetSchema) {
        def connectionOpenedInsideMethod = false
        def sqlDB
        if (!((sqlRed) && (sqlBlue))) {
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
            default:
                log.error "Not supported schema type: " + targetSchema
                return
        }

        for (query in sqlQueriesList) {
            log.info "Executing SQL query: " + query
            sqlDB.execute query
        }

        if (connectionOpenedInsideMethod) {
            log.debug "Connection to DB opened during method execution - close opened connection"
            closeConnection()
        }
    }

    // Clean all the messages from the DB
    def cleanDatabaseAll(){
        log.info "Clean all message related information from DB"
        openConnection()


        def sqlQueriesList = [
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
    }

    // Clean single message identified by messageID starting with provided value
    def cleanDBMessageIDStartsWith(String messageID){
        cleanDBMessageID(messageID, true)
    }

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

    static def getStatusRetriveStatus(log, context, messageExchange) {
        def outStatus=null
        def responseContent = messageExchange.getResponseContentAsXml()
        def requestFile = new XmlSlurper().parseText(responseContent)
        requestFile.depthFirst().each{
            if(it.name()== "getMessageStatusResponse"){
                outStatus=it.text()
            }
        }
        assert (outStatus != null),"Error:getStatusRetriveStatus: Not able to return status from response message"
        return outStatus
    }


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
    // Start Gateway
    static def startMSH(String side, context, log){
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def pathS=context.expand( '${#Project#pathExeSender}' )
        def pathR=context.expand( '${#Project#pathExeReceiver}' )
        def proc=null

        log.info "Trying to start the " + side.toUpperCase()

        if(side.toLowerCase()=="sender"){
            proc="cmd /c cd ${pathS} && startup.bat".execute()
        }
        else
        {
            if(side.toLowerCase()=="receiver")
            {
                proc="cmd /c cd ${pathR} && startup.bat".execute()
            }
            else
            {
                assert (false) , "Incorrect side"
            }
        }
        if(proc!=null){
            proc.consumeProcessOutput(outputCatcher, errorCatcher)
            proc.waitFor()
        }
        assert((!errorCatcher)&&(proc!=null)), "Error:startMSH: Error while trying to start the MSH."

        sleep(65_000)
        log.info "--- DONE - " + side.toUpperCase() + " started ---"

    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Stop Gateway
    static def stopMSH(String side, context, log){
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def proc=null
        def pathS=context.expand( '${#Project#pathExeSender}' )
        def pathR=context.expand( '${#Project#pathExeReceiver}' )

        log.info "Trying to start the " + side.toUpperCase()

        if(side.toLowerCase()=="sender"){
            proc="cmd /c cd ${pathS} && shutdown.bat".execute()
        }
        else
        {
            if(side.toLowerCase()=="receiver")
            {
                proc="cmd /c cd ${pathR} && shutdown.bat".execute()
            }
            else
            {
                assert (false) , "Incorrect side"
            }
        }
        if(proc!=null){
            proc.consumeProcessOutput(outputCatcher, errorCatcher)
            proc.waitFor()
        }
        assert((!errorCatcher)&&(proc!=null)),"Error:startMSH: Error while trying to stop the MSH."
        sleep(8000)
        log.info "--- DONE - " + side.toUpperCase() + " stopped ---"
    }


}