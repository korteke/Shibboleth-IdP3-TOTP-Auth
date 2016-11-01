package net.kvak.shibboleth.totpauth.authn.impl;

import javax.annotation.Nonnull;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

@SuppressWarnings("rawtypes")
public class GenerateNewToken extends AbstractProfileAction {

	/** Class logger. */
	@Nonnull
	private final Logger log = LoggerFactory.getLogger(GenerateNewToken.class);

	TokenUserContext tokenCtx;

	UsernamePasswordContext upCtx;

	/** Google Authenticator **/
	@Nonnull
	@NotEmpty
	private GoogleAuthenticator gAuth;

	/** Issuer name for Authenticator **/
	@Nonnull
	@NotEmpty
	private String gAuthIssuerName;

	/** Inject token authenticator **/
	public void setgAuth(@Nonnull @NotEmpty final GoogleAuthenticator gAuth) {
		this.gAuth = gAuth;
	}

	public void setgAuthIssuerName(@Nonnull @NotEmpty final String gAuthIssuerName) {
		this.gAuthIssuerName = gAuthIssuerName;
	}

	@Override
	protected void doInitialize() throws ComponentInitializationException {
	    super.doInitialize();
	}

	@Override
	protected boolean doPreExecute(ProfileRequestContext profileRequestContext) {
		log.debug("Entering GenerateNewToken doPreExecute");

		try {
			tokenCtx = profileRequestContext.getSubcontext(AuthenticationContext.class)
					.getSubcontext(TokenUserContext.class, true);
			upCtx = profileRequestContext.getSubcontext(AuthenticationContext.class)
					.getSubcontext(UsernamePasswordContext.class);
			return true;
		} catch (Exception e) {
			log.debug("Error with doPreExecute", e);
			return false;

		}

	}

    @Override
	protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
    	log.debug("Entering GenerateNewToken doExecute");

		try {
			log.debug("Trying to create new token for {}", upCtx.getUsername());
			generateToken();
		} catch (Exception e) {
			log.debug("Failed to create new token", e);
		}

	}

	private void generateToken() {
		log.debug("Generating new token shared secret and URL for {}", upCtx.getUsername());

		try {
			final GoogleAuthenticatorKey key = gAuth.createCredentials();

			String totpUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL(gAuthIssuerName, upCtx.getUsername(), key);
			log.debug("Totp URL for {} is {}", upCtx.getUsername(), totpUrl);
			tokenCtx.setTotpUrl(totpUrl);

			String sharedSecret = StringSupport.trimOrNull(key.getKey());
			log.debug("Shared secret for {} is {}", upCtx.getUsername(), sharedSecret);
			tokenCtx.setSharedSecret(sharedSecret);

		} catch (Exception e) {
			log.debug("Error generating new token",e);
		}



	}

}
