package net.kvak.shibboleth.totpauth.authn.impl;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Validates users TOTP token code against injected authenticator
 * 
 * An action that checks for a {@link TokenCodeContext} and directly produces an
 * {@link net.shibboleth.idp.authn.AuthenticationResult} based on submitted
 * tokencode and username
 * 
 * @author korteke
 *
 */

/*
 * TODO, EVERYTHING..
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RegisterNewToken extends AbstractValidationAction {

	/** Class logger. */
	@Nonnull
	@NotEmpty
	private final Logger log = LoggerFactory.getLogger(RegisterNewToken.class);

	/** Google Authenticator **/
	@Nonnull
	@NotEmpty
	private GoogleAuthenticator gAuth;

	/** Username context for username **/
	@Nonnull
	@NotEmpty
	private UsernamePasswordContext upCtx;
	
	/* Token user context */
	@Nonnull
	@NotEmpty
	private TokenUserContext tokenCtx;

	/** Inject token authenticator **/
	public void setgAuth(@Nonnull @NotEmpty final GoogleAuthenticator gAuth) {
		this.gAuth = gAuth;
	}

	/** Constructor **/
	public RegisterNewToken() {
		super();

	}

	@Override
	protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
			@Nonnull final AuthenticationContext authenticationContext) {
		log.debug("{} Entering RegisterNewToken", getLogPrefix());

			tokenCtx = authenticationContext.getSubcontext(TokenUserContext.class, true);
			upCtx = authenticationContext.getSubcontext(UsernamePasswordContext.class, true);
			
			buildAuthenticationResult(profileRequestContext, authenticationContext);

	}


	@Override
	protected Subject populateSubject(Subject subject) {
		if (StringSupport.trimOrNull(upCtx.getUsername()) != null) {
			log.debug("{} Populate subject {}", getLogPrefix(), upCtx.getUsername());
			subject.getPrincipals().add(new UsernamePrincipal(upCtx.getUsername()));
			return subject;
		}
		return null;

	}
	
	@SuppressWarnings("unused")
	protected void registerToken(String blop) {
	final GoogleAuthenticatorKey key = gAuth.createCredentials();
	String sharedSecret = StringSupport.trimOrNull(key.getKey());
	
	final String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthURL("TOTP", upCtx.getUsername(), key);
	
	}
}
