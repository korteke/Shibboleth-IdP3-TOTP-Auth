package net.kvak.shibboleth.totpauth.authn.impl.seed.MongoDB;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class MongoUser {

	@Id
	private String id;
	
	private String userName;
	private String tokenSeed;

	/* Default Constructor */
	public MongoUser() {
	}
	
	/* Constructor */
	public MongoUser(String userName, String tokenSeed) {
		this.userName = userName;
		this.tokenSeed = tokenSeed;
	}
	
	public String getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTokenSeed() {
		return tokenSeed;
	}

	public void setTokenSeed(String tokenSeed) {
		this.tokenSeed = tokenSeed;
	}

}
