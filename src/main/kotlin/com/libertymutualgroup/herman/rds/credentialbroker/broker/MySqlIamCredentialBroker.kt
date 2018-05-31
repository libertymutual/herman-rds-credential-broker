package com.libertymutualgroup.herman.rds.credentialbroker.broker

import com.libertymutualgroup.herman.rds.credentialbroker.config.DataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component


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
@Profile("mysqliam")
open class MySqlIamCredentialBroker(
        private val template: JdbcTemplate,
        private val dataSourceProperties: DataSourceProperties,
        @Value("\${aws.region}")
        val region: String,
        @Qualifier("appCredential")
        private val appCredential: Credential,
        @Qualifier("adminCredential")
        private val adminCredential: Credential
) : CredentialBroker {
    private val LOG = LoggerFactory.getLogger(MySqlIamCredentialBroker::class.java)

    override fun brokerCredentials() {
        if(!credentialExists(appCredential)) {
            setUpUser(appCredential)
            grantAppAccess(appCredential)
            testCredential(appCredential)
        }

        if(!credentialExists(adminCredential)) {
            setUpUser(adminCredential)
            grantAdminAccess(adminCredential)
            testCredential(adminCredential)
        }
    }

    private fun dropUserIfExists(credential: Credential) {
        val query = String.format("DROP USER IF EXISTS %s;", credential.username)
        template.execute(query)
    }

    private fun setUpUser(credential: Credential) {
        LOG.info("Setting up IAM user: {}", credential.username)
        createUser(credential)
    }

    private fun credentialExists(credential: Credential): Boolean{
        val query = "SELECT 1 FROM mysql.user WHERE user = '${credential.username}'"

        return template.queryForList(query).size == 1
    }

    private fun createUser(credential: Credential) {
        dropUserIfExists(credential)
        val query = String.format("CREATE USER '%s' IDENTIFIED WITH AWSAuthenticationPlugin as 'RDS';", credential.username)
        template.execute(query)
    }

    private fun grantAppAccess(credential: Credential) {
        var query = String.format("GRANT SELECT, INSERT, UPDATE, DELETE ON %s.* TO '%s'@'%%';", dataSourceProperties.databaseName, credential.username)
        template.execute(query)

        query = "FLUSH PRIVILEGES;"
        template.execute(query)

    }

    private fun grantAdminAccess(credential: Credential) {
        var query = String.format("GRANT ALL ON %s.* TO '%s'@'%%';", dataSourceProperties.databaseName, credential.username)
        template.execute(query)

        query = "FLUSH PRIVILEGES;"
        template.execute(query)
    }

    private fun testCredential(credential: Credential): Boolean {
        return true
    }
}