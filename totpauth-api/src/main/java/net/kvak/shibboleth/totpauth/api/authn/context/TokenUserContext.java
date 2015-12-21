package net.kvak.shibboleth.totpauth.api.authn.context;

import org.opensaml.messaging.context.BaseContext;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class TokenUserContext extends BaseContext {

	public enum AuthState {
		OK, ERROR, MISSING_SEED, REGISTER, CANT_VALIDATE, UNKNOWN
	}

	/** The username. */
	private String username;

	/** The password associated with the username. */
	private String password;

	/** The tokenCode associated with the username. */
	private int tokenCode;

	/** The tokenSeed associated with the username. */
	private ArrayList<String> tokenSeed = new ArrayList<String>();
	
	/* Url for QR-code */
	private String totpUrl;
	
	/* TOTP shared secret */
	private String sharedSecret;

	public String getTotpUrl() {
		return totpUrl;
	}

	public void setTotpUrl(String totpUrl) {
		this.totpUrl = totpUrl;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	private AuthState state = AuthState.OK;

	public AuthState getState() {
		return state;
	}

	public void setState(AuthState state) {
		this.state = state;
	}

	/**
	 * Gets the token seed.
	 *
	 * @return the tokenSeed
	 */
	public ArrayList<String> getTokenSeed() {
		return tokenSeed;
	}

	/**
	 * Sets the token seed.
	 *
	 * @param name
	 *            the tokenSeed
	 *
	 * @return this tokenSeed
	 */
	public void setTokenSeed(String tokenSeed) {		
		this.tokenSeed.add(tokenSeed);
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	@Nullable
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param name
	 *            the username
	 *
	 * @return this context
	 */
	public TokenUserContext setUsername(@Nullable final String name) {
		username = name;
		return this;
	}

	/**
	 * Gets the password associated with the username.
	 *
	 * @return password associated with the username
	 */
	@Nullable
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password associated with the username.
	 *
	 * @param pass
	 *            password associated with the username
	 *
	 * @return this context
	 */
	public TokenUserContext setPassword(@Nullable final String pass) {
		password = pass;
		return this;
	}

	/**
	 * Gets the tokenCode.
	 *
	 * @return the tokenCode
	 */
	@Nullable
	public int getTokenCode() {
		return tokenCode;
	}

	/**
	 * Sets the tokenCode associated with the username.
	 *
	 * @param pass
	 *            tokenCode associated with the username
	 *
	 * @return this context
	 */
	public TokenUserContext setTokenCode(@Nullable final int tCode) {
		tokenCode = tCode;
		return this;
	}

}