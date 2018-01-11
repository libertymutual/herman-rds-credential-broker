package com.lmig.ci.lmb.herman.rds.credentialbroker.config

import com.lmig.ci.lmb.herman.rds.credentialbroker.broker.Credential
import de.zalando.spring.cloud.config.aws.kms.KmsTextEncryptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DriverManagerDataSource

/**
 * Created by n0200057 on 3/26/17.
 */
@Configuration
open class DataSourceConfig {

    /* LOCAL CONFIG */
    @Bean
    @Qualifier("masterCredential")
    @Profile("local")
    open fun masterCredentialLocal(credentialProperties: CredentialProperties): Credential {
        return Credential(
                username = credentialProperties.masterUsername,
                password = credentialProperties.masterEncryptedPassword
        )
    }

    @Bean
    @Qualifier("appCredential")
    @Profile("local")
    open fun appCredentialLocal(credentialProperties: CredentialProperties): Credential {
        return Credential(
            username = credentialProperties.appUsername,
            password = credentialProperties.appEncryptedPassword
        )
    }

    @Bean
    @Qualifier("adminCredential")
    @Profile("local")
    open fun adminCredentialLocal(credentialProperties: CredentialProperties): Credential {
        return Credential(
                username = credentialProperties.adminUsername,
                password = credentialProperties.adminEncryptedPassword
        )
    }

    /* NON-LOCAL CONFIG */
    @Bean
    @Qualifier("masterCredential")
    @Profile("!local")
    open fun masterCredentialNonLocal(credentialProperties: CredentialProperties, decryptor: KmsTextEncryptor): Credential {
        return Credential(
                username = credentialProperties.masterUsername,
                password = decryptor.decrypt(credentialProperties.masterEncryptedPassword)
        )
    }

    @Bean
    @Qualifier("appCredential")
    @Profile("!local")
    open fun appCredentialNonLocal(credentialProperties: CredentialProperties, decryptor: KmsTextEncryptor): Credential {
        return Credential(
            username = credentialProperties.appUsername,
            password = decryptor.decrypt(credentialProperties.appEncryptedPassword)
        )
    }

    @Bean
    @Qualifier("adminCredential")
    @Profile("!local")
    open fun adminCredentialNonLocal(credentialProperties: CredentialProperties, decryptor: KmsTextEncryptor): Credential {
        return Credential(
            username = credentialProperties.adminUsername,
            password = decryptor.decrypt(credentialProperties.adminEncryptedPassword)
        )
    }

    @Bean
    @Profile("postgres", "mysql", "mysqliam")
    open fun dataSource(dataSourceProperties: DataSourceProperties, @Qualifier("masterCredential") credential: Credential) : DriverManagerDataSource {
        var ds = DriverManagerDataSource(dataSourceProperties.url, credential.username, credential.password)
        ds.setDriverClassName(dataSourceProperties.driverClassName)

        return ds
    }
}