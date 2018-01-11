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
@Profile("postgres")
class PostgresCredentialBroker(
        val template: JdbcTemplate,
        val dataSourceProperties: DataSourceProperties,
        @Qualifier("masterCredential")
        val masterCredential: Credential,
        @Qualifier("appCredential")
        val appCredential: Credential,
        @Qualifier("adminCredential")
        val adminCredential: Credential
): CredentialBroker {
    val LOG = LoggerFactory.getLogger(PostgresCredentialBroker::class.java)

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
            LOG.info("Updating password for existing DB user ${appCredential.username} for admin use")
            updateCredential(adminCredential)
        }
        else{
            LOG.info("Creating DB user ${appCredential.username} for administration to use")
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
        connectionProps.put("user", credential.username)
        connectionProps.put("password", credential.password)
        val connectionString = template.dataSource.connection.metaData.url
        LOG.info("Connecting to " + connectionString)
        DriverManager.getConnection(connectionString, connectionProps)
    }
}
