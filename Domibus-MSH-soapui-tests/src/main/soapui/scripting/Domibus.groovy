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


class Domibus{
    def messageExchange = null;
    def context = null;
    def log = null;

    def allDomainsProperties = null
    def allDomains = null

    // sleepDelay value is increased from 2000 to 6000 because of pull request take longer ...
    def sleepDelay = 6000

    def dbConnections = [:]
    def blueDomainID = null //"C2Default"
    def redDomainID = null //"C3Default"
    def greenDomainID = null //"thirdDefault"
    def thirdGateway = "false"; def multitenancyModeC2 = 0; def multitenancyModeC3 = 0;
    static def backup_file_sufix = "_backup_for_soapui_tests";
    static def DEFAULT_LOG_LEVEL = 1;
    static def DEFAULT_PASSWORD = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92";
    static def SUPER_USER="super";
    static def SUPER_USER_PWD="Domibus-123";
    static def DEFAULT_ADMIN_USER="admin";
    static def DEFAULT_ADMIN_USER_PWD="Domibus-123";
    static def TRUSTSTORE_PASSWORD="test123";
    static def MAX_LOGIN_BEFORE_LOCK = 5;
    static def XSFRTOKEN_C2=null;
    static def XSFRTOKEN_C3=null;
    static def XSFRTOKEN_C_Other=null;
    static def CLEAR_CACHE_COMMAND_TOMCAT = $/rmdir /S /Q ..\work & rmdir /S /Q ..\logs & del /S /Q ..\temp\* & FOR /D %p IN ("..\temp\*.*") DO rmdir /s /q "%p"  & rmdir /S /Q ..\webapps\domibus & rmdir /S /Q ..\conf\domibus\work/$;

        // Short constructor of the Domibus Class
        Domibus(log, messageExchange, context) {
        this.log = log
        this.messageExchange = messageExchange
        this.context = context
        this.allDomainsProperties = parseDomainProperties(context.expand('${#Project#allDomainsProperties}'))
        this.thirdGateway = context.expand('${#Project#thirdGateway}')
        this.blueDomainID = context.expand('${#Project#defaultBlueDomainID}')
        this.redDomainID = context.expand('${#Project#defaultRedDomainId}')
        this.greenDomainID = context.expand('${#Project#defaultGreenDomainID}')

/* Still not added as previous values was used in static context 
            this.SUPER_USER = context.expand('${#Project#superAdminUsername}')
        this.SUPER_USER_PWD = context.expand('${#Project#superAdminPassword}')
        this.DEFAULT_ADMIN_USER = context.expand('${#Project#defaultAdminUsername}')
        this.DEFAULT_ADMIN_USER_PWD = context.expand('${#Project#defaultAdminPassword}')
*/
        this.multitenancyModeC2 = getMultitenancyMode(context.expand('${#Project#multitenancyModeC2}'), log)
        this.multitenancyModeC3 = getMultitenancyMode(context.expand('${#Project#multitenancyModeC3}'), log)
    }

