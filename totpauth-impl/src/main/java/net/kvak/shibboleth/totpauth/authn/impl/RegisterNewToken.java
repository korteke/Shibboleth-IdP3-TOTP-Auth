package net.kvak.shibboleth.totpauth.authn.impl;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;

import com.google.common.base.Strings;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import net.kvak.shibboleth.totpauth.api.authn.TokenValidator;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
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
@SuppressWarnings("rawtypes")
public class RegisterNewToken extends AbstractProfileAction implements TokenValidator {

	/** Class logger. */
	@Nonnull
	@NotEmpty
	private final Logger log = LoggerFactory.getLogger(RegisterNewToken.class);

	/** Google Authenticator **/
	@Nonnull
	@NotEmpty
	private GoogleAuthenticator gAuth;
	
	/* LdapTemplate */
	private LdapTemplate ldapTemplate;

	@Nonnull
	@NotEmpty
	private String tokenCodeField;

	/** Username context for username **/
	@Nonnull
	@NotEmpty
	private UsernamePasswordContext upCtx;

	/* Token user context */
	@Nonnull
	@NotEmpty
	private TokenUserContext tokenCtx;
	
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	
	

	/** Inject token authenticator **/
	public void setgAuth(@Nonnull @NotEmpty final GoogleAuthenticator gAuth) {
		this.gAuth = gAuth;
	}

	/** Constructor **/
	public RegisterNewToken() {
		super();

	}

	public void settokenCodeField(@Nonnull @NotEmpty final String fieldName) {
		log.debug("{} {} is tokencode field from the form", getLogPrefix(), fieldName);
		ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
		tokenCodeField = fieldName;
	}

	protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
			@Nonnull final AuthenticationContext authenticationContext) {
		log.debug("{} Entering RegisterNewToken", getLogPrefix());

		tokenCtx = authenticationContext.getSubcontext(TokenUserContext.class, true);
		upCtx = authenticationContext.getSubcontext(UsernamePasswordContext.class, true);

		final HttpServletRequest request = getHttpServletRequest();

		if (request == null) {
			log.debug("{} Empty request", getLogPrefix());
			ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
			return;
		}

		String token = StringSupport.trimOrNull(request.getParameter(tokenCodeField));

		if (!net.kvak.shibboleth.totpauth.authn.impl.ExtractTokenFromForm.isNumeric(token)
				&& !Strings.isNullOrEmpty(token)) {
			log.debug("{} Empty or invalid tokenCode", getLogPrefix());
			ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
			return;

		} else {
			boolean value = validateToken(tokenCtx.getSharedSecret(), Integer.parseInt(token));
			if (value) {
				registerToken(upCtx.getUsername(), tokenCtx.getSharedSecret());
			}
		}

	}

	private void registerToken(String username, String token) {
		//ldapTemplate.
	}

	@Override
	public boolean validateToken(String seed, int token) {
		log.debug("{} Entering validatetoken", getLogPrefix());

		if (seed.length() == 16) {
			log.debug("{} authorize {} - {} ", getLogPrefix(), seed, token);
			return gAuth.authorize(seed, token);
		}
		log.debug("{} Token code validation failed. Seed is not 16 char long", getLogPrefix());
		return false;
	}

}