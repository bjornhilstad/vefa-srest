/* Created by steinar on 08.01.12 at 21:46 */
package no.sr.ringo.persistence;

import com.google.inject.Inject;
import no.difi.vefa.peppol.common.model.ParticipantIdentifier;
import no.sr.ringo.ObjectMother;
import no.sr.ringo.account.Account;
import no.sr.ringo.account.AccountRepository;
import no.sr.ringo.guice.ServerTestModuleFactory;
import no.sr.ringo.message.MessageMetaData;
import no.sr.ringo.message.PeppolMessageNotFoundException;
import no.sr.ringo.message.PeppolMessageRepository;
import no.sr.ringo.message.ReceptionId;
import no.sr.ringo.persistence.jdbc.util.DatabaseHelper;
import no.sr.ringo.transport.TransferDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Integration test verifying that messages can be filtered by various params
 *
 * @author Adam Mscisz adam@sendregning.no
 */
@Guice(moduleFactory = ServerTestModuleFactory.class)
public class InboxWithMessageWithoutUUIDTest {

    Logger logger = LoggerFactory.getLogger(InboxWithMessageWithoutUUIDTest.class);

    private final AccountRepository accountRepository;
    private final DatabaseHelper databaseHelper;
    private final PeppolMessageRepository peppolMessageRepository;
    private final DataSource dataSource;
    private final DbmsTestHelper dbmsTestHelper;

    private Long messageNo;
    private Long messageNo2;
    private Long messageNo3;
    private String receiver1 = ObjectMother.getAdamsParticipantId().getIdentifier();
    private Account account;
    private ParticipantIdentifier sender;

    @Inject
    public InboxWithMessageWithoutUUIDTest(AccountRepository accountRepository, DatabaseHelper databaseHelper, PeppolMessageRepository peppolMessageRepository, DataSource dataSource, DbmsTestHelper dbmsTestHelper) {
        this.accountRepository = accountRepository;
        this.databaseHelper = databaseHelper;
        this.peppolMessageRepository = peppolMessageRepository;
        this.dataSource = dataSource;
        this.dbmsTestHelper = dbmsTestHelper;
    }

    @BeforeMethod
    public void setUp() throws Exception {
        account = accountRepository.createAccount(ObjectMother.getAdamsAccount(), ObjectMother.getAdamsParticipantId());
        sender = ObjectMother.getAdamsParticipantId();
    }

    /**
     * This test must be run as last one, because it creates new message which would impact other tests
     */
    @Test(groups = {"persistence"})
    public void testMessagesWithNoUUID() throws PeppolMessageNotFoundException, SQLException {

        //proper message
        messageNo = dbmsTestHelper.createSampleMessage(account.getAccountId().toInteger(), TransferDirection.IN, ObjectMother.getAdamsParticipantId().getIdentifier(), receiver1, new ReceptionId(), null);
        //uuid = null
        messageNo2 = dbmsTestHelper.createSampleMessage(account.getAccountId().toInteger(), TransferDirection.IN, ObjectMother.getAdamsParticipantId().getIdentifier(), receiver1, new ReceptionId(), null);
        //uuid = ''
        messageNo3 = dbmsTestHelper.createSampleMessage(account.getAccountId().toInteger(), TransferDirection.IN, ObjectMother.getAdamsParticipantId().getIdentifier(), receiver1, new ReceptionId(), null);

        inspectDbms();
        // We used to expect only a single message as message_uuid was required to be null in order to be deemed in the /inbox
        Integer inboxCount = peppolMessageRepository.getInboxCount(account.getAccountId());
        assertEquals(inboxCount,(Integer) 3);

        //we expect to have only one message
        List<MessageMetaData> undeliveredInboundMessagesByAccount = peppolMessageRepository.findUndeliveredInboundMessagesByAccount(account.getAccountId());
        assertEquals(undeliveredInboundMessagesByAccount.size(),3);

    }

    void inspectDbms() throws SQLException {

        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("select * from MESSAGE");
        ResultSet rs = preparedStatement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();

        while (rs.next()) {
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String columnLabel = metaData.getColumnLabel(i);
                System.out.format("%20s : %s\n", columnLabel, rs.getString(i));
            }
        }
    }


    @AfterMethod
    public void tearDown() throws Exception {
        databaseHelper.deleteAllMessagesForAccount(account);
        accountRepository.deleteAccount(account.getAccountId());
    }

}
