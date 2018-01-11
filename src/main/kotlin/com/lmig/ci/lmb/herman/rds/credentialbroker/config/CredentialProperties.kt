package com.lmig.ci.lmb.herman.rds.credentialbroker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

/**
 * Created by n0200057 on 3/26/17.
 */
@Component
@ConfigurationProperties(prefix = "credentials")
class CredentialProperties {
    lateinit var masterUsername: String
    lateinit var masterEncryptedPassword: String
    lateinit var appUsername: String
    lateinit var appEncryptedPassword: String
    lateinit var adminUsername: String
    lateinit var adminEncryptedPassword: String
}


