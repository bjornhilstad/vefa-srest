package no.sr.ringo.client;

import com.google.inject.Inject;
import no.sr.ringo.ObjectMother;
import no.sr.ringo.account.Account;
import no.sr.ringo.account.AccountRepository;
import no.sr.ringo.guice.ServerTestModuleFactory;
import no.sr.ringo.http.AbstractHttpClientServerTest;
import no.sr.ringo.message.PeppolMessageRepository;
import no.sr.ringo.message.ReceptionId;
import no.sr.ringo.persistence.DbmsTestHelper;
import no.sr.ringo.persistence.jdbc.util.DatabaseHelper;
import no.sr.ringo.transport.TransferDirection;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: adam
 * Date: 1/24/12
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Guice(moduleFactory = ServerTestModuleFactory.class)
public class MarkAsReadIntegrationTest extends AbstractHttpClientServerTest {

    /**
     * This class cannot extend DatabaseHelper to have access to helper methods as it
     * already extends different test....
     */

    @Inject
    AccountRepository accountRepository;
    
    @Inject
    PeppolMessageRepository peppolMessageRepository;

    @Inject
    DatabaseHelper databaseHelper;

    @Inject
    DbmsTestHelper dbmsTestHelper;

    private Account account;

    private String receiver1 = "9908:976098897";

    @BeforeTest(groups = {"persistence"})
    public void setUp() throws Exception {
        account = accountRepository.findAccountByParticipantIdentifier(ObjectMother.getTestParticipantId());
        dbmsTestHelper.createSampleMessage(account.getAccountId().toInteger(), TransferDirection.IN, ObjectMother.getTestParticipantIdForSMPLookup().getIdentifier(), receiver1, new ReceptionId(), new Date());
    }

    @AfterTest(groups = {"persistence"})
    public void cleanUp() throws Exception {
    }

    /**
     * Tests downloading of messages from the inbox and marking them all as read.
     */
    @Test(groups = {"persistence"})
    public void testMarkAsRead(){

        //fetch the inbox
        Inbox inbox = ringoRestClientImpl.getInbox();
        //get all the messages
        //mark all the messages as read
        while (inbox.getCount() > 0) {
            Messages messages = inbox.getMessages();
            for (Message message : messages) {
                assertTrue(message.markAsRead());
            }
        }
        // get the messages again
        Messages messages = inbox.getMessages();

        //there should be no messages
        assertTrue(messages.isEmpty());
        assertFalse(messages.iterator().hasNext());
    }

}
