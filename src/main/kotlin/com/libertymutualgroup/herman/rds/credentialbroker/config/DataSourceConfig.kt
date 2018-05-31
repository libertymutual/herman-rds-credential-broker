package com.libertymutualgroup.herman.rds.credentialbroker.config

import com.libertymutualgroup.herman.rds.credentialbroker.broker.Credential
import de.zalando.spring.cloud.config.aws.kms.KmsTextEncryptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.jdbc.datasource.DriverManagerDataSource

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
        val ds = DriverManagerDataSource(dataSourceProperties.url, credential.username, credential.password)
        ds.setDriverClassName(dataSourceProperties.driverClassName)

        return ds
    }
}