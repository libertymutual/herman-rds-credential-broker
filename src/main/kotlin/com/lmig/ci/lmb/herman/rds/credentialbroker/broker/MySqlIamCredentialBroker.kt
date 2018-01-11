package com.lmig.ci.lmb.herman.rds.credentialbroker.broker

import com.lmig.ci.lmb.herman.rds.credentialbroker.config.DataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component


/**
 * Created by n0200057 on 3/26/17.
 */
@Component
@Profile("mysqliam")
open class MySqlIamCredentialBroker(
        val template: JdbcTemplate,
        val dataSourceProperties: DataSourceProperties,
        @Value("\${aws.region}")
        val region: String,
        @Qualifier("appCredential")
        val appCredential: Credential,
        @Qualifier("adminCredential")
        val adminCredential: Credential
) : CredentialBroker {
    val LOG = LoggerFactory.getLogger(MySqlIamCredentialBroker::class.java)

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
        var query = "SELECT 1 FROM mysql.user WHERE user = '${credential.username}'"

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