        // Class destructor
        void finalize() {
        closeAllDbConnections()
        log.info "Domibus class not needed longer."
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Log information wrapper
    static def void debugLog(logMsg, log,  logLevel = DEFAULT_LOG_LEVEL) {
        if (logLevel.toString() == "1" || logLevel.toString() == "true") log.info(logMsg)
    }

//---------------------------------------------------------------------------------------------------------------------------------
// Parse domain properties
        def parseDomainProperties(allDomainsPropertiesString) {
        debugLog("  ====  Calling \"returnDBproperties\".", log)
        debugLog("  parseDomainProperties  [][]  Parse properties for connection.", log)
        debugLog("  parseDomainProperties  [][]  All domain custom properties before parsing $allDomainsPropertiesString.", log)
        def mandatoryProperties = ["site", "domainName", "domNo", "dbType", "dbDriver", "dbJdbcUrl", "dbUser", "dbPassword"]

        def jsonSlurper = new JsonSlurper()
        def domPropMap = jsonSlurper.parseText(allDomainsPropertiesString)
        assert domPropMap != null
        // it’s possible that the response wasn’t in proper JSON format and is deserialized as empty
        assert!domPropMap.isEmpty()

        debugLog("  parseDomainProperties  [][]  Mandatory logs are: ${mandatoryProperties}.", log)

        domPropMap.each { domain, properties ->
            debugLog("  parseDomainProperties  [][]  Check mandatory properties are not null for domain ID: ${domain}", log)
            mandatoryProperties.each { propertyName ->
                assert(properties[propertyName] != null),"Error:returnDBproperties: \"${propertyName}\" property couldn't be retrieved for domain ID \"$domain\"." }
        }
        debugLog("  parseDomainProperties  [][]  DONE.", log)
        return domPropMap
    }

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  DB Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
        // Connect to a schema
        def connectTo(String database, String driver, String url, String dbUser, String dbPassword) {
        debugLog("  ====  Calling \"connectTo\".", log)
        debugLog("  connectTo  [][]  Open connection to || DB: " + database + " || Url: " + url + " || Driver: " + driver + " ||", log)
        def sql = null;

        try {
            switch (database.toLowerCase()) {
            case  "mysql":
                GroovyUtils.registerJdbcDriver("com.mysql.jdbc.Driver")
                sql = Sql.newInstance(url, dbUser, dbPassword, driver)
                break
            case "oracle":
                GroovyUtils.registerJdbcDriver("oracle.jdbc.driver.OracleDriver")
                sql = Sql.newInstance(url, dbUser, dbPassword, driver)
                break
            default:
                log.warn "Unknown type of DB"
                sql = Sql.newInstance(url, driver)
            }
            debugLog("  connectTo  [][]  Connection opened with success", log)
            return sql;
        } catch (SQLException ex) {
            log.error "  connectTo  [][]  Connection failed";
            assert 0,"SQLException occurred: " + ex;
        }
    }



//---------------------------------------------------------------------------------------------------------------------------------
        // Open all DB connections
        def openAllDbConnections() {
        debugLog("  ====  Calling \"openAllDbConnections\".", log)
        log.warn allDomainsProperties.keySet().toString()
        openDbConnections(allDomainsProperties.keySet())
    }

//---------------------------------------------------------------------------------------------------------------------------------
        // Open DB connections for provided list of domain defined by domain IDs
        def openDbConnections(domainIdList) {
        debugLog("  ====  Calling \"openDbConnections\" ${domainIdList}.", log)

        domainIdList.each { domainName ->
            def domain = retrieveDomainId(domainName)
            if (!dbConnections.containsKey(domain)) {
                debugLog("  openConnection  [][]  Open DB connection for domain ID: ${domain}", log)
                this.dbConnections[domain] = connectTo(allDomainsProperties[domain].dbType,
                                                       allDomainsProperties[domain].dbDriver,
                                                       allDomainsProperties[domain].dbJdbcUrl,
                                                       allDomainsProperties[domain].dbUser,
                                                       allDomainsProperties[domain].dbPassword)
            } else debugLog("  openConnection  [][]  DB connection for domain ID: ${domain} already open.", log)
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
        // Close all DB connections opened previously
        def closeAllDbConnections() {
        debugLog("  ====  Calling \"closeAllDbConnections\".", log)
        closeDbConnections(allDomainsProperties.keySet())
    }

//---------------------------------------------------------------------------------------------------------------------------------
        // Close all DB connections opened previously
        def closeDbConnections(domainIdList) {
        debugLog("  ====  Calling \"closeConnection\".", log)

        for (domainName in domainIdList) {
            def domID = retrieveDomainId(domainName)
            if (dbConnections.containsKey(domID)) {
                debugLog("  closeConnection  [][]  Close DB connection for domain ID: ${domID}", log)
                dbConnections[domID].connection.close()
                dbConnections.remove(domID)
            } else debugLog("  closeConnection  [][]  DB connection for domain ID: ${domID} was NOT open.", log)
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
        def executeListOfQueriesOnAllDB(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnAllDB\".", log)
        dbConnections.each { domainId, connection ->
            executeListOfSqlQueries(sqlQueriesList, domainId)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // to be removed - invoked in SoapUI
        def executeListOfQueriesOnBlue(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnBlue\".", log)
        log.info "  executeListOfQueriesOnBlue  [][]  Executing SQL queries on sender/Blue"
        executeListOfSqlQueries(sqlQueriesList, blueDomainID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // to be removed  - invoked in SoapUI
        def executeListOfQueriesOnRed(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnRed\".", log)
        log.info "  executeListOfQueriesOnRed  [][]  Executing SQL queries on receiver/Red"
        executeListOfSqlQueries(sqlQueriesList, redDomainID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // to be removed  - NOT invoked in SoapUI
        def executeListOfQueriesOnGreen(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnGreen\".", log)
        log.info "  executeListOfQueriesOnGreen  [][]  Executing SQL queries on Third/Green"
        executeListOfSqlQueries(sqlQueriesList, greenDomainID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
        def executeListOfQueriesOnMany(String[] sqlQueriesList, executeOnDomainIDList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnMany\".", log)
        executeOnDomainIDList.each { domainID ->
            executeListOfSqlQueries(sqlQueriesList, domainID)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
        def executeListOfSqlQueries(String[] sqlQueriesList, String inputTargetDomainID) {
        debugLog("  ====  Calling \"executeListOfSqlQueries\".", log)
        def connectionOpenedInsideMethod = false
        def targetDomainID = retrieveDomainId(inputTargetDomainID)

        if (!dbConnections.containsKey(targetDomainID)) {
            debugLog("  executeListOfSqlQueries  [][]  Method executed without DB connections open - try to open connection", log)
            openDbConnections([targetDomainID])
            connectionOpenedInsideMethod = true
        }

        for (query in sqlQueriesList) {
            debugLog("  executeListOfSqlQueries  [][]  Executing SQL query: " + query + " on domibus: " + targetDomainID, log)
            try {
                dbConnections[targetDomainID].execute query
            } catch (SQLException ex) {
                closeAllDbConnections()
                assert 0,"SQLException occured: " + ex;
            }
        }

        // Maybe this part is not needed as connection would be always close in class destructor
        if (connectionOpenedInsideMethod) {
            debugLog("  executeListOfSqlQueries  [][]  Connection to DB opened during method execution - close opened connection", log)
            closeDbConnections([targetDomainID])
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
        // Retrieve domain ID reference from provided name. When name exists use it
        def retrieveDomainId(String inputName) {
        debugLog("  ====  Calling \"retrieveDomainId\". With inputName: \"${inputName}\"", log)
        def domID = null;

        // For Backward compatibilty
        if (allDomainsProperties.containsKey(inputName)) {
            domID = inputName
        } else {
            switch (inputName.toUpperCase()) {
            case "C3":
            case "RED":
            case "RECEIVER":
                domID = this.redDomainID
                break
            case "C2":
            case "BLUE":
            case "SENDER":
                domID = this.blueDomainID
                break
            case "GREEN":
                assert(thirdGateway.toLowerCase().trim() == "true"), "\"GREEN\" schema is not active. Please set soapui project custom property \"thirdGateway\" to \"true\"."
                domID = this.greenDomainID
                break
            default:
                assert false, "Not supported domain ID ${inputName} provide for retrieveDomainId method. Not able to found it in allDomainsProperties nor common names list. "
                break
            }
        }
        debugLog("   retrieveDomainId  [][]  Input value ${inputName} translated to following domain ID: ${domID}", log)

        return domID as String
    }

        //---------------------------------------------------------------------------------------------------------------------------------
        // Clean all the messages from all defined for domains databases
        def cleanDatabaseAll() {
        debugLog("  ====  Calling \"cleanDatabaseAll\".", log)
        openAllDbConnections()
        cleanDatabaseForDomains(allDomainsProperties.keySet())
        closeAllDbConnections()
    }

//---------------------------------------------------------------------------------------------------------------------------------
        // Clean all the messages from the DB for provided list of domain defined by domain IDs
        def cleanDatabaseForDomains(domainIdList) {
        debugLog("  ====  Calling \"cleanDatabaseForDomains\" ${domainIdList}.", log)
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



        domainIdList.each { domainName ->
            def domain = retrieveDomainId(domainName)
            debugLog("  cleanDatabaseForDomains  [][]  Clean DB for domain ID: ${domain}", log)
            executeListOfSqlQueries(sqlQueriesList, domain)
        }

        log.info "  cleanDatabaseAll  [][]  Cleaning Done"
    }



//---------------------------------------------------------------------------------------------------------------------------------
        // Clean single message identified by messageID starting with provided value from ALL defined DBs
        def cleanDBMessageIDStartsWith(String messageID) {
        debugLog("  ====  Calling \"cleanDBMessageIDStartsWith\".", log)
        cleanDBMessageID(messageID, true)
    }

//---------------------------------------------------------------------------------------------------------------------------------
        // Clean single message identified by messageID starting with provided value from provided list of domains
        def cleanDBMessageIDStartsWithForDomains(String messageID, domainIdList) {
        debugLog("  ====  Calling \"cleanDBMessageIDStartsWith\".", log)
        cleanDBMessageIDForDomains(messageID, domainIdList, true)
    }

//---------------------------------------------------------------------------------------------------------------------------------
        // Clean single message identified by ID
        def cleanDBMessageID(String messageID, boolean  messgaeIDStartWithProvidedValue = false) {
        debugLog("  ====  Calling \"cleanDBMessageID\".", log)
        log.info "  cleanDBMessageID  [][]  Clean from DB information related to the message with ID: " + messageID
        openAllDbConnections()
        cleanDBMessageIDForDomains(messageID, allDomainsProperties.keySet(), messgaeIDStartWithProvidedValue)
        closeAllDbConnections()
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Clean single message identified by ID
        def cleanDBMessageIDForDomains(String messageID, domainIdList, boolean  messgaeIDStartWithProvidedValue = false) {
        debugLog("  ====  Calling \"cleanDBMessageIDForDomains\".", log)
        log.info "  cleanDBMessageIDForDomains  [][]  Clean from DB information related to the message with ID: " + messageID

        def messageIDCheck = "= '${messageID}'" //default comparison method use equal operator
        if (messgaeIDStartWithProvidedValue) messageIDCheck = "like '${messageID}%'" //if cleanDBMessageIDStartsWith method was called change method for comparison

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

        domainIdList.each { domainName ->
            def domain = retrieveDomainId(domainName)
            debugLog("  cleanDBMessageIDForDomains  [][]  Clean DB for domain ID: ${domain}", log)
            executeListOfSqlQueries(sqlQueriesList, domain)
        }

    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Messages Info Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
        // Extract messageID from the request if it exists
        String findGivenMessageID() {
        debugLog("  ====  Calling \"findGivenMessageID\".", log)
        def messageID = null
        def requestContent = messageExchange.getRequestContentAsXml()
        def requestFile = new XmlSlurper().parseText(requestContent)
        requestFile.depthFirst().each {
            if (it.name() == "MessageId") {
                messageID = it.text().toLowerCase().trim()
            }
        }
        return (messageID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Extract messageID from the response
        String findReturnedMessageID() {
        debugLog("  ====  Calling \"findReturnedMessageID\".", log)
        def messageID = null
        def responseContent = messageExchange.getResponseContentAsXml()
        def responseFile = new XmlSlurper().parseText(responseContent)
        responseFile.depthFirst().each {
            if (it.name() == "messageID") {
                messageID = it.text()
            }
        }
        assert(messageID != null),locateTest(context) + "Error:findReturnedMessageID: The message ID is not found in the response"
        if ( (findGivenMessageID() != null) && (findGivenMessageID().trim() != "") ) {
            //if(findGivenMessageID()!=null){
            assert(messageID.toLowerCase() == findGivenMessageID().toLowerCase()),locateTest(context) + "Error:findReturnedMessageID: The message ID returned is (" + messageID + "), the message ID provided is (" + findGivenMessageID() + ")."
        }
        return (messageID.toLowerCase().trim())
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Verification of message existence
        def verifyMessagePresence(int presence1, int presence2, String IDMes = null, String senderDomainId = blueDomainID, String receiverDomanId =  redDomainID) {
        debugLog("  ====  Calling \"verifyMessagePresence\".", log)
        def messageID = null;
        def sqlSender = null; def sqlReceiver = null;
        sleep(sleepDelay)

        if (IDMes != null) {
            messageID = IDMes
        } else {
            messageID = findReturnedMessageID()
        }
        def total = 0
        debugLog("  verifyMessagePresence  [][]  senderDomainId=" + senderDomainId + " receiverDomaindId=" + receiverDomanId, log)
        sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomanId)
        def usedDomains = [senderDomainId, receiverDomanId]
        openDbConnections(usedDomains)

        // Sender DB
        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        if (presence1 == 1) {
            //log.info "total = "+total
            assert(total > 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is not found in sender side."
        }
        if (presence1 == 0) {
            assert(total == 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is found in sender side."
        }

        // Receiver DB
        total = 0
        sleep(sleepDelay)
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        if (presence2 == 1) {
            assert(total > 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is not found in receiver side."
        }
        if (presence2 == 0) {
            assert(total == 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is found in receiver side."
        }

        closeDbConnections(usedDomains)
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Verification of message unicity
        def verifyMessageUnicity(String IDMes = null, String senderDomainId = blueDomainID, String receiverDomanId =  redDomainID) {
        debugLog("  ====  Calling \"verifyMessageUnicity\".", log)
        sleep(sleepDelay)
        def messageID;
        def total = 0;
        def sqlSender = null; def sqlReceiver = null;

        if (IDMes != null) {
            messageID = IDMes
        } else {
            messageID = findReturnedMessageID()
        }
        debugLog("  verifyMessageUnicity  [][]  senderDomainId=" + senderDomainId + " receiverDomaindId=" + receiverDomanId, log)
        sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomanId)
        def usedDomains = [senderDomainId, receiverDomanId]
        openDbConnections(usedDomains)

        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        assert(total == 1),locateTest(context) + "Error:verifyMessageUnicity: Message found " + total + " times in sender side."
        sleep(sleepDelay)
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        assert(total == 1),locateTest(context) + "Error:verifyMessageUnicity: Message found " + total + " times in receiver side."
        closeDbConnections(usedDomains)
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Wait until status or timer expire
        def waitForStatus(String SMSH=null, String RMSH=null, String IDMes=null, String bonusTimeForSender=null, String bonusTimeForReceiver=null, String senderDomainId = blueDomainID, String receiverDomanId =  redDomainID) {
        debugLog("  ====  Calling \"waitForStatus\".", log)
        def MAX_WAIT_TIME=80_000; // Maximum time to wait to check the message status.
        def STEP_WAIT_TIME=1000; // Time to wait before re-checking the message status.
        def messageID = null;
        def numberAttempts = 0;
        def maxNumberAttempts = 5;
        def messageStatus = "INIT";
        def wait = false;
        def sqlSender = null; def sqlReceiver = null;

        //log.info "waitForStatus params: messageID: " + messageID + " RMSH: " + RMSH + " IDMes: " + IDMes + " bonusTimeForSender: " + bonusTimeForSender + " bonusTimeForReceiver: " + bonusTimeForReceiver
        if (IDMes != null) {
            messageID = IDMes
        } else {
            messageID = findReturnedMessageID()
        }

        log.info "  waitForStatus  [][]  params: messageID: " + messageID + " SMSH: " + SMSH + " RMSH: " + RMSH + " IDMes: " + IDMes + " bonusTimeForSender: " + bonusTimeForSender + " bonusTimeForReceiver: " + bonusTimeForReceiver

        if (bonusTimeForSender) {
            log.info "  waitForStatus  [][]  Waiting time for Sender extended to 500 seconds"
            MAX_WAIT_TIME = 500_000
        }

        debugLog("  waitForStatus  [][]  senderDomainId=" + senderDomainId + " receiverDomaindId=" + receiverDomanId, log)
        sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomanId)
        def usedDomains = [senderDomainId, receiverDomanId]
        openDbConnections(usedDomains)

        if (SMSH) {
            while ( ( (messageStatus != SMSH) && (MAX_WAIT_TIME > 0) ) || (wait) ) {
                sleep(STEP_WAIT_TIME)
                if (MAX_WAIT_TIME > 0) {
                    MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
                }
                log.info "  waitForStatus  [][]  WAIT: " + MAX_WAIT_TIME
                sqlSender.eachRow("Select * from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
                    messageStatus = it.MESSAGE_STATUS
                    numberAttempts = it.SEND_ATTEMPTS
                }
                log.info "|MSG_ID: " + messageID + " | SENDER: Expected MSG Status =" + SMSH + "-- Current MSG Status = " + messageStatus + " | maxNumbAttempts: " + maxNumberAttempts + "-- numbAttempts: " + numberAttempts;
                if (SMSH == "SEND_FAILURE") {
                    if (messageStatus == "WAITING_FOR_RETRY") {
                        if ( ( (maxNumberAttempts - numberAttempts) > 0) && (!wait) ) {
                            wait = true
                        }
                        if ( (maxNumberAttempts - numberAttempts) <= 0) {
                            wait = false
                        }
                    } else {
                        if (messageStatus == SMSH) {
                            wait = false;
                        }
                    }
                }
            }
            log.info "  waitForStatus  [][]  finished checking sender, messageStatus: " + messageStatus + " MAX_WAIT_TIME: " + MAX_WAIT_TIME

            assert(messageStatus != "INIT"),locateTest(context) + "Error:waitForStatus: Message " + messageID + " is not present in the sender side."
            assert(messageStatus.toLowerCase() == SMSH.toLowerCase()),locateTest(context) + "Error:waitForStatus: Message in the sender side has status " + messageStatus + " instead of " + SMSH + "."
        }
        if (bonusTimeForReceiver) {
            log.info "  waitForStatus  [][]  Waiting time for Receiver extended to 500 seconds"
            MAX_WAIT_TIME = 100_000
        } else {
            MAX_WAIT_TIME = 30_000
        }
        messageStatus = "INIT"
        if (RMSH) {
            while ( (messageStatus != RMSH) && (MAX_WAIT_TIME > 0) ) {
                sleep(STEP_WAIT_TIME)
                MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
                sqlReceiver.eachRow("Select * from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
                    messageStatus = it.MESSAGE_STATUS
                }
                log.info "  waitForStatus  [][]  W:" + MAX_WAIT_TIME + " M:" + messageStatus
            }
            log.info "  waitForStatus  [][]  finished checking receiver, messageStatus: " + messageStatus + " MAX_WAIT_TIME: " + MAX_WAIT_TIME
            assert(messageStatus != "INIT"),locateTest(context) + "Error:waitForStatus: Message " + messageID + " is not present in the receiver side."
            assert(messageStatus.toLowerCase() == RMSH.toLowerCase()),locateTest(context) + "Error:waitForStatus: Message in the receiver side has status " + messageStatus + " instead of " + RMSH + "."
        }
        closeDbConnections(usedDomains)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Check that an entry is created in the table TB_SEND_ATTEMPT
    def checkSendAttempt(String messageID, String targetSchema="BLUE"){
        debugLog("  ====  Calling \"checkSendAttempt\".", log)
        def MAX_WAIT_TIME=10_000;
        def STEP_WAIT_TIME=1000;
        def sqlSender = null;
        int total = 0;
        openAllDbConnections()

        sqlSender = retrieveSqlConnectionRefFromDomainId(targetSchema)

        while ( (MAX_WAIT_TIME > 0) && (total == 0) ) {
            sqlSender.eachRow("Select count(*) lignes from tb_send_attempt where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
                total = it.lignes
            }
            log.info "  checkSendAttempt  [][]  W: " + MAX_WAIT_TIME;
            sleep(STEP_WAIT_TIME)
            MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME;
        }
        assert(total > 0),locateTest(context) + "Error: Message " + messageID + " is not present in the table tb_send_attempt."
        closeAllDbConnections()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getStatusRetriveStatus(log, context, messageExchange) {
        debugLog("  ====  Calling \"getStatusRetriveStatus\".", log)
        def outStatus = null
        def responseContent = messageExchange.getResponseContentAsXml()
        def requestFile = new XmlSlurper().parseText(responseContent)
        requestFile.depthFirst().each {
            if (it.name() == "getMessageStatusResponse") {
                outStatus = it.text()
            }
        }
        assert(outStatus != null),locateTest(context) + "Error:getStatusRetriveStatus: Not able to return status from response message"
        return outStatus
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Compare payloads order
        static def checkPayloadOrder(submitRequest, log, context, messageExchange){
        debugLog("  ====  Calling \"checkPayloadOrder\".", log)
        def requestAtts = [];
        def responseAtts = [];
        def i = 0;
        //def requestContent = messageExchange.getRequestContentAsXml()
        def requestContent = submitRequest;
        def responseContent = messageExchange.getResponseContentAsXml();;
        assert(requestContent != null),locateTest(context) + "Error: request is empty.";
        assert(responseContent != null),locateTest(context) + "Error: response is empty.";
        def parserFile = new XmlSlurper().parseText(requestContent)
        debugLog("===========================================", log)
        debugLog("  checkPayloadOrder  [][]  Attachments in request: ", log)
        parserFile.depthFirst().each {
            if (it.name() == "PartInfo") {
                requestAtts[i] = it.@href.text()
                debugLog("  checkPayloadOrder  [][]  Attachment: " + requestAtts[i] + " in position " + (i + 1) + ".", log)
                i++;
            }
        }
        debugLog("===========================================", log)
        debugLog("  checkPayloadOrder  [][]  Attachments in response: ", log)
        i = 0;
        parserFile = new XmlSlurper().parseText(responseContent)
        parserFile.depthFirst().each {
            if (it.name() == "PartInfo") {
                responseAtts[i] = it.@href.text()
                debugLog("  checkPayloadOrder  [][]  Attachment: " + responseAtts[i] + " in position " + (i + 1) + ".", log)
                i++;
            }
        }
        debugLog("===========================================", log)
        assert(requestAtts.size() == responseAtts.size()),locateTest(context) + "Error: request has " + requestAtts.size() + " attachements wheras response has " + responseAtts.size() + " attachements.";
        for (i = 0; i < requestAtts.size(); i++) {
            assert(requestAtts[i] == responseAtts[i]),locateTest(context) + "Error: in position " + (i + 1) + " request has attachment " + requestAtts[i] + " wheras response has attachment " + responseAtts[i] + ".";
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  PopUP Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def showPopUpForManualCheck(messagePrefix, log, testRunner) {
        debugLog("  ====  Calling \"showPopUpForManualCheck\".", log)
        def message = messagePrefix + """

		After the manual check of the expected result:
		- Click 'Yes' when result is correct.
		- Click 'No' when result is incorrect. 
		- Click 'Cancel' to skip this check."""

        def result = showConfirmDialog(null, message)
        if (result == JOptionPane.YES_OPTION)
        {
            log.info "PASS MANUAL TEST STEP: Result as expected, continuing the test."
        } else if (result == JOptionPane.NO_OPTION)
        {
            log.info "FAIL MANUAL TEST STEP: Manual check unsuccessful."
            testRunner.fail("Manual check indicated as unsuccessful by user.")
        } else if (result == JOptionPane.CANCEL_OPTION)
        {
            log.info "SKIP MANUAL TEST STEP: Check skipped bu user."
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
        static def showPopUpForManualConfigurationChange(messagePrefix, log, testRunner) {
        debugLog("  ====  Calling \"showPopUpForManualConfigurationChange\".", log)
        def message = messagePrefix + """

		Did configuration was changed?
		- Click 'Yes' when configuration was changed.
		- Click 'No' when configuration was not changed, this test step would be marked as failed.
		- Click 'Cancel' to skip this configuration change, the test would be continue from next test step."""

        def result = showConfirmDialog(null, message)
        if (result == JOptionPane.YES_OPTION)
        {
            log.info "User indicated configuration was changed as described in test step, continuing the test."
        } else if (result == JOptionPane.NO_OPTION)
        {
            log.info "User indicated configuration wasn't changed, this test step would be marked as failed."
            testRunner.fail("User indicated configuration wasn't changed, this test step would be marked as failed.")
        } else if (result == JOptionPane.CANCEL_OPTION)
        {
            log.info "This configuration changed was skipped, continue with next test step."
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Domibus Administration Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
        // Ping Gateway
        static def String pingMSH(String side, context, log) {
        debugLog("  ====  Calling \"pingMSH\".", log)
        def commandString = null;
        def commandResult = null;

        commandString = "curl -s -o /dev/null -w \"%{http_code}\" --noproxy localhost " + urlToDomibus(side, log, context) + "/services";
        commandResult = runCurlCommand(commandString, log)
        return commandResult[0].trim()
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Clear domibus cache
        static def clearCache(String side, context, log, String server = "tomcat") {
        debugLog("  ====  Calling \"clearCache\".", log)
        log.info "Cleaning cache for domibus " + side + " ...";
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def proc = null;
        def pathS = context.expand('${#Project#pathExeSender}')
        def pathR = context.expand('${#Project#pathExeReceiver}')
        def pathRG = context.expand('${#Project#pathExeGreen}')
        def commandToRun = null;
        switch (server.toLowerCase()) {
        case "tomcat":
            switch (side.toLowerCase()) {
            case "sender":
                log.info "PATH = " + pathS;
                commandToRun = "cmd /c cd ${pathS} && " + CLEAR_CACHE_COMMAND_TOMCAT;
                break;
            case "receiver":
                log.info "PATH = " + pathR;
                commandToRun = "cmd /c cd ${pathR} && " + CLEAR_CACHE_COMMAND_TOMCAT;
                break;
            case "receivergreen":
                log.info "PATH = " + pathRG;
                commandToRun = "cmd /c cd ${pathRG} && " + CLEAR_CACHE_COMMAND_TOMCAT;
                break;
            default:
                assert(false), "Unknown side.";
            }
            break;
        case "weblogic":
            log.info "  clearCache  [][]  I don't know how to clean in weblogic yet.";
            break;
        case "wildfly":
            log.info "  clearCache  [][]  I don't know how to clean in wildfly yet.";
            break;
        default:
            assert(false), "Unknown server.";
        }
        if (commandToRun) {
            proc = commandToRun.execute()
            if (proc != null) {
                proc.consumeProcessOutput(outputCatcher, errorCatcher)
                proc.waitFor()
            }
            debugLog("  clearCache  [][]  commandToRun = " + commandToRun, log)
            debugLog("  clearCache  [][]  outputCatcher = " + outputCatcher, log)
            debugLog("  clearCache  [][]  errorCatcher = " + errorCatcher, log)
            log.info "  clearCache  [][]  Cleaning should be done."
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Start several gateways
        static def startSetMSHs(int dom1, int dom2, int dom3, context, log) {
        debugLog("  ====  Calling \"startSetMSHs\".", log)
        if (dom1 > 0) {
            startMSH("sender", context, log)
        }
        if (dom2 > 0) {
            startMSH("receiver", context, log)
        }
        if (dom3 > 0) {
            startMSH("receivergreen", context, log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Start Gateway
        static def startMSH(String side, context, log){
        debugLog("  ====  Calling \"startMSH\".", log)
        def MAX_WAIT_TIME=100000; // Maximum time to wait for the domibus to start.
        def STEP_WAIT_TIME=2000; // Time to wait before re-checking the domibus status.
        def confirmation = 0;
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def pathS = context.expand('${#Project#pathExeSender}')
        def pathR = context.expand('${#Project#pathExeReceiver}')
        def pathRG = context.expand('${#Project#pathExeGreen}')
        def proc = null;
        def passedDuration = 0;

        // In case of ping failure try 2 times: from experience, sometimes domibus is running and for some reason the ping fails (trying 2 times could reduce the error occurence).
        while (confirmation <= 1) {
            if (pingMSH(side, context, log).equals("200")) {
                log.info "  startMSH  [][]  " + side.toUpperCase() + " is already running!";
                confirmation++;
            } else {
                if (confirmation > 0) {
                    log.info "  startMSH  [][]  Trying to start the " + side.toUpperCase()
                    if (side.toLowerCase() == "sender") {
                        proc = "cmd /c cd ${pathS} && startup.bat".execute()
                    } else {
                        if (side.toLowerCase() == "receiver") {
                            proc = "cmd /c cd ${pathR} && startup.bat".execute()
                        } else {
                            if ( (side.toLowerCase() == "receivergreen") ) {
                                proc = "cmd /c cd ${pathRG} && startup.bat".execute()
                            } else {
                                assert(false), "Incorrect side"
                            }
                        }
                    }
                    if (proc != null) {
                        proc.consumeProcessOutput(outputCatcher, errorCatcher)
                        proc.waitFor()
                    }
                    assert((!errorCatcher) && (proc != null)), locateTest(context) + "Error:startMSH: Error while trying to start the MSH."
                    while ( (!pingMSH(side, context, log).equals("200")) && (passedDuration < MAX_WAIT_TIME) ) {
                        passedDuration = passedDuration + STEP_WAIT_TIME
                        sleep(STEP_WAIT_TIME)
                    }
                    assert(pingMSH(side, context, log).equals("200")),locateTest(context) + "Error:startMSH: Error while trying to start the MSH."
                    log.info "  startMSH  [][]  DONE - " + side.toUpperCase() + " started."
                }
            }
            sleep(STEP_WAIT_TIME)
            confirmation++;
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Stop several gateways
    static def stopSetMSHs(int dom1, int dom2, int dom3, context, log) {
        debugLog("  ====  Calling \"stopSetMSHs\".", log)
        if (dom1 > 0) {
            stopMSH("sender", context, log)
        }
        if (dom2 > 0) {
            stopMSH("receiver", context, log)
        }
        if (dom3 > 0) {
            stopMSH("receivergreen", context, log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Stop Gateway
        static def stopMSH(String side, context, log){
        debugLog("  ====  Calling \"stopMSH\".", log)
        def MAX_WAIT_TIME=5000; // Maximum time to wait for the domibus to stop.
        def STEP_WAIT_TIME=500; // Time to wait before re-checking the domibus status.
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def proc = null;
        def pathS = context.expand('${#Project#pathExeSender}')
        def pathR = context.expand('${#Project#pathExeReceiver}')
        def pathRG = context.expand('${#Project#pathExeGreen}')
        def passedDuration = 0;

        if (!pingMSH(side, context, log).equals("200")) {
            log.info "  stopMSH  [][]  " + side.toUpperCase() + " is not running!"
        } else {
            log.info "  stopMSH  [][]  Trying to stop the " + side.toUpperCase()
            switch (side.toLowerCase()) {
            case "sender":
                proc = "cmd /c cd ${pathS} && shutdown.bat".execute()
                break;
            case "receiver":
                proc = "cmd /c cd ${pathR} && shutdown.bat".execute()
                break;
            case "receivergreen":
                proc = "cmd /c cd ${pathRG} && shutdown.bat".execute()
                break;
            default:
                assert(false), "Unknown side.";
            }
            if (proc != null) {
                proc.consumeProcessOutput(outputCatcher, errorCatcher)
                proc.waitFor()
            }
            assert((!errorCatcher) && (proc != null)),locateTest(context) + "Error:stopMSH: Error while trying to stop the MSH."
            while ( (pingMSH(side, context, log).equals("200")) && (passedDuration < MAX_WAIT_TIME) ) {
                passedDuration = passedDuration + STEP_WAIT_TIME;
                sleep(STEP_WAIT_TIME)
            }
            assert(!pingMSH(side, context, log).equals("200")),locateTest(context) + "Error:startMSH: Error while trying to stop the MSH."
            log.info "  stopMSH  [][]  DONE - " + side.toUpperCase() + " stopped."
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uploadPmode(String side, String baseFilePath, String extFilePath, context, log, String domainValue = "Default", String outcome = "successfully", String message = null, String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"uploadPmode\".", log)
        log.info "  uploadPmode  [][]  Start upload PMode for Domibus \"" + side + "\".";
        def commandString = null;
        def commandResult = null;
        def pmDescription = "SoapUI sample test description for PMode upload";
        def multitenancyOn = false;
        def authenticationUser = authUser;
        def authenticationPwd = authPwd;
        def String pmodeFile = computePathRessources(baseFilePath, extFilePath, context, log)

        log.info "  uploadPmode  [][]  PMODE FILE PATH: " + pmodeFile;

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            commandString="curl "+urlToDomibus(side, log, context)+"/rest/pmode -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -F \"description="+pmDescription+"\" -F  file=@"+pmodeFile;
            commandResult = runCurlCommand(commandString, log)
            assert(commandResult[0].contains(outcome)),"Error:uploadPmode: Error while trying to upload the PMode: response doesn't contain the expected outcome \"" + outcome + "\"."
            if (outcome.toLowerCase() == "successfully") {
                log.info "  uploadPmode  [][]  " + commandResult[0] + " Domibus: \"" + side + "\".";
                if (message != null) {
                    assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload done but expected message \"" + message + "\" was not returned."
                }
            } else {
                log.info "  uploadPmode  [][]  Upload PMode was not done for Domibus: \"" + side + "\".";
                if (message != null) {
                    assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload was not done but expected message \"" + message + "\" was not returned."
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uploadPmodeWithoutToken(String side, String baseFilePath, String extFilePath, context, log, String outcome = "successfully", String message =null, String userLogin=null, passwordLogin=null){
        debugLog("  ====  Calling \"uploadPmodeWithoutToken\".", log)
        log.info "  uploadPmodeWithoutToken  [][]  Start upload PMode for Domibus \""+side+"\".";
        def commandString = null;
        def commandResult = null;
        def pmDescription = "Dummy";

        def String output = fetchCookieHeader(side, context, log, userLogin, passwordLogin)
        def XXSRFTOKEN = null;
        def String pmodeFile = computePathRessources(baseFilePath, extFilePath, context, log)

        commandString = "curl " + urlToDomibus(side, log, context) + "/rest/pmode -v -F \"description=" + pmDescription + "\" -F  file=@" + pmodeFile;
        commandResult = runCurlCommand(commandString, log)
        assert(commandResult[0].contains(outcome)),"Error:uploadPmode: Error while trying to connect to domibus."
        if (outcome.toLowerCase() == "successfully") {
            log.info "  uploadPmodeWithoutToken  [][]  " + commandResult[0] + " Domibus: \"" + side + "\".";
            if (message != null) {
                assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload done but expected message \"" + message + "\" was not returned."
            }
        } else {
            log.info "  uploadPmodeWithoutToken  [][]  Upload PMode was not done for Domibus: \"" + side + "\".";
            if (message != null) {
                assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload was not done but expected message \"" + message + "\" was not returned."
            }
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uploadTruststore(String side, String baseFilePath, String extFilePath, context, log, String domainValue="Default", String outcome="successfully", String tsPassword=TRUSTSTORE_PASSWORD, String authUser=null, authPwd=null){
        debugLog("  ====  Calling \"uploadTruststore\".", log)
        log.info "  uploadTruststore  [][]  Start upload truststore for Domibus \""+side+"\".";
        def commandString = null;
        def commandResult = null;
        def multitenancyOn=false;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;
        def String truststoreFile=null;

        try{
            debugLog("  uploadTruststore  [][]  Fetch multitenancy mode on domibus $side.", log)
            (authenticationUser, authenticationPwd)=retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            truststoreFile=computePathRessources(baseFilePath,extFilePath,context,log)
            commandString = "curl " + urlToDomibus(side, log, context) + "/rest/truststore/save -b " + context.expand('${projectDir}') + "\\cookie.txt -v -H \"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd) + "\" -F \"password=" + tsPassword + "\" -F  truststore=@" + truststoreFile;
            commandResult = runCurlCommand(commandString, log)
            assert(commandResult[0].contains(outcome)),"Error:uploadTruststore: Error while trying to upload the truststore to domibus."
            log.info "  uploadTruststore  [][]  " + commandResult[0] + " Domibus: \"" + side + "\".";
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Change Domibus configuration file
    static def void changeDomibusProperties(color, propValueDict, log, context, testRunner){
        debugLog("  ====  Calling \"changeDomibusProperties\".", log)
        // Check that properties file exist and if yes create backup_file
        // For all properties name and new value pairs change value in file
        // to restore configuration use method restoreDomibusPropertiesFromBackup(domibusPath,  log, context, testRunner)
        def pathToPropertyFile = pathToDomibus(color, log, context) + context.expand('${#Project#subPathToDomibusProperties}')

        // Check file exists
        def testFile = new File(pathToPropertyFile)
        if (!testFile.exists()) {
            testRunner.fail("File [${pathToPropertyFile}] does not exist. Can't change value.")
            return null
        } else log.info "  changeDomibusProperties  [][]  File [${pathToPropertyFile}] exists."

        // Create backup file if already not created
        def backupFileName = "${pathToPropertyFile}${backup_file_sufix}"
        def backupFile = new File(backupFileName)
        if (backupFile.exists()) {
            log.info "  changeDomibusProperties  [][]  File [${backupFileName}] already exists and would not be overwrite - old backup file would be preserved."
        } else  {
            copyFile(pathToPropertyFile, backupFileName, log)
            log.info "  changeDomibusProperties  [][]  Backup copy of config file created: [${backupFile}]"
        }

        def fileContent = testFile.text
        //run in loop for all properties key values pairs
        for(item in propValueDict){
            def propertyToChangeName = item.key
            def newValueToAssign = item.value

            // Check that property exist in config file
            def found = 0
            def foundInCommentedRow = 0
            testFile.eachLine{
                line, n ->
                n++
                if(line =~ /^\s*$ { propertyToChangeName }
                   = /) {
                    log.info "  changeDomibusProperties  [][]  In line $n searched property was found. Line value is: $line"
                    found++
                }
                if(line =~ ~/#+\s*$ { propertyToChangeName }
                   = .*/) {
                    log.info "  changeDomibusProperties  [][]  In line $n commented searched property was found. Line value is: $line"
                    foundInCommentedRow++
                }
            }

            if (found > 1) {
                testRunner.fail("The search string ($propertyToChangeName=) was found ${found} times in file [${pathToPropertyFile}]. Expect only one assigment - check if configuration file is not corrupted.")
                return null
            }
            // If property is present in file change it value
            if(found)
            fileContent = fileContent.replaceAll(/(?m)^\s*($ { propertyToChangeName }
                                                           = )(.*)/) { all, paramName, value -> "${paramName}${newValueToAssign}" } else
            if(foundInCommentedRow)
            fileContent = fileContent.replaceFirst(/(?m)^#+\s*($ { propertyToChangeName }
                                                               = )(.*)/) { all, paramName, value -> "${paramName}${newValueToAssign}" } else {
                testRunner.fail("The search string ($propertyToChangeName) was not found in file [${pathToPropertyFile}]. No changes would be applied - properties file restored.")
                return null
            }
            log.info "  changeDomibusProperties  [][]  In [${pathToPropertyFile}] file property ${propertyToChangeName} was changed to value ${newValueToAssign}"
        } //loop end

        // Store new content of properties file after all changes
        testFile.text=fileContent
        log.info "  changeDomibusProperties  [][]  Property file [${pathToPropertyFile}] amended"
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Restor Domibus configuration file
    static def void restoreDomibusPropertiesFromBackup(color, log, context, testRunner) {
        debugLog("  ====  Calling \"restoreDomibusPropertiesFromBackup\".", log)
        // Restore from backup file domibus.properties file
        def pathToPropertyFile = pathToDomibus(color, log, context) + context.expand('${#Project#subPathToDomibusProperties}')
        def backupFile = "${pathToPropertyFile}${backup_file_sufix}"

        // Check backup file exists
        def backupFileHandler = new File(backupFile)
        if (!backupFileHandler.exists()) {
            testRunner.fail("CRITICAL ERROR: File [${backupFile}] does not exist.")
            return null
        } else {
            log.info "  restoreDomibusPropertiesFromBackup  [][]  Restore properties file from existing backup"
            copyFile(backupFile, pathToPropertyFile, log)
            if (backupFileHandler.delete()) {
                log.info "  restoreDomibusPropertiesFromBackup  [][]  Successufuly restory configuration from backup file and backup file was removed"
            } else {
                testRunner.fail "Not able to delete configuration backup file"
                return null
            }
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Domain Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def getDomainName(String domainInfo, log) {
        debugLog("  ====  Calling \"getDomainName\".", log)
        assert((domainInfo != null) && (domainInfo != "")),"Error:getDomainName: provided domain info are empty.";
        debugLog("  getDomainName  [][]  Get domain name from domain info: $domainInfo.", log)
        def jsonSlurper = new JsonSlurper()
        def domainMap = jsonSlurper.parseText(domainInfo)
        assert(domainMap.name != null),"Error:getDomain: Domain informations are corrupted: $domainInfo.";
        return domainMap.name;
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getDomain(String side, context, log, String userLogin = SUPER_USER, String passwordLogin = SUPER_USER_PWD) {
        debugLog("  ====  Calling \"getDomain\".", log)
        assert(userLogin == SUPER_USER),"Error:getDomains: To manipulate domains, login must be done with user: \"$SUPER_USER\"."
        log.info "  getDomain  [][]  Get current domain for Domibus $side.";
        def commandString = null;
        def commandResult = null;

        commandString = "curl " + urlToDomibus(side, log, context) + "/rest/security/user/domain -b " + context.expand('${projectDir}') + "\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, userLogin, passwordLogin) + "\" GET ";
        commandResult = runCurlCommand(commandString, log)
        assert(commandResult[1].contains("200 OK")),"Error:getDomain: Error while trying to connect to domibus."
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def setDomain(String side, context, log, String domainValue, String userLogin=SUPER_USER, String passwordLogin=SUPER_USER_PWD){
        debugLog("  ====  Calling \"setDomain\".", log)
        def commandString = null;
        def commandResult = null;

        assert(userLogin==SUPER_USER),"Error:getDomains: To manipulate domains, login must be done with user: \"$SUPER_USER\"."
        debugLog("  setDomain  [][]  Set domain for Domibus $side.", log)
        if (domainValue == getDomainName(getDomain(side, context, log), log)) {
            debugLog("  setDomain  [][]  Requested domain is equal to the current value: no action needed", log)
        } else {
            commandString="curl "+urlToDomibus(side, log, context)+"/rest/security/user/domain -b "+context.expand('${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side, context, log, userLogin, passwordLogin)+"\" -X PUT -d \"\"\""+domainValue+"\"\"\"";
            commandResult = runCurlCommand(commandString, log)
            assert(commandResult[1].contains("200 OK")||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:setDomain: Error while trying to set the domain: verify that domain $domainValue is correctly configured."
            debugLog("  setDomain  [][]  Domain set to $domainValue.",log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Return number of domains
    static def getMultitenancyMode(String inputValue, log) {
        debugLog("  ====  Calling \"getMultitenancyMode\".", log)
        if ( (inputValue == null) || (inputValue == "") ) {
            return 0;
        }
        if (inputValue.trim().isInteger()) {
            return (inputValue as Integer)
        } else {
            return 0;
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Users Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def getAdminConsoleUsers(String side, context, log, String authUser=null, String authPwd=null){
        debugLog("  ====  Calling \"getAdminConsoleUsers\".", log)
        debugLog("  getAdminConsoleUsers  [][]  Get Admin Console users for Domibus \"$side\".", log)
        def commandString = null;
        def commandResult = null;
        def multitenancyOn=false;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;

        (authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)

        commandString="curl "+urlToDomibus(side, log, context)+"/rest/user/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET ";
        commandResult = runCurlCommand(commandString, log)
        assert(commandResult[1].contains("200 OK") || commandResult[1].contains("successfully")),"Error:getAdminConsoleUsers: Error while trying to connect to domibus.";
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def addAdminConsoleUser(String side, context, log, String domainValue="Default", String userRole="ROLE_ADMIN", String userAC, String passwordAC="Domibus-123", String authUser=null, String authPwd=null){
        debugLog("  ====  Calling \"addAdminConsoleUser\".", log)
        def usersMap=null;
        def mapElement=null;
        def multitenancyOn=false;
        def commandString = null;
        def commandResult = null;
        def jsonSlurper = new JsonSlurper()
        def curlParams=null;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  addAdminConsoleUser  [][]  Fetch users list and verify that user \"$userAC\" doesn't already exist.",log)
            usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log))
            if (userExists(usersMap, userAC, log, false)) {
                log.error "Error:addAdminConsoleUser: Admin Console user \"$userAC\" already exist: usernames must be unique.";
            } else {
                debugLog("  addAdminConsoleUser  [][]  Users list before the update: " + usersMap, log)
                debugLog("  addAdminConsoleUser  [][]  Prepare user \"$userAC\" details to be added.", log)
                curlParams = "[\n  {\n    \"roles\": \"$userRole\",\n    \"userName\": \"$userAC\",\n    \"password\": \"$passwordAC\",\n    \"status\": \"NEW\",\n    \"active\": true,\n    \"suspended\": false,\n    \"authorities\": [],\n    \"deleted\": false\n    }\n]";
                debugLog("  addAdminConsoleUser  [][]  Inserting user \"$userAC\" in list.", log)
                debugLog("  addAdminConsoleUser  [][]  User \"$userAC\" parameters: $curlParams.", log)
                commandString = "curl " + urlToDomibus(side, log, context) + "/rest/user/users -b " + context.expand('${projectDir}') + "\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd) + "\" -X PUT -d " + formatJsonForCurl(curlParams, log)
                commandResult = runCurlCommand(commandString, log)
                assert(commandResult[1].contains("200 OK")||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:addAdminConsoleUser: Error while trying to add a user.";
                log.info "  addAdminConsoleUser  [][]  Admin Console user \"$userAC\" added.";
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def removeAdminConsoleUser(String side, context, log, String domainValue="Default", String userAC, String authUser=null, String authPwd=null){
        debugLog("  ====  Calling \"removeAdminConsoleUser\".", log)
        def usersMap=null;
        def multitenancyOn=false;
        def commandString = null;
        def commandResult = null;
        def jsonSlurper = new JsonSlurper()
        def curlParams=null;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;
        def roleAC=null;
        def userDeleted=false;
        def i=0;

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  removeAdminConsoleUser  [][]  Fetch users list and verify that user \"$userAC\" exists.",log)
            usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log))
            if (!userExists(usersMap, userAC, log, false)) {
                log.info "  removeAdminConsoleUser  [][]  Admin console user \"$userAC\" doesn't exist. No action needed.";
            } else {
                while (i < usersMap.size()) {
                    assert(usersMap[i] != null),"Error:removeAdminConsoleUser: Error while parsing the list of admin console users.";
                    if (usersMap[i].userName == userAC) {
                        roleAC = usersMap[i].roles;
                        userDeleted = usersMap[i].deleted;
                        i = usersMap.size()
                    }
                    i++;
                }
                assert(roleAC != null),"Error:removeAdminConsoleUser: Error while fetching the role of user \"$userAC\".";
                assert(userDeleted != null),"Error:removeAdminConsoleUser: Error while fetching the \"deleted\" status of user \"$userAC\".";
                if (userDeleted == false) {
                    curlParams = "[\n  {\n    \"userName\": \"$userAC\",\n    \"roles\": \"$roleAC\",\n    \"active\": true,\n    \"authorities\": [\n      \"$roleAC\"\n    ],\n    \"status\": \"REMOVED\",\n    \"suspended\": false,\n    \"deleted\": true\n    }\n]";
                    commandString = "curl " + urlToDomibus(side, log, context) + "/rest/user/users -b " + context.expand('${projectDir}') + "\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd) + "\" -X PUT -d " + formatJsonForCurl(curlParams, log)
                    commandResult = runCurlCommand(commandString, log)
                    assert(commandResult[1].contains("200 OK")),"Error:removeAdminConsoleUser: Error while trying to remove user $userAC.";
                    log.info "  removeAdminConsoleUser  [][]  User \"$userAC\" Removed."
                } else {
                    log.info "  removeAdminConsoleUser  [][]  User \"$userAC\" was already deleted. No action needed.";
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getPluginUsers(String side, context, log, String authUser=null, String authPwd=null){
        debugLog("  ====  Calling \"getPluginUsers\".", log)
        def commandString = null;
        def commandResult = null;
        def multitenancyOn=false;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;

        (authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)

        commandString="curl "+urlToDomibus(side, log, context)+"/rest/plugin/users -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET ";
        commandResult = runCurlCommand(commandString, log)
        assert(commandResult[1].contains("200 OK") || commandResult[1].contains("successfully")),"Error:getPluginUsers: Error while trying to connect to domibus.";
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def addPluginUser(String side, context, log, String domainValue="Default", String userRole="ROLE_ADMIN", String userPl, String passwordPl="Domibus-123", String originalUser="urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", String authUser=null, String authPwd=null){
        debugLog("  ====  Calling \"addPluginUser\".", log)
        def usersMap=null;
        def mapElement=null;
        def multitenancyOn=false;
        def commandString = null;
        def commandResult = null;
        def jsonSlurper = new JsonSlurper()
        def curlParams=null;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  addPluginUser  [][]  Fetch users list and verify that user $userPl doesn't already exist.",log)
            usersMap = jsonSlurper.parseText(getPluginUsers(side, context, log))
            if (userExists(usersMap, userPl, log, true)) {
                log.error "Error:addPluginUser: plugin user $userPl already exist: usernames must be unique.";
            } else {
                debugLog("  addPluginUser  [][]  Users list before the update: " + usersMap, log)
                debugLog("  addPluginUser  [][]  Prepare user $userPl details to be added.", log)
                curlParams = "[\n  {\n    \"status\": \"NEW\",\n    \"username\": \"$userPl\",\n    \"authenticationType\": \"BASIC\",\n    \"originalUser\": \"$originalUser\",\n    \"authRoles\": \"$userRole\",\n    \"passwd\": \"$passwordPl\"\n  }\n]";
                debugLog("  addPluginUser  [][]  Inserting user $userPl in the list.", log)
                commandString = "curl " + urlToDomibus(side, log, context) + "/rest/plugin/users -b " + context.expand('${projectDir}') + "\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd) + "\" -X PUT -d " + formatJsonForCurl(curlParams, log)
                commandResult = runCurlCommand(commandString, log)
                assert(commandResult[1].contains("200 OK")||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:addPluginUser: Error while trying to add a user.";
                log.info "  addPluginUser  [][]  Plugin user $userPl added.";
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def removePluginUser(String side, context, log, String domainValue="Default", String userPl, String authUser=null, String authPwd=null){
        debugLog("  ====  Calling \"removePluginUser\".", log)
        def usersMap=null;
        def mapElement=null;
        def multitenancyOn=false;
        def commandString = null;
        def commandResult = null;
        def jsonSlurper = new JsonSlurper()
        def curlParams=null;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;
        def originalUser=null;
        def rolePl=null;
        def entityId=null;
        def i=0;

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  removePluginUser  [][]  Fetch users list and verify that user $userPl exists.",log)
            usersMap = jsonSlurper.parseText(getPluginUsers(side, context, log))
            debugLog("  removePluginUser  [][]  usersMap:	$usersMap", log)
            if (!userExists(usersMap, userPl, log, true)) {
                log.info "  removePluginUser  [][]  Plugin user $userPl doesn't exist. No action needed.";
            } else {
                while (i < usersMap.entries.size()) {
                    assert(usersMap.entries[i] != null),"Error:removePluginUser: Error while parsing the list of plugin users.";
                    if (usersMap.entries[i].username == userPl) {
                        rolePl = usersMap.entries[i].authRoles;
                        originalUser = usersMap.entries[i].originalUser;
                        entityId = usersMap.entries[i].entityId;
                        i = usersMap.entries.size()
                    }
                    i++;
                }
                assert(rolePl != null),"Error:removePluginUser: Error while fetching the role of user \"$userPl\".";
                assert(originalUser != null),"Error:removePluginUser: Error while fetching the original user of user \"$userPl\".";
                assert(entityId != null),"Error:removePluginUser: Error while fetching the \"entityId\" of user \"$userPl\" from the user list.";
                curlParams = "[\n  {\n    \"entityId\": $entityId,\n    \"username\": \"$userPl\",\n    \"password\": null,\n    \"certificateId\": null,\n    \"originalUser\": \"$originalUser\",\n    \"authRoles\": \"$rolePl\",\n    \"authenticationType\": \"BASIC\",\n    \"status\": \"REMOVED\"\n  }\n]";
                commandString = "curl " + urlToDomibus(side, log, context) + "/rest/plugin/users -b " + context.expand('${projectDir}') + "\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd) + "\" -X PUT -d " + formatJsonForCurl(curlParams, log)
                commandResult = runCurlCommand(commandString, log)
                assert(commandResult[1].contains("200 OK")||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:removePluginUser: Error while trying to remove user $userPl.";
                log.info "  removePluginUser  [][]  Plugin user $userPl removed.";
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def userExists(usersMap, String targetedUser, log, boolean plugin = false) {
        debugLog("  ====  Calling \"userExists\".", log)
        def i = 0;
        def userFound = false;
        if (plugin) {
            debugLog("  userExists  [][]  Checking if plugin user \"$targetedUser\" exists.", log)
            debugLog("  userExists  [][]  Plugin users map: $usersMap.", log)
            assert(usersMap.entries != null),"Error:userExists: Error while parsing the list of plugin users.";
            while ( (i < usersMap.entries.size()) && (userFound == false) ) {
                assert(usersMap.entries[i] != null),"Error:userExists: Error while parsing the list of plugin users.";
                debugLog("  userExists  [][]  Iteration $i: comparing --$targetedUser--and--" + usersMap.entries[i].username + "--.", log)
                if (usersMap.entries[i].username == targetedUser) {
                    userFound = true;
                }
                i++;
            }
        } else {
            debugLog("  userExists  [][]  Checking if admin console user \"$targetedUser\" exists.", log)
            debugLog("  userExists  [][]  Admin console users map: $usersMap.", log)
            assert(usersMap != null),"Error:userExists: Error while parsing the list of admin console users.";
            while ( (i < usersMap.size()) && (userFound == false) ) {
                assert(usersMap[i] != null),"Error:userExists: Error while parsing the list of admin console users.";
                if (usersMap[i].userName == targetedUser) {
                    userFound = true;
                }
                i++;
            }
        }

        return userFound;
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def resetAuthTokens(log) {
        debugLog("  ====  Calling \"resetAuthTokens\".", log)
        XSFRTOKEN_C2 = null;
        XSFRTOKEN_C3 = null;
        XSFRTOKEN_C_Other = null;
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Insert wrong password
    static def insertWrongPassword(String side, context, log, String username, int attempts=1, String domainValue="Default", String wrongPass="zzzdumzzz", String authUser=null, String authPwd=null){
        debugLog("  ====  Calling \"insertWrongPassword\".", log)
        def usersMap=null;
        def multitenancyOn=false;
        def jsonSlurper = new JsonSlurper()
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;
        def commandResult = null;
        def commandString = null;

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  insertWrongPassword  [][]  Fetch users list and verify that user $username exists.",log)
            usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log))
            debugLog("  insertWrongPassword  [][]  usersMap:	$usersMap", log)
            assert(userExists(usersMap, username, log, false)),"Error:insertWrongPassword: user \"$username\" was not found.";
            // Try to login with wrong password
            commandString = "curl " + urlToDomibus(side, log, context) + "/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\"" + username + "\"\"\",\"\"\"password\"\"\":\"\"\"$wrongPass\"\"\"}\" -c " + context.expand('${projectDir}') + "\\cookie.txt";
            for (def i = 1; i <= attempts; i++) {
                log.info("  insertWrongPassword  [][]  Try to login with wrong password: Attempt $i.")
                commandResult = runCurlCommand(commandString, log)
                assert((commandResult[0].contains("Bad credentials")) || (commandResult[0].contains("Suspended"))),"Error:Authenticating user: Error while trying to connect to domibus."
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Message filter Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def getMessageFilters(String side, context, log, String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"getMessageFilters\".", log)
        log.info "  getMessageFilters  [][]  Get message filters for Domibus \"" + side + "\".";
        def commandString = null;
        def commandResult = null;
        def multitenancyOn = false;
        def authenticationUser = authUser;
        def authenticationPwd = authPwd;

        (authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)

        commandString="curl "+urlToDomibus(side, log, context)+"/rest/messagefilters -b "+context.expand( '${projectDir}')+"\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET ";
        commandResult = runCurlCommand(commandString, log)
        assert(commandResult[1].contains("200 OK") || commandResult[1].contains("successfully")),"Error:getMessageFilter: Error while trying to connect to domibus."
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def formatFilters(filtersMap, String filterChoice, context, log, String extraCriteria = null) {
        debugLog("  ====  Calling \"formatFilters\".", log)
        log.info "  formatFilters  [][]  Analysing backends filters order ..."
        def swapBck = null;
        def i = 0;
        assert(filtersMap != null),"Error:formatFilters: Not able to get the backend details.";
        debugLog("  formatFilters  [][]  FILTERS:" + filtersMap, log)

        // Single backend: no action needed
        if (filtersMap.messageFilterEntries.size() == 1) {
            return "ok";
        }
        debugLog("  formatFilters  [][]  Loop over :" + filtersMap.messageFilterEntries.size() + " backend filters.", log)
        while (i < filtersMap.messageFilterEntries.size()) {
            assert(filtersMap.messageFilterEntries[i] != null),"Error:formatFilters: Error while parsing filter details.";
            if (filtersMap.messageFilterEntries[i].backendName.toLowerCase() == filterChoice.toLowerCase()) {
                debugLog("  formatFilters  [][]  Comparing --" + filtersMap.messageFilterEntries[i].backendName + "-- and --" + filterChoice + "--", log)
                if ( (extraCriteria == null) || ( (extraCriteria != null) && filtersMap.messageFilterEntries[i].toString().contains(extraCriteria)) ) {
                    if (i == 0) {
                        return "correct";
                    }
                    debugLog("  formatFilters  [][]  switch $i element", log)
                    swapBck = filtersMap.messageFilterEntries[0];
                    filtersMap.messageFilterEntries[0] = filtersMap.messageFilterEntries[i];
                    filtersMap.messageFilterEntries[i] = swapBck;
                    return filtersMap;
                }
            }
            i++;
        }
        return "ko";
    }
//---------------------------------------------------------------------------------------------------------------------------------
        static def setMessageFilters(String side, String filterChoice, context, log, domainValue="Default", String extraCriteria=null, String authUser=null, authPwd=null){
        debugLog("  ====  Calling \"setMessageFilters\".", log)
        log.info "  setMessageFilters  [][]  Start setting message filters for Domibus \""+side+"\".";
        def String output=null;
        def commandString=null;
        def commandResult=null;
        def curlParams=null;
        def filtersMap=null;
        def multitenancyOn=false;
        def authenticationUser=authUser;
        def authenticationPwd=authPwd;
        def jsonSlurper=new JsonSlurper()

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            filtersMap=jsonSlurper.parseText(getMessageFilters(side,context,log))
            debugLog("  setMessageFilters  [][]  filtersMap:" + filtersMap, log)
            assert(filtersMap != null),"Error:setMessageFilter: Not able to get the backend details.";
            assert(filtersMap.toString().toLowerCase().contains(filterChoice.toLowerCase())),"Error:setMessageFilter: The backend you want to set is not installed.";
            filtersMap = formatFilters(filtersMap, filterChoice, context, log, extraCriteria)
            assert(filtersMap != "ko"),"Error:setMessageFilter: The backend you want to set is not installed."
            debugLog("  setMessageFilters  [][]  Backend filters order analyse done.", log)
            if (filtersMap.equals("ok")) {
                log.info "  setMessageFilters  [][]  Only one backend installed: Nothing to do.";
            } else {
                if (filtersMap.equals("correct")) {
                    log.info "  setMessageFilters  [][]  The requested backend is already selected: Nothing to do.";
                } else {
                    curlParams = JsonOutput.toJson(filtersMap).toString()
                    commandString = "curl " + urlToDomibus(side, log, context) + "/rest/messagefilters -b " + context.expand('${projectDir}') + "\\cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd) + "\" -X PUT -d " + formatJsonForCurl(curlParams, log)
                    commandResult = runCurlCommand(commandString, log)
                    assert(commandResult[1].contains("200 OK") || commandResult[1].contains("successfully")),"Error:setMessageFilter: Error while trying to connect to domibus.";
                    log.info "  setMessageFilters  [][]  Message filters update done successfully for Domibus: \"" + side + "\".";
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Curl related Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def fetchCookieHeader(String side, context, log, String userLogin = SUPER_USER, passwordLogin = SUPER_USER_PWD) {
        debugLog("  ====  Calling \"fetchCookieHeader\".", log)
        def commandString = null;
        def commandResult = null;

        commandString = "curl " + urlToDomibus(side, log, context) + "/rest/security/authentication -i -H \"Content-Type: application/json\" -X POST -d \"{\"\"\"username\"\"\":\"\"\"$userLogin\"\"\",\"\"\"password\"\"\":\"\"\"$passwordLogin\"\"\"}\" -c " + context.expand('${projectDir}') + "\\cookie.txt";
        commandResult = runCurlCommand(commandString, log)
        assert(commandResult[0].contains("200 OK")),"Error:Authenticating user: Error while trying to connect to domibus."
        return commandResult[0];
    }
//---------------------------------------------------------------------------------------------------------------------------------
        static def String returnXsfrToken(String side, context, log, String userLogin = SUPER_USER, passwordLogin = SUPER_USER_PWD) {
        debugLog("  ====  Calling \"returnXsfrToken\".", log)
        debugLog("  returnXsfrToken  [][]  Call returnXsfrToken with values: --side=$side--XSFRTOKEN_C2=$XSFRTOKEN_C2--XSFRTOKEN_C3=$XSFRTOKEN_C3.", log)
        def String output = null;

        switch (side.toLowerCase()) {
        case "c2":
        case "blue":
        case "sender":
            if (XSFRTOKEN_C2 == null) {
                output = fetchCookieHeader(side, context, log, userLogin, passwordLogin)
                XSFRTOKEN_C2 = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
            }
            return XSFRTOKEN_C2;
            break;
        case "c3":
        case "red":
        case "receiver":
            if (XSFRTOKEN_C3 == null) {
                output = fetchCookieHeader(side, context, log, userLogin, passwordLogin)
                XSFRTOKEN_C3 = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
            }
            return XSFRTOKEN_C3;
            break;
        case "receivergreen":
            if (XSFRTOKEN_C_Other == null) {
                output = fetchCookieHeader(side, context, log, userLogin, passwordLogin)
                XSFRTOKEN_C_Other = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
            }
            return XSFRTOKEN_C_Other;
            break;
        default:
            assert(false), "returnXsfrToken: Unknown side. Supported values: sender, receiver, receivergreen ...";
        }
        assert(false), "returnXsfrToken: Error while retrieving XSFRTOKEN ..."
    }
//---------------------------------------------------------------------------------------------------------------------------------
        static def formatJsonForCurl(String input, log) {
        debugLog("  ====  Calling \"formatJsonForCurl\".", log)
        def intermediate = null;
        assert(input != null),"Error:formatJsonForCurl: input string is null.";
        assert(input.contains("[") && input.contains("]")),"Error:formatJsonForCurl: input string is corrupted.";
        intermediate = input.substring(input.indexOf("[") + 1, input.lastIndexOf("]")).replace("\"", "\"\"\"")
        return "[\"" + intermediate + "\"]";
    }
//---------------------------------------------------------------------------------------------------------------------------------
        static def computePathRessources(String type, String extension, context, log) {
        debugLog("  ====  Calling \"computePathRessources\".", log)
        def returnPath = null;
        if (type.toLowerCase() == "special") {
            returnPath = (context.expand('${#Project#specialPModesPath}') + extension).replace("\\\\", "\\").replace("\\", "\\\\")
        } else {
            returnPath = (context.expand('${#Project#defaultPModesPath}') + extension).replace("\\\\", "\\").replace("\\", "\\\\")
        }
        return returnPath.toString()
    }
//---------------------------------------------------------------------------------------------------------------------------------
        // Run curl command
        static def runCurlCommand(String inputCommand, log) {
        debugLog("  ====  Calling \"runCurlCommand\".", log)
        def proc = null;
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        debugLog("  runCurlCommand  [][]  Run curl command: " + inputCommand, log)
        if (inputCommand) {
            proc = inputCommand.execute()
            if (proc != null) {
                proc.consumeProcessOutput(outputCatcher, errorCatcher)
                proc.waitFor()
            }
        }
        debugLog("  runCurlCommand  [][]  outputCatcher: " + outputCatcher.toString(), log)
        debugLog("  runCurlCommand  [][]  errorCatcher: " + errorCatcher.toString(), log)
        return ([outputCatcher.toString(), errorCatcher.toString()])
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Multitenancy Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
        // Return multitenancy mode
        static def getMultitenancyFromSide(String side, context, log) {
        debugLog("  ====  Calling \"getMultitenancyFromSide\".", log)
        def mode = 0;
        switch (side.toUpperCase()) {
        case "C2":
        case "BLUE":
        case "SENDER":
        case "C2DEFAULT":
            //mode=multitenancyModeC2;
            mode = getMultitenancyMode(context.expand('${#Project#multitenancyModeC2}'), log)
            debugLog("  getMultitenancyFromSide  [][]  mode on domibus $side set to $mode.", log)
            break;
        case "C3":
        case "RED":
        case "RECEIVER":
        case "C3DEFAULT":
            //mode=multitenancyModeC3;
            mode = getMultitenancyMode(context.expand('${#Project#multitenancyModeC3}'), log)
            debugLog("  getMultitenancyFromSide  [][]  mode on domibus $side set to $mode.", log)
            break;
        default:
            log.error "  getMultitenancyFromSide  [][]  ERROR:getMultitenancyFromSide: dominus $side not found.";
        }
        if (mode > 0) {
            return true;
        } else {
            return false;
        }
    }

// Return admin credentials for super user in multidomain configuration and admin user in single domaind situation with domain provided
        static def retriveAdminCredentialsForDomain(context, log, String side, String domainValue, String authUser, authPwd) {
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def multitenancyOn =  null
        multitenancyOn = getMultitenancyFromSide(side, context, log)
        if (multitenancyOn) {
            log.info("  retriveAdminCredentials  [][]  retriveAdminCredentials for Domibus $side and domain $domainValue.")
            debugLog("  retriveAdminCredentials  [][]  First, set domain to $domainValue.", log)
            setDomain(side, context, log, domainValue)
            // If authentication details are not fully provided, use default values
            if ( (authUser == null) || (authPwd == null) ) {
                authenticationUser = SUPER_USER;
                authenticationPwd = SUPER_USER_PWD;
            }
        } else {
            log.info("  retriveAdminCredentials  [][]  retriveAdminCredentials for Domibus $side.")
            // If authentication details are not fully provided, use default values
            if ( (authUser == null) || (authPwd == null) ) {
                authenticationUser = DEFAULT_ADMIN_USER;
                authenticationPwd = DEFAULT_ADMIN_USER_PWD;
            }
        }
        return [authenticationUser, authenticationPwd]
}

// Return admin credentials for super user in multidomain configuration and admin user in single domaind situation without domain provided
    static def retriveAdminCredentials(context, log, String side, String authUser, authPwd) {
    def authenticationUser = authUser
    def authenticationPwd = authPwd
    def multitenancyOn =  null
// If authentication details are not fully provided, use default values
    if ( (authUser == null) || (authPwd == null) ) {
        multitenancyOn = getMultitenancyFromSide(side, context, log)
        if (multitenancyOn) {
            authenticationUser = SUPER_USER;
            authenticationPwd = SUPER_USER_PWD;
        } else {
            authenticationUser = DEFAULT_ADMIN_USER;
            authenticationPwd = DEFAULT_ADMIN_USER_PWD;
        }
    }
    return  [authenticationUser, authenticationPwd]
}

    //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    //  Utilities Functions
    //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Returns: "--TestCase--testStep--"
    static def String locateTest(context) {
    return ("--" + context.testCase.name + "--" + context.testCase.getTestStepAt(context.getCurrentStepIndex()).getLabel() + "--  ")
}
//---------------------------------------------------------------------------------------------------------------------------------
// Copy file from source to destination
static def void copyFile(String source, String destination, log, overwriteOpt=true){
    debugLog("  ====  Calling \"copyFile\".",log)
    // Check that destination folder exists.
    //def destFolder = new File("${destination}")
    //assert destFolder.exists(), "Error while trying to copy file to folder "+destination+": Destination folder doesn't exist.";

    def builder = new AntBuilder()
    try {
        builder.sequential {
            copy(tofile: destination, file:source, overwrite:overwriteOpt)
        }
        log.info "  copyFile  [][]  File ${source} was successfuly copied to ${destination}"
    } catch (Exception ex) {
        log.error "  copyFile  [][]  Error while trying to copy files: " + ex;
        assert 0;
    }
}

//---------------------------------------------------------------------------------------------------------------------------------
// replace slashes in project custom properties values
static def String formatPathSlashes(String source) {
    if ( (source != null) && (source != "") ) {
        return source.replaceAll("/", "\\\\")
    }
}

//---------------------------------------------------------------------------------------------------------------------------------
// Return path to domibus folder
static def String pathToDomibus(color, log, context) {
    debugLog("  ====  Calling \"pathToDomibus\".", log)
    // Return path to domibus folder base on the "color"
    def propName = ""
    switch (color.toLowerCase()) {
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
        assert(false), "Unknown side color. Supported values: BLUE, RED, GREEN"
    }

    return context.expand("\${#Project#${propName}}")
}

//---------------------------------------------------------------------------------------------------------------------------------
// Retrieve sql reference from domain ID if connection is not present try to open it
def retrieveSqlConnectionRefFromDomainId(String domainName) {
    debugLog("  ====  Calling \"retrieveSqlConnectionRefFromDomainId\".", log)
    def domain = retrieveDomainId(domainName)
    openDbConnections([domain])
    assert(dbConnections.containsKey(domain) && dbConnections[domain] != null),"Error: Selecting sql references failed: Null values found."
    return dbConnections[domain]
}

//---------------------------------------------------------------------------------------------------------------------------------
// Return url to specific domibus
static def String urlToDomibus(side, log, context) {
    debugLog("  ====  Calling \"urlToDomibus\".", log)
    // Return url to specific domibus base on the "side"
    def propName = ""
    switch (side.toLowerCase()) {
    case "c2":
    case "blue":
    case "sender":
        propName = "localUrl"
        break;
    case "c3":
    case "red":
    case "receiver":
        propName = "remoteUrl"
        break;
    case "green":
    case "receivergreen":
        propName  = "greenUrl"
        break;
    case "testEnv":
        propName = "testEnvUrl"
        break;
    default:
        assert(false), "Unknown side. Supported values: sender, receiver, receivergreen and testEnv"
    }
    return context.expand("\${#Project#${propName}}")
}
//---------------------------------------------------------------------------------------------------------------------------------
}


