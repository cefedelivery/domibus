
package eu.domibus.common;

import java.util.Date;


/**
 * This interface describes Objects representing ebMS3 error messages. those could i.e. be exceptions,
 * database entities or xml elements.
 *
 * @author Christian Koch, Stefan Mueller
 */
public interface ErrorResult {

    /**
     * @return MSHRole.SENDING if the related message was outgiong.
     * MSHRole.RECIEVING if the related message was incoming
     */
    MSHRole getMshRole();

    /**
     *
     * @return the internal ID of the erroneous message
     */
    String getMessageInErrorId();

    /**
     *
     * @return the ebMS3 error code
     */
    ErrorCode getErrorCode();

    /**
     *
     * @return the error detail message as produced by the MSH generating the error
     */
    String getErrorDetail();


    /**
     *
     * @return the time when this error was generated/received
     */
    Date getTimestamp();

    /**
     *
     * @return the time the corresponding backend plugin was made aware of this error or null
     * if there was no corresponding backend found or the corresponding PMode is set to not
     * forward error messages. This is controlled by PMode[1].errorHandling.Report.ProcessErrorNotifyConsumer
     * and PMode[1].errorHandling.Report.ProcessErrorNotifyProducer respectively
     */
    Date getNotified();
}


