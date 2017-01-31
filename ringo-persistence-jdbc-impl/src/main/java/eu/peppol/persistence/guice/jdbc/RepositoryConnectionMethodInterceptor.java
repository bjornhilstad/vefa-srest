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

package eu.peppol.persistence.guice.jdbc;

import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Looks for all @Repository annotations and makes a database connection available to the
 * methods within the repository.
 *
 * User: andy
 * Date: 8/13/12
 * Time: 2:19 PM
 */
public class RepositoryConnectionMethodInterceptor implements MethodInterceptor {
    static final Logger log = LoggerFactory.getLogger(RepositoryConnectionMethodInterceptor.class);

    @Inject
    JdbcTxManager jdbcTxManager;

    /**
     * Starts a jdbc transaction if a transaction doesnt already exist.
     * Joins the transaction if one exists
     *
     * @param invocation the method invocation joinpoint
     * @return the result of the call to {@link
     *         org.aopalliance.intercept.Joinpoint#proceed()}, might be intercepted by the
     *         interceptor.
     * @throws Throwable if the interceptors or the
     *                   target-object throws an exception.
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        //Ignore the call if the current method is annotated with Transactional
        if (invocation.getMethod().isAnnotationPresent(Transactional.class)) {
            return invocation.proceed();
        }

        //if there is already a connection do nothing as the connection will be cleaned up elsewhere
        if (jdbcTxManager.isConnection()) {
            // just continue
            return invocation.proceed();
        }


        // Commits/rollbacks the transaction
        try {
            jdbcTxManager.newConnection(true);

            return invocation.proceed();
        } catch (Throwable thr) {
            throw thr;
        } finally {
            //Essential that we clean up
            jdbcTxManager.cleanUp();
        }
    }
}
