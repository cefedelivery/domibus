package eu.domibus.taxud;

import eu.domibus.plugin.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class MessageAccessPointSwitch {
    private final static Logger LOG = LoggerFactory.getLogger(MessageAccessPointSwitch.class);

    public  void switchAccessPoint(Submission submission) {
        Set<Submission.Party> fromParties = new HashSet<>(submission.getFromParties());
        Set<Submission.Party> toParties = new HashSet<>(submission.getToParties());

       // LOG.info("switching access point from :");
        for (Submission.Party party : submission.getFromParties()) {
         //   LOG.info("From [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }
        for (Submission.Party party : submission.getToParties()) {
          //  LOG.info("to [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }

        submission.getFromParties().clear();
        submission.getToParties().clear();

        submission.getFromParties().addAll(toParties);
        submission.getToParties().addAll(fromParties);

        //LOG.info("to:");
        for (Submission.Party party : submission.getFromParties()) {
          //  LOG.info("From [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }
        for (Submission.Party party : submission.getToParties()) {
           // LOG.info("to [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }

        String fromRole = submission.getFromRole();
        String toRole = submission.getToRole();

        submission.setFromRole(toRole);
        submission.setToRole(fromRole);
    }
}
