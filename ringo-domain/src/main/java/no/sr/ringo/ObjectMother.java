package no.sr.ringo;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeId;
import eu.peppol.persistence.api.UserName;
import eu.peppol.persistence.api.account.Account;
import eu.peppol.persistence.api.account.AccountId;
import eu.peppol.persistence.api.account.Customer;

import java.util.Date;

/**
 * Object which shall be used to create complex objects for testing.
 *
 * @author andy
 * @author adam
 * @author thore
 */
public class ObjectMother {

    public static Account getTestAccount(){
        return new Account(
                new Customer(1, "Andy", new Date(), "Andy Swift", "andy@sendregning.no", "091289273432", "Norge", "Adam vei", "22", "0976", "Oslo", "976098897"),
                new UserName("sr"), new Date(), getTestPassword(), new AccountId(1), false, true);
    }

    public static Account getAdamsAccount() {
        return new Account(
                new Customer(1, "Adam",new Date(), "Adam Mscisz", "adam@sendregning.no", "1111111111", "Norge", "Adam vei", "22", "0976", "Oslo", "976098897"),
                new UserName("adam"), new Date(), getTestPassword(), new AccountId(2), false, true);
    }

    public static Account getThoresAccount() {
        return new Account(
                new Customer(1, "Thore",new Date(), "Thore Johnsen", "thore@sendregning.no", "04791375276", "Norge", "Motorvei", "22", "0494", "Oslo", "976098897"),
                new UserName("teedjay"), new Date(), getTestPassword(), new AccountId(3), false, true);
    }

    private static String getTestPassword() {
        return "ringo";
    }

    public static ParticipantId getTestParticipantIdForSMPLookup() {
        return new ParticipantId(RingoConstant.PEPPOL_PARTICIPANT_PREFIX+RingoConstant.DIFI_ORG_NO);
    }

    public static ParticipantId getTestParticipantIdForConsumerReceiver() {
        return new ParticipantId("9999:01029400470");
    }

    public static ParticipantId getTestParticipantId() {
        return new ParticipantId(RingoConstant.PEPPOL_PARTICIPANT_PREFIX+RingoConstant.SR_ORG_NO);
    }

    public static ParticipantId getAdamsParticipantId() {
        return new ParticipantId(RingoConstant.PEPPOL_PARTICIPANT_PREFIX+"988890081");
    }

    public static final PeppolDocumentTypeId getDocumentIdForBisInvoice() {
        return PeppolDocumentTypeId.valueOf("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice##urn:www.cenbii.eu:transaction:biitrns010:ver2.0:extended:urn:www.peppol.eu:bis:peppol4a:ver2.0::2.1");
    }

}
