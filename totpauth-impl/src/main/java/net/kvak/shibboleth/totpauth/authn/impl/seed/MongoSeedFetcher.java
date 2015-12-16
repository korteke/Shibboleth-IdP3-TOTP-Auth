package net.kvak.shibboleth.totpauth.authn.impl.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.kvak.shibboleth.totpauth.authn.impl.seed.MongoDB.MongoRepo;
import net.kvak.shibboleth.totpauth.authn.impl.seed.MongoDB.MongoUser;

@EnableMongoRepositories(basePackages = "net.kvak.shibboleth.totpauth.authn.impl.seed.MongoDB")
public class MongoSeedFetcher implements SeedFetcher {

	/* TODO Implement this whole thing */

	/* Class logger */
	private final Logger log = LoggerFactory.getLogger(MongoSeedFetcher.class);

	@Autowired
	MongoRepo repository;

	public MongoSeedFetcher() {
	}

	@Override
	public void getSeed(String username, TokenUserContext tokenUserCtx) {
		log.debug("Entering MongoDB getSeed with user {}", username);

		//repository.save(new MongoUser("johnd", "G24YUKCHHXRDWCPR"));
		//log.debug("Added johnd to db");

		log.debug("DB contains {} user with name {}", repository.countByUserName(username), username);
		
		if (repository.countByUserName(username) != 0) {
			MongoUser user = repository.findByUserName(username);
			log.debug("Fetched user object [{}] with Id [{}] and tokenSeed [{}] from MongoDB.",
					 user.getUserName(), user.getId(), user.getTokenSeed());

			try {
				log.debug("Trying to update tokenUserContext with tokenSeed");
				tokenUserCtx.setTokenSeed(user.getTokenSeed());
			} catch (Exception e) {
				log.debug("Aaaaand we got an error");
				log.debug("Mongo Failed", e);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
