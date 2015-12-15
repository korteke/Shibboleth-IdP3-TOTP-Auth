package net.kvak.shibboleth.totpauth.api.authn;

public interface TokenValidator {
	
	public boolean validateToken(String seed, int token);

}
