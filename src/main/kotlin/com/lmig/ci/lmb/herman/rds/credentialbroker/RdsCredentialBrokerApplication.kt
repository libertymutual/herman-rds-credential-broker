package com.lmig.ci.lmb.herman.rds.credentialbroker;

import com.lmig.ci.lmb.herman.rds.credentialbroker.broker.CredentialBroker
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties
open class RdsCredentialBrokerApplication(
		val credentialBroker: CredentialBroker
) : CommandLineRunner {
	override fun run(vararg args: String?) {
		credentialBroker.brokerCredentials()
	}
}

fun main(args: Array<String>) {
	SpringApplication.run(RdsCredentialBrokerApplication::class.java, *args)
}
