package net.kvak.shibboleth.totpauth.api.authn;

public interface SeedFetcher {
	
	public String getSeed(String username);

}
