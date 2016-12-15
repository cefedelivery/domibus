/**
 * Created by ghouiah on 16/09/2016.
 */

package soapui.Domibus;

import groovy.sql.Sql;
import java.sql.SQLException;



class Domibus
{
    def messageExchange;
    def context
    def log;
    def urlBlue=null;
    def urlRed=null;
    def driver=null
    def sqlBlue=null;
    def sqlRed=null;
    def sleepDelay=2000;
    def database=null;
    def user=null;
    def password=null;


    // Constructor of the Domibus Class
    Domibus(log,messageExchange,context,urlBlue,urlRed,driver,database,user,password) {
        this.log = log
        this.messageExchange = messageExchange;
        this.context=context;
        this.urlBlue=urlBlue;
        this.urlRed=urlRed;
        this.driver=driver;
        this.database=context.expand( database );
        this.user=context.expand( user );
        this.password=context.expand( password );
    }

    // Class destructor
    void finalize() {
        log.info "Test finished."
    }

    // Simply open DB connection
    def openConnection(){
        log.info(database)
        if(database.toLowerCase()=="mysql"){
            try{
                sqlBlue = Sql.newInstance(context.expand( urlBlue ), user, password, context.expand( driver ))
                sqlRed = Sql.newInstance(context.expand( urlRed ), user, password, context.expand( driver ))
            }
            catch (SQLException ex){
                assert 0,"SQLException occured: " + ex;
            }
        }
        else{
            try{
                sqlBlue = Sql.newInstance(context.expand( urlBlue ), context.expand( driver ))
                sqlRed = Sql.newInstance(context.expand( urlRed ), context.expand( driver ))
            }
            catch (SQLException ex)
            {
                assert 0,"SQLException occured: " + ex;
            }
        }
    }

    def demo(){
        sqlBlue.eachRow("Select * from TB_PAYLOAD"){
            log.info(it.ID_PK);
        }
    }

