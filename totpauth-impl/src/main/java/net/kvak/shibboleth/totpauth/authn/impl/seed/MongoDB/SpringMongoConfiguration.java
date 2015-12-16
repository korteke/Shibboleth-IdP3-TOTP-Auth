package net.kvak.shibboleth.totpauth.authn.impl.seed.MongoDB;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories(basePackages = "net.kvak.shibboleth.totpauth.authn.impl.seed.MongoDB")
public class SpringMongoConfiguration extends AbstractMongoConfiguration {
	
	/* Class logger */
	private final Logger log = LoggerFactory.getLogger(SpringMongoConfiguration.class);
	
	@Nonnull
	private String dbName;
	
	@Nonnull
	private String dbUrl;

	public SpringMongoConfiguration(String dbName, String dbUrl) {
		log.debug("Initializing mongo configuration with {} - {}", dbName, dbUrl);
		this.dbName = dbName;
		this.dbUrl = dbUrl;
	}

	@Override
	protected String getDatabaseName() {
		return dbName;
	}

	@Override
	@Bean
	public Mongo mongo() throws Exception {
		return new MongoClient(dbUrl);
	}

}
