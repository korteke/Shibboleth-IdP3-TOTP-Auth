package net.kvak.shibboleth.totpauth.api.authn;

import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;

public interface SeedFetcher {
	
	public void getSeed(String username, TokenUserContext tokenUserCtx);

}
