package net.kvak.shibboleth.totpauth.authn.impl.seed;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;

public class DummySeedFetcher implements SeedFetcher {

	public DummySeedFetcher() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getSeed(String username) {
		// Dummy seed for testing
		if (username != null) {
		return "G24YUKCHHXRDWCPR";
		}
		return "";
	}

}
