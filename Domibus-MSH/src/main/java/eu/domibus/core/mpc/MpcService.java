package eu.domibus.core.mpc;

/**
 * User Messages submitted with the optional attribute mpc filled in
 * could be put in READY_TO_PULL state if the configuration property DOMIBUS_PULL_FORCE_BY_MPC is
 * enabled and the mpc value includes the initiator name.
 *
 * e.g. mpc="urn:fdc:ec.europa.eu:2019:eu_ics2_c2t/EORI/BE1234567890"
 * baseMpc=urn:fdc:ec.europa.eu:2019:eu_ics2_c2t
 * separator=EORI
 * initiator_name=BE1234567890
 *
 * @author idragusa
 * @since 4.1
 *
 */
public interface MpcService {


    /**
     * Verify the conditions for pull: DOMIBUS_PULL_FORCE_BY_MPC enabled and the
     * mpc contains the initiator party
     *
     * @param mpc the full qualified mpc
     * @return true when pull is enforced
     */
    boolean forcePullOnMpc(String mpc);

    /**
     * This method extracts the initiator name (e.g BE1234567890)
     *
     * @param mpc the full qualified mpc
     * @return the initiator name
     */
    String extractInitiator(String mpc);

    /**
     * This method extracts the base mpc (e.g urn:fdc:ec.europa.eu:2019:eu_ics2_c2t)
     *
     * @param mpc the full qualified mpc
     * @return the base mpc
     */
    String extractBaseMpc(String mpc);
}
