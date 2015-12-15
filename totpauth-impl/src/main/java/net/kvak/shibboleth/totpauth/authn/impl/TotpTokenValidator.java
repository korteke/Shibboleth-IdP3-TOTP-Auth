package net.kvak.shibboleth.totpauth.authn.impl;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;

import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;
import net.kvak.shibboleth.totpauth.api.authn.TokenValidator;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.shibboleth.idp.authn.AbstractValidationAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.profile.ActionSupport;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * Validates users TOTP token code against injected authenticator 
 * 
 * An action that checks for a {@link TokenCodeContext} and directly produces an
 * {@link net.shibboleth.idp.authn.AuthenticationResult} based on submitted tokencode and username
 * 
 * @author korteke
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TotpTokenValidator extends AbstractValidationAction implements TokenValidator {

	/** Class logger. */
	@Nonnull
	@NotEmpty
	private final Logger log = LoggerFactory.getLogger(TotpTokenValidator.class);

	/** Google Authenticator **/
	@Nonnull
	@NotEmpty
	private GoogleAuthenticator gAuth;

	/** Username context for username **/
	@Nonnull
	@NotEmpty
	private UsernamePasswordContext upCtx;

	/** Injected seedFetcher **/
	@Nonnull
	@NotEmpty
	private SeedFetcher seedFetcher;

	/** Inject seedfetcher **/
	public void setseedFetcher(@Nonnull @NotEmpty final SeedFetcher seedFetcher) {
		this.seedFetcher = seedFetcher;
	}

	/** Inject token authenticator **/
	public void setgAuth(@Nonnull @NotEmpty final GoogleAuthenticator gAuth) {
		this.gAuth = gAuth;
	}

	/** Constructor **/
	public TotpTokenValidator() {
		super();

	}

	@Override
	protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
			@Nonnull final AuthenticationContext authenticationContext) {
		log.debug("{} Entering totpvalidator", getLogPrefix());

		try {
			
			final TokenUserContext tokenCtx = authenticationContext.getSubcontext(TokenUserContext.class, true);
			upCtx = authenticationContext.getSubcontext(UsernamePasswordContext.class, true);
			
			boolean result = validateToken(seedFetcher.getSeed(upCtx.getUsername()),tokenCtx.getTokenCode());

			if (!result) {
				log.debug("{} Token authentication failed for user: {}", getLogPrefix(), upCtx.getUsername());
			    ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
			    return;
			}
			
			log.debug("{} Token authentication success for user: {}", getLogPrefix(), upCtx.getUsername());
			buildAuthenticationResult(profileRequestContext, authenticationContext);
			
		} catch (Exception e) {
            log.warn("{} Login by {} produced exception", getLogPrefix(), upCtx.getUsername(), e);
		}

	}

	@Override
	public boolean validateToken(String seed, int token) {
		log.debug("{} Entering validatetoken", getLogPrefix());

		if (seed.length() == 16) {
			log.debug("{} authorize {} - {} ", getLogPrefix(), seed, token);
			return gAuth.authorize(seed, token);
		}
		log.debug("{} Token code validation failed", getLogPrefix());
		return false;
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
}
