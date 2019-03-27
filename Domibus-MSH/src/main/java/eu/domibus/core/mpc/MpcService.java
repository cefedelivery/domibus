package eu.domibus.core.mpc;

/**
 * Created by idragusa on 3/27/19.
 */
public interface MpcService {


    /**
     * User Messages that include the optional attribute mpc are meant for PULL if
     * the configuration property is enabled and the mpc includes the initiator party
     * This method verifies the conditions.
     *
     * @param mpc the full qualified mpc
     * @return true when pull is enforced
     */
    boolean forcePullOnMpc(String mpc);

    /**
     * User Messages that include the optional attribute mpc are meant for PULL if
     * the configuration property is enabled and the mpc includes the initiator party
     * e.g. urn:fdc:ec.europa.eu:2019:eu_ics2_c2t/EORI/BE1234567890
     * This method extracts the initiator (e.g BE1234567890)
     *
     * @param mpc the full qualified mpc
     * @return the initiator name
     */
    String extractInitiator(String mpc);

    /**
     * User Messages that include the optional attribute mpc are meant for PULL if
     * the configuration property is enabled and the mpc includes the initiator party.
     * e.g. urn:fdc:ec.europa.eu:2019:eu_ics2_c2t/EORI/BE1234567890
     * This method extracts the base mpc (e.g urn:fdc:ec.europa.eu:2019:eu_ics2_c2t)
     *
     * @param mpc the full qualified mpc
     * @return the base mpc
     */
    String extractBaseMpc(String mpc);
}
