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

package eu.peppol.persistence.jdbc;

import com.google.inject.Inject;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.persistence.AccountId;
import eu.peppol.persistence.MessageNumber;
import eu.peppol.persistence.api.SrAccountNotFoundException;
import eu.peppol.persistence.api.UserName;
import eu.peppol.persistence.api.account.Account;
import eu.peppol.persistence.api.account.AccountRepository;
import eu.peppol.persistence.api.account.Customer;
import eu.peppol.persistence.guice.jdbc.JdbcTxManager;
import eu.peppol.persistence.guice.jdbc.Repository;
import eu.peppol.persistence.guice.jdbc.Transactional;

import java.sql.*;

/**
 * @author Steinar Overbeck Cook
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {


    private static final String CLIENT_ROLE = "client";
    private final JdbcTxManager jdbcTxManager;

    @Inject
    public AccountRepositoryImpl(JdbcTxManager jdbcTxManager) {
        this.jdbcTxManager = jdbcTxManager;
    }

    @Override
    public Account findAccountById(final AccountId id) throws SrAccountNotFoundException {
        Account account = findAccountWithWhereClause("a.id=?", new String[]{id.toString()});
        if (account == null) {
            throw new SrAccountNotFoundException(id);
        }

        return account;
    }

    @Override
    public Customer findCustomerById(final Integer id) {

        try {
            Connection con = jdbcTxManager.getConnection();
            final String sql = "select * from  customer where id = ?";
            final PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                //Integer originatorId = rs.getInt("originator_id");
                Date createTs = rs.getDate("created_ts");
                String address1 = rs.getString("address1");
                String address2 = rs.getString("address2");
                String zip = rs.getString("zip");
                String city = rs.getString("city");
                String country = rs.getString("country");
                String contactPerson = rs.getString("contact_person");
                String email = rs.getString("contact_email");
                String phone = rs.getString("contact_phone");
                String orgNo = rs.getString("org_no");

                Customer customer = new Customer(id, name, createTs, contactPerson, email, phone, country, address1, address2, zip, city, orgNo);

                return customer;
            }

            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("Error locating customer with id " + id + "; " + e, e);
        }
    }

    @Override
    public void updatePasswordOnAccount(final AccountId id, final String hash) {
        try {
            final Connection con = jdbcTxManager.getConnection();
            String sql = "update account set password = ? where id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(2, id.toInteger());
            ps.setString(1, hash);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

    }

    @Override
    public Account findAccountAsOwnerOfMessage(MessageNumber messageNumber) {
        if (messageNumber == null) {
            return null;
        }
        Account account = findAccountWithWhereClause("a.id = (select account_id from message m where m.msg_no = ?)", new String[]{messageNumber.getValue()});
        return account;
    }

    @Override
    public Account findAccountByParticipantId(final ParticipantId participantId) {
        if (participantId == null) {
            return null;
        }
        Account account = findAccountWithWhereClause("a.id = (select account_id from account_receiver ac where ac.participant_id =?)", new String[]{participantId.stringValue()});
        return account;
    }

    @Override
    public Account findAccountByUsername(final UserName username) throws SrAccountNotFoundException {
        Account account = findAccountWithWhereClause("a.username=?", new String[]{username.stringValue()});
        if (account == null) {
            throw new SrAccountNotFoundException(username);
        }

        return account;
    }

    @Override
    @Transactional
    public Account createAccount(final Account account, final ParticipantId participantId) {
        Account ringoAccount = findAccountByParticipantId(participantId);

        if (ringoAccount != null) {
            return ringoAccount;
        }

        final Connection con = jdbcTxManager.getConnection();
        Account result = null;

        try {
            //create test account                   1         2       3        4
            String sql = "insert into account (customer_id, name, username, password) values (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, account.getCustomerId().toInteger());
            ps.setString(2, account.getName());
            ps.setString(3, account.getUserName().stringValue());
            ps.setString(4, account.getPassword());

            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                try {
                    AccountId id = new AccountId(rs.getInt(1));
                    result = findAccountById(id);
                } catch (SrAccountNotFoundException e) {
                    throw new IllegalStateException("Unable to find account by Id after creating it. username=" + account.getUserName().stringValue());
                }
            } else {
                throw new IllegalStateException("Unable to obtain generated key after insert.");
            }

            //create the account_role entry
            sql = "insert into account_role(username, role_name) values (?, ?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, account.getUserName().stringValue());
            ps.setString(2, CLIENT_ROLE);

            ps.execute();

            //create the account_receiver if participantId is not null
            if (participantId != null) {
                sql = "insert into account_receiver (account_id, participant_id) values (?, ?)";
                ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setInt(1, result.getAccountId().toInteger());
                ps.setString(2, participantId.stringValue());
                ps.execute();

            }


        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Customer createCustomer(final String name, final String email, final String phone, final String country, final String contactPerson, final String address1, final String address2, final String zip, final String city, final String orgNo) {
        final Connection con = jdbcTxManager.getConnection();
        Customer result = null;
        if (name == null) {
            throw new IllegalArgumentException("Name required when creating new customer");
        }

        try {

            //create test account                   1     2       3       4    5        6        7             8             9              10
            String sql = "insert into customer (name, address1,address2, zip, city, country, contact_person, contact_email, contact_phone, org_no) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, address1);
            ps.setString(3, address2);
            ps.setString(4, zip);
            ps.setString(5, city);
            ps.setString(6, country);
            ps.setString(7, contactPerson);
            ps.setString(8, email);
            ps.setString(9, phone);
            ps.setString(10, orgNo);

            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                result = findCustomerById(rs.getInt(1));
            } else {
                throw new IllegalStateException("Unable to obtain generated key after insert.");
            }


        } catch (SQLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public void deleteAccount(final AccountId accountId) {
        final Connection con = jdbcTxManager.getConnection();
        if (accountId == null) {
            return;
        }

        String sql = "delete from account_role where username = (select username from account where id= ?)";

        try {

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accountId.toInteger());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(String.format("%s failed with accountId: %s", sql, accountId), e);
        }


        sql = "delete from account where id= ?";

        try {

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accountId.toInteger());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(String.format("%s failed with accountId: %s", sql, accountId), e);
        }
    }

    @Override
    public boolean accountExists(final UserName username) {
        if (username == null) {
            return false;
        }

        final Connection con = jdbcTxManager.getConnection();
        String sql = "select count(*) from account where username like ?";
        try {

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username.stringValue());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(String.format("%s failed with username: %s", sql, username.stringValue()), e);
        }
    }

    Account findAccountWithWhereClause(final String whereClause, final String[] parameters) {

        final String sql = "select a.*, c.id AS customer_id, c.name as customer_name, c.created_ts c_ts, c.contact_email, c.contact_phone, c.address1, c.address2, c.zip, c.city, c.contact_person, c.country, c.org_no from account a join customer c on a.customer_id = c.id where " + whereClause;
        try {
            final Connection con = jdbcTxManager.getConnection();
            final PreparedStatement ps = con.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                ps.setString(i + 1, parameters[i]);
            }

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                final AccountId accountId = AccountId.valueOf(rs.getString("id"));
                final String password = rs.getString("password");
                final Date account_created_ts = new Date(rs.getTimestamp("created_ts").getTime());
                final String accountName = rs.getString("name");

                final UserName username = new UserName(rs.getString("username"));

                final int customer_id = rs.getInt("customer_id");
                final String customer_name = rs.getString("customer_name");
                final Date customer_created_ts = new Date(rs.getTimestamp("c_ts").getTime());
                final String email = rs.getString("contact_email");
                final String phone = rs.getString("contact_phone");
                final String city = rs.getString("city");
                final String country = rs.getString("country");
                final String contactPerson = rs.getString("contact_person");
                final String address1 = rs.getString("address1");
                final String address2 = rs.getString("address2");
                final String zip = rs.getString("zip");
                final String orgNo = rs.getString("org_no");
                final boolean account_validateUpload = rs.getBoolean("validate_upload");
                final boolean account_sendNotification = rs.getBoolean("send_notification");

                Customer customer = new Customer(customer_id, customer_name, customer_created_ts, contactPerson, email, phone, country, address1, address2, zip, city, orgNo);

                Account account = new Account(customer.getCustomerId(), accountName, username, account_created_ts, password, accountId, account_validateUpload, account_sendNotification);

                return account;
            }

            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("Error locating account with using: " + sql + "; " + e, e);
        }
    }
}
