# Test Plan - DRAFT #

## Test Scenarios ##

### 2.3. File System Submission ###

The default File System plugin allows to easily exchange files between an Access Points by sending and receiving files 
created and consumed from the Access Point's local file system.
The sending Access Point will observe the configured file system folder OUT for a dropped file, construct an AS4 message 
and send it to the receiving Access Point where it will be available in the IN folder.

**Precondition**

The configuration of the sending and receiving Access Point is done according to the instructions in the Quick Start 
Guide.

**Assumption**

The File System Plugin is up and running in both sending and receiving Access Points. The system user that runs the 
Domibus has file system privileges to read and write files in the configured File System Plugin folders.

**Test Steps**
1. [domibus-blue] Step “submitFile” creates file in the FS Plugin observed folder “fs_plugin_data/OUT”. The file is a 
simple text file named “fs_plugin_test_file.txt”.

1. [domibus-blue] In the background, the Access Point running at localUrl detects the created file from step 1 as input 
to construct an AS4 message and send it to the Access Point that is running at remoteUrl.

1. [domibus-blue] Once a message is sent by the FS Plugin to Domibus the initial message (text file) will be renamed to 
message_fs_plugin_test_file_messageID.txt (where messageID is the generated message id by Domibus). This convention is 
used in order to be able to associate the message file with the User Message created in Domibus.

1. [domibus-blue] Each time the message status is changed in Domibus the FS Plugin will rename the message file so that 
it includes the message status. The file will be renamed to message_fs_plugin_test_file_messageID.txt.READY_TO_SEND, 
(*.SENDING) and finally (*.SENT)

1. [domibus-blue] The successfully sent message will be moved to the “SENT” folder according to the default FS Plugin 
configuration.

1. [domibus-red] In the background, the Access Point that is running at remoteUrl will receive the message that is sent 
in step 2. Upon reception, the message will be consumed by the File System Plugin and the contained file will be created 
in the “fs_plugin_data/IN”.
