package com.libertymutualgroup.herman.rds.credentialbroker.broker

import com.libertymutualgroup.herman.rds.credentialbroker.config.DataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.DriverManager
import java.util.Properties

/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Component
@Profile("postgres")
class PostgresCredentialBroker(
        private val template: JdbcTemplate,
        private val dataSourceProperties: DataSourceProperties,
        @Qualifier("masterCredential")
        private val masterCredential: Credential,
        @Qualifier("appCredential")
        private val appCredential: Credential,
        @Qualifier("adminCredential")
        private val adminCredential: Credential
): CredentialBroker {
    private val LOG = LoggerFactory.getLogger(PostgresCredentialBroker::class.java)

    override fun brokerCredentials(){
        initializePermissions()

        if(credentialExists(appCredential)){
            LOG.info("Updating password for existing DB user ${appCredential.username} for application use")
            updateCredential(appCredential)
        }
        else{
            LOG.info("Creating DB user ${appCredential.username} for application to use")
            createAppUser()
        }

        testCredential(appCredential)

        if(credentialExists(adminCredential)){
            LOG.info("Updating password for existing DB user ${adminCredential.username} for admin use")
            updateCredential(adminCredential)
        }
        else{
            LOG.info("Creating DB user ${adminCredential.username} for administration to use")
            createAdminUser()
        }

        testCredential(adminCredential)
    }

    private fun initializePermissions(){
        var query = "revoke create on schema public from public"
        template.execute(query)
    }

    private fun credentialExists(credential: Credential): Boolean{
        var query = "select 1 from pg_roles where rolname='${credential.username}'"

        return template.queryForList(query).size == 1
    }

    private fun updateCredential(credential: Credential) {
        var query = "alter user \"${credential.username}\" with encrypted password '${credential.password}'"
        template.execute(query)
    }

    private fun createAppUser(){
        var query = "create role \"${appCredential.username}\" login encrypted password '${appCredential.password}'"
        template.execute(query)

        query = "grant connect on database \"${dataSourceProperties.databaseName}\" to \"${appCredential.username}\""
        template.execute(query)

        query = "grant select, insert, update, delete on all tables in schema public to \"${appCredential.username}\""
        template.execute(query)
    }

    private fun createAdminUser(){
        var query = "create role \"${adminCredential.username}\" login encrypted password '${adminCredential.password}'"
        template.execute(query)

        query = "grant connect on database \"${dataSourceProperties.databaseName}\" to \"${adminCredential.username}\""
        template.execute(query)

        query = "grant create, usage on schema public to \"${adminCredential.username}\""
        template.execute(query)

        query = "grant select, insert, update, delete on all tables in schema public to \"${adminCredential.username}\""
        template.execute(query)

        query = "grant \"${adminCredential.username}\" to \"${masterCredential.username}\""
        template.execute(query)
    }

    private fun testCredential(credential: Credential) {
        LOG.info("Testing login for DB user ${credential.username}")
        val connectionProps = Properties()
        connectionProps["user"] = credential.username
        connectionProps["password"] = credential.password
        val connectionString = template.dataSource.connection.metaData.url
        LOG.info("Connecting to $connectionString")
        DriverManager.getConnection(connectionString, connectionProps)
    }
}
