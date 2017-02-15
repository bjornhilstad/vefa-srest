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

package eu.peppol.persistence.test;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.sql.DataSource;

/**
 * Guice module which will create a new fresh database with schema, in memory
 *
 * @author steinar
 *         Date: 01.12.2016
 *         Time: 12.00
 */
public class InMemoryTestDatabaseModule extends AbstractModule {

    @Override
    protected void configure() {
    }

    @Provides
    DataSource provideH2InMemoryDatabase() {
        return InMemoryDataSourceHelper.createInMemoryDatabase();
    }
}
