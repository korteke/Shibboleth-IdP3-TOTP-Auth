package net.kvak.shibboleth.totpauth.authn.impl.seed;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;

public class DummySeedFetcher implements SeedFetcher {

	public DummySeedFetcher() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void getSeed(String username, TokenUserContext tokenUserCtx) {
		// Dummy seed for testing
		tokenUserCtx.setTokenSeed("G24YUKCHHXRDWCPR");
	}
}
