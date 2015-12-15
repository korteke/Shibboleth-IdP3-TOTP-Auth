package net.kvak.shibboleth.totpauth.api.authn.context;

import org.opensaml.messaging.context.BaseContext;

import javax.annotation.Nullable;

public class TokenUserContext extends BaseContext{

	/** The username. */
    private String username;

    /** The password associated with the username. */
    private String password;
    
    private int tokenCode;

    /**
     * Gets the username.
     *
     * @return the username
     */
    @Nullable public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param name the username
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
    @Nullable public String getPassword() {
        return password;
    }

    /**
     * Sets the password associated with the username.
     *
     * @param pass password associated with the username
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
    @Nullable public int getTokenCode() {
        return tokenCode;
    }
    
    /**
     * Sets the tokenCode associated with the username.
     *
     * @param pass tokenCode associated with the username
     *
     * @return this context
     */
    public TokenUserContext setTokenCode(@Nullable final int tCode) {
        tokenCode = tCode;
        return this;
    }

}