    // Close the DB connection opened previously
    def closeConnection(){
        if(sqlBlue){
            sqlBlue.connection.close();
            sqlBlue = null;
        }
        if(sqlRed){
            sqlRed.connection.close();
            sqlRed = null;
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Extract messageID from the request if it exists
    def String findGivenMessageID(){
        def messageID=null;
        def requestContent = messageExchange.getRequestContentAsXml() ;
        def requestFile = new XmlSlurper().parseText(requestContent);
        def allNodes = requestFile.depthFirst().each{
            if(it.name()== "MessageId"){
                messageID=it.text().toLowerCase();
            }
        }
        return(messageID);
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Extract messageID from the response
    def String findReturnedMessageID(){
        def messageID=null;
        def responseContent = messageExchange.getResponseContentAsXml() ;
        def responseFile = new XmlSlurper().parseText(responseContent);
        def allNodes = responseFile.depthFirst().each{
            if(it.name()== "messageID"){
                messageID=it.text();
            }
        }
        assert (messageID != null),"Error:findReturnedMessageID: The message ID is not found in the response";
        if((findGivenMessageID()!=null) && (findGivenMessageID().trim()!="")){
            //if(findGivenMessageID()!=null){
            assert (messageID.toLowerCase() == findGivenMessageID().toLowerCase()),"Error:findReturnedMessageID: The message ID returned is ("+messageID+"), the message ID provided is ("+findGivenMessageID()+").";
        }
        return(messageID.toLowerCase());
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Verification of message existence
    def verifyMessagePresence(int presence1,int presence2, String IDMes=null){
        def messageID=null;
        sleep(sleepDelay);
        if(IDMes!=null){
            messageID=IDMes;
        }
        else{
            messageID=findReturnedMessageID();
        }
        def total=0;
        openConnection();

        // Sender DB
        sqlBlue.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes;
        }
        if(presence1==1){
            //log.info "total = "+total
            assert(total>0),"Error:verifyMessagePresence: Message with ID "+messageID+" is not found in sender side.";
        }
        if(presence1==0){
            assert(total==0),"Error:verifyMessagePresence: Message with ID "+messageID+" is found in sender side.";
        }

        // Receiver DB
        total=0;
        sleep(sleepDelay);
        sqlRed.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes;
        }
        if(presence2==1){
            assert(total>0),"Error:verifyMessagePresence: Message with ID "+messageID+" is not found in receiver side.";
        }
        if(presence2==0){
            assert(total==0),"Error:verifyMessagePresence: Message with ID "+messageID+" is found in receiver side.";
        }

        closeConnection();
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Verification of message unicity
    def verifyMessageUnicity(String IDMes=null){
        sleep(sleepDelay);
        def messageID=null;
        def total=0;
        if(IDMes!=null){
            messageID=IDMes;
        }
        else{
            messageID=findReturnedMessageID();
        }
        openConnection();
        sqlBlue.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes;
        }
        assert(total==1),"Error:verifyMessageUnicity: Message found "+total+" times in sender side.";
        sleep(sleepDelay);
        sqlBlue.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = LOWER(${messageID})"){
            total=it.lignes;
        }
        assert(total==1),"Error:verifyMessageUnicity: Message found "+total+" times in receiver side.";
        closeConnection();
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Wait until status or timer expire
    def waitForStatus(String SMSH=null,String RMSH=null,String IDMes=null,String bonusTime=null){
        def messageID=null;
        def waitMax=10000;
        def numberAttempts=0;
        def maxNumberAttempts=4;
        def interval=1000;
        def messageStatus="INIT";
        def wait=false;

        if(IDMes!=null){
            messageID=IDMes;
        }
        else{
            messageID=findReturnedMessageID();
        }
		
		if(bonusTime){
            waitMax=500000;
        }
        openConnection();
        if(SMSH){
            while(((messageStatus!=SMSH)&&(waitMax>0))||(wait)){
                sleep(interval);
                if(waitMax>0){
                    waitMax=waitMax-interval;
                }
				log.info "maxNumberAttempts-numberAttempts: "+maxNumberAttempts+"-"+numberAttempts;
                log.info "WAIT: "+waitMax;
                sqlBlue.eachRow("Select * from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = ${messageID}"){
                    messageStatus=it.MESSAGE_STATUS;
                    numberAttempts=it.SEND_ATTEMPTS;
                }
                if((SMSH=="SEND_FAILURE")&&(messageStatus=="WAITING_FOR_RETRY")){
                    if(((maxNumberAttempts-numberAttempts)>0)&&(wait==false)){
                        wait=true;
                    }
                    if((maxNumberAttempts-numberAttempts)<=0){
                        wait=false;
                    }
                }
            }
            assert(messageStatus!="INIT"),"Error:waitForStatus: Message "+messageID+" is not present in the sender side.";
            assert(messageStatus.toLowerCase()==SMSH.toLowerCase()),"Error:waitForStatus: Message in the sender side has status "+messageStatus+" instead of "+SMSH+".";
        }
        waitMax=10000;
        messageStatus="INIT";
        if(RMSH){
            while((messageStatus!=RMSH)&&(waitMax>0)){
                sleep(interval);
                waitMax=waitMax-interval;
                sqlRed.eachRow("Select * from TB_MESSAGE_LOG where LOWER(MESSAGE_ID) = ${messageID}"){
                    messageStatus=it.MESSAGE_STATUS;
                }
            }
            assert(messageStatus!="INIT"),"Error:waitForStatus: Message "+messageID+" is not present in the receiver side.";
            assert(messageStatus.toLowerCase()==RMSH.toLowerCase()),"Error:waitForStatus: Message in the receiver side has status "+messageStatus+" instead of "+RMSH+".";
        }
        closeConnection();
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Start Gateway
    def startMSH(String side){
        def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def pathS=context.expand( '${#Project#pathExeSender}' );
        def pathR=context.expand( '${#Project#pathExeReceiver}' );
        def proc=null;
        if(side=="sender"){
            proc="cmd /c cd ${pathS} && startup.bat".execute();
        }
        else{
            proc="cmd /c cd ${pathR} && startup.bat".execute();
        }
        if(proc!=null){
            proc.consumeProcessOutput(outputCatcher, errorCatcher);
            proc.waitFor();
        }
        assert((!errorCatcher)&&(proc!=null)),"Error:startMSH: Error while trying to start the MSH.";
        sleep(35000);
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Stop Gateway
    def stopMSH(String side){
        def outputCatcher = new StringBuffer();
        def errorCatcher = new StringBuffer();
        def proc=null;
        def pathS=context.expand( '${#Project#pathExeSender}' );
        def pathR=context.expand( '${#Project#pathExeReceiver}' );
        if(side=="sender"){
            proc="cmd /c cd ${pathS} && shutdown.bat".execute();
        }
        else{
            proc="cmd /c cd ${pathR} && shutdown.bat".execute();
        }
        if(proc!=null){
            proc.consumeProcessOutput(outputCatcher, errorCatcher);
            proc.waitFor();
        }
        assert((!errorCatcher)&&(proc!=null)),"Error:startMSH: Error while trying to stop the MSH.";
        sleep(5000);
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Clean all the messages from the DB
    def cleanDatabaseAll(){
        openConnection();
		sqlBlue.execute "delete from TB_RECEIPT_DATA";	
        sqlBlue.execute "delete from TB_PROPERTY";
        sqlBlue.execute "delete from TB_PART_INFO";
        sqlBlue.execute "delete from TB_PARTY_ID";
        //sqlBlue.execute "delete from TB_PARTY_ID";
        sqlBlue.execute "delete from TB_MESSAGING";
        sqlBlue.execute "delete from TB_ERROR";
        sqlBlue.execute "delete from TB_USER_MESSAGE";
        sqlBlue.execute "delete from TB_SIGNAL_MESSAGE";
		sqlBlue.execute "delete from TB_RECEIPT";
        sqlBlue.execute "delete from TB_MESSAGE_INFO";
        sqlBlue.execute "delete from TB_MESSAGE_LOG";
		
        sqlRed.execute "delete from TB_RECEIPT_DATA";
        sqlRed.execute "delete from TB_PROPERTY";
        sqlRed.execute "delete from TB_PART_INFO";
        sqlRed.execute "delete from TB_PARTY_ID";
        //sqlRed.execute "delete from TB_PARTY_ID";
        sqlRed.execute "delete from TB_MESSAGING";
        sqlRed.execute "delete from TB_ERROR";
        sqlRed.execute "delete from TB_USER_MESSAGE";
        sqlRed.execute "delete from TB_SIGNAL_MESSAGE";
		sqlRed.execute "delete from TB_RECEIPT";
        sqlRed.execute "delete from TB_MESSAGE_INFO";
        sqlRed.execute "delete from TB_MESSAGE_LOG";
        closeConnection();
    }

    // Clean single message identified by ID
    def cleanDBMessageID(String messageID){
        openConnection();
        sqlBlue.execute "delete from TB_RECEIPT_DATA where RECEIPT_ID IN (select ID_PK from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})))";
        sqlBlue.execute "delete from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlBlue.execute "delete from TB_PROPERTY where MESSAGEPROPERTIES_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlBlue.execute "delete from TB_PROPERTY where PARTPROPERTIES_ID IN (select ID_PK from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})))";
        sqlBlue.execute "delete from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlBlue.execute "delete from TB_PARTY_ID where FROM_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlBlue.execute "delete from TB_PARTY_ID where TO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlBlue.execute "delete from TB_MESSAGING where (SIGNAL_MESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))) OR (USER_MESSAGE_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})))";
        sqlBlue.execute "delete from TB_ERROR where SIGNALMESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlBlue.execute "delete from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})";
        sqlBlue.execute "delete from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where ((MESSAGE_ID = ${messageID}) OR (REF_TO_MESSAGE_ID = ${messageID})))";
        sqlBlue.execute "delete from TB_MESSAGE_INFO where ((MESSAGE_ID = ${messageID}) OR (REF_TO_MESSAGE_ID = ${messageID}))";
        sqlBlue.execute "delete from TB_MESSAGE_LOG where MESSAGE_ID = ${messageID}";
		
        sqlRed.execute "delete from TB_RECEIPT_DATA where RECEIPT_ID IN (select ID_PK from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_message where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})))";
        sqlRed.execute "delete from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlRed.execute "delete from TB_PROPERTY where MESSAGEPROPERTIES_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlRed.execute "delete from TB_PROPERTY where PARTPROPERTIES_ID IN (select ID_PK from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})))";
        sqlRed.execute "delete from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlRed.execute "delete from TB_PARTY_ID where FROM_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlRed.execute "delete from TB_PARTY_ID where TO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlRed.execute "delete from TB_MESSAGING where (SIGNAL_MESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))) OR (USER_MESSAGE_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})))";
        sqlRed.execute "delete from TB_ERROR where SIGNALMESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID}))";
        sqlRed.execute "delete from TB_USER_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID = ${messageID})";
        sqlRed.execute "delete from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where ((MESSAGE_ID = ${messageID}) OR (REF_TO_MESSAGE_ID = ${messageID})))";
        sqlRed.execute "delete from TB_MESSAGE_INFO where ((MESSAGE_ID = ${messageID}) OR (REF_TO_MESSAGE_ID = ${messageID}))";
        sqlRed.execute "delete from TB_MESSAGE_LOG where MESSAGE_ID = ${messageID}";
        closeConnection();
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
}

