/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.sr.ringo.persistence.jdbc;

import com.google.inject.Inject;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import no.sr.ringo.account.*;
import no.sr.ringo.message.MessageNumber;
import no.sr.ringo.persistence.ObjectMother;
import no.sr.ringo.persistence.guice.PersistenceTestModuleFactory;
import no.sr.ringo.persistence.jdbc.util.DatabaseHelper;
import no.sr.ringo.transport.TransferDirection;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.testng.Assert.*;

/**
 * @author Steinar Overbeck Cook
 *         <p/>
 *         Created by
 *         User: steinar
 *         Date: 31.12.11
 *         Time: 17:24
 */
@Guice(moduleFactory = PersistenceTestModuleFactory.class)
public class AccountRepositoryImplTest {


    Account adamsAccount;
    private AccountRepository accountRepository;
    private Account ringoAccount;
    private ParticipantId participantId;
    private DatabaseHelper databaseHelper;
    private Customer customer;

    @Inject
    public AccountRepositoryImplTest(DatabaseHelper databaseHelper, AccountRepository accountRepository) {
        this.databaseHelper = databaseHelper;
        this.accountRepository = accountRepository;
    }

    @BeforeMethod(groups = {"persistence"})
    public void setUp() throws Exception {
        participantId = ObjectMother.getAdamsParticipantId();
        adamsAccount = ObjectMother.getAdamsAccount();
        adamsAccount = accountRepository.createAccount(adamsAccount, participantId);
        ringoAccount = accountRepository.createAccount(ringoAccount, participantId);

    }

    @AfterMethod(groups = {"persistence"})
    public void tearDown() throws Exception {
        databaseHelper.deleteAllMessagesForAccount(ringoAccount);
        accountRepository.deleteAccount(ringoAccount.getAccountId());
        databaseHelper.deleteCustomer(customer);

        databaseHelper.deleteAllMessagesForAccount(adamsAccount);
        accountRepository.deleteAccount(adamsAccount.getAccountId());
    }

    @Test(groups = {"persistence"})
    public void testFindAccountById() throws Exception {

        Account accountById = accountRepository.findAccountById(ringoAccount.getAccountId());

        assertNotNull(accountById.getAccountId());
        assertNotNull(accountById.getCustomerId());
    }

    @Test(groups = {"persistence"})
    public void testFindAccountByUsername() throws Exception {

        Account accountByUsername = accountRepository.findAccountByUsername(ringoAccount.getUserName());

        assertNotNull(accountByUsername.getAccountId());
        assertNotNull(accountByUsername.getCustomerId());
        assertNotNull(accountByUsername.getUserName());
    }


    @Test(groups = {"persistence"})
    public void testFindAccountByParticipantId() throws Exception {
        Account accountByParticipantId = accountRepository.findAccountByParticipantId(participantId);
        assertNotNull(accountByParticipantId);
        assertNotNull(accountByParticipantId.getAccountId());
        assertNotNull(accountByParticipantId.getCustomerId());
        assertNotNull(accountByParticipantId.getUserName());
    }

    @Test(groups = {"persistence"})
    public void testAccountExists() throws Exception {
        assertTrue(accountRepository.accountExists(new UserName("sr")));
        assertTrue(accountRepository.accountExists(new UserName("SR")));
        assertFalse(accountRepository.accountExists(new UserName("notExistingAccount")));
    }

    @Test(groups = {"persistence"})
    public void testCreateCustomer() {
        customer = accountRepository.createCustomer("adam", "adam@sendregning.no", "666", "Norge", "Andy S", "Adam vei", "222", "0976", "Oslo", "976098897");
        assertNotNull(customer.getId());
        assertNotNull(customer.getCreated());
        assertEquals("adam", customer.getName());
        assertEquals("adam@sendregning.no", customer.getEmail());
        assertEquals("666", customer.getPhone());
        assertEquals("Norge", customer.getCountry());
        assertEquals("Adam vei", customer.getAddress1());
        assertEquals("222", customer.getAddress2());
        assertEquals("0976", customer.getZip());
        assertEquals("Oslo", customer.getCity());
        assertEquals("976098897", customer.getOrgNo());


    }

    @Test(groups = {"persistence"})
    public void testUpdatePasswordOnAccount() throws SrAccountNotFoundException {
        AccountId id = new AccountId(1);
        String currentPass = accountRepository.findAccountById(id).getPassword();
        String pass = "testPassword";

        accountRepository.updatePasswordOnAccount(id, pass);
        assertEquals(pass, accountRepository.findAccountById(id).getPassword());

        accountRepository.updatePasswordOnAccount(id, currentPass);
        assertEquals(currentPass, accountRepository.findAccountById(id).getPassword());

    }

    @Test(groups = {"persistence"})
    public void testValidateFlag() throws SrAccountNotFoundException {
        assertFalse(adamsAccount.isValidateUpload());

        databaseHelper.updateValidateFlagOnAccount(adamsAccount.getAccountId(), true);

        adamsAccount = accountRepository.findAccountById(adamsAccount.getAccountId());
        assertTrue(adamsAccount.isValidateUpload());

    }

    @Test(groups = {"persistence"})
    public void findMessageOwner() {
        Long messageNumber = databaseHelper.createMessage(adamsAccount.getAccountId().toInteger(), TransferDirection.IN, participantId.stringValue(), participantId.stringValue(), UUID.randomUUID().toString(), null, PeppolDocumentTypeIdAcronym.EHF_INVOICE.getDocumentTypeIdentifier(), PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId());
        assertEquals(adamsAccount, accountRepository.findAccountAsOwnerOfMessage(MessageNumber.create(messageNumber)));

    }


}
