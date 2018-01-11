package com.lmig.ci.lmb.herman.rds.credentialbroker.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Created by n0200057 on 3/26/17.
 */
@Component
@ConfigurationProperties(prefix = "datasource")
class DataSourceProperties{
    lateinit var url: String
    lateinit var databaseName: String
    lateinit var username: String
    lateinit var encryptedPassword: String
    lateinit var driverClassName: String
    lateinit var host: String
}