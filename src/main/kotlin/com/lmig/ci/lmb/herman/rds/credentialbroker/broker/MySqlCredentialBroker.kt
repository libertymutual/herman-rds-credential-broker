package com.lmig.ci.lmb.herman.rds.credentialbroker.broker

import com.lmig.ci.lmb.herman.rds.credentialbroker.config.DataSourceProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.sql.DriverManager
import java.util.*

/**
 * Created by n0200057 on 3/26/17.
 */
@Component
@Profile("mysql")
open class MySqlCredentialBroker(
        val template: JdbcTemplate,
        val dataSourceProperties: DataSourceProperties,
        @Qualifier("masterCredential")
        val masterCredential: Credential,
        @Qualifier("appCredential")
        val appCredential: Credential,
        @Qualifier("adminCredential")
        val adminCredential: Credential
) : CredentialBroker {
    val LOG = LoggerFactory.getLogger(MySqlCredentialBroker::class.java)

    override fun brokerCredentials() {
        setUpUser(appCredential)
        grantAppAccess(appCredential)
        testCredential(appCredential)
        
        setUpUser(adminCredential)
        grantAdminAccess(adminCredential)
        
        grantAdminAccess(masterCredential)
        
        testCredential(adminCredential)
    }

    private fun setUpUser(credential: Credential) {
        LOG.info("Setting up user: {}", credential.username)
        dropUserIfExists(credential)
        createUser(credential)
    }

    private fun dropUserIfExists(credential: Credential) {
        val query = String.format("DROP USER IF EXISTS %s;", credential.username)
        template.execute(query)
    }

    private fun createUser(credential: Credential) {
        val query = String.format("CREATE USER '%s' IDENTIFIED BY '%s';", credential.username, credential.password)
        template.execute(query)
    }

    private fun grantAppAccess(credential: Credential) {
        var query = String.format("GRANT SELECT, INSERT, UPDATE, DELETE ON %s.* TO '%s'@'%%' IDENTIFIED BY '%s';", dataSourceProperties.databaseName, credential.username, credential.password)
        template.execute(query)
        
        query = "FLUSH PRIVILEGES;"
        template.execute(query)

    }

    private fun grantAdminAccess(credential: Credential) {
        var query = String.format("GRANT ALL ON %s.* TO '%s'@'%%' IDENTIFIED BY '%s';", dataSourceProperties.databaseName, credential.username, credential.password)
        template.execute(query)

        query = "FLUSH PRIVILEGES;"
        template.execute(query)
    }

    private fun testCredential(credential: Credential) {
        LOG.info("Testing login for DB user {}", credential.username)
        val properties = Properties()
        properties.put("user", credential.username)
        properties.put("password", credential.password)
        val url = template.dataSource.connection.metaData.url
        LOG.info("Connecting to {}", url)
        DriverManager.getConnection(url, properties)
    }
}
