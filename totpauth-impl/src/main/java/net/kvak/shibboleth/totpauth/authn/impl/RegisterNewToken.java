package net.kvak.shibboleth.totpauth.authn.impl;

import java.util.List;

import javax.annotation.Nonnull;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.EqualsFilter;

import com.google.common.base.Strings;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext.AuthState;
import net.kvak.shibboleth.totpauth.authn.impl.TotpUtils;
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
@SuppressWarnings({ "rawtypes", "deprecation" })
public class RegisterNewToken extends AbstractProfileAction {

	/** Class logger. */
	@Nonnull
	@NotEmpty
	private final Logger log = LoggerFactory.getLogger(RegisterNewToken.class);

	/** Google Authenticator **/
	@Nonnull
	@NotEmpty
	private GoogleAuthenticator gAuth;

	/** LdapTemplate **/
	private LdapTemplate ldapTemplate;

	/** TokenCodeField that is on RegisterToken form **/
	@Nonnull
	@NotEmpty
	private String tokenCodeField;

	/** User attribute in LDAP (ex. uid) **/
	@Nonnull
	@NotEmpty
	private String userAttribute;

	/** seedToken attribute in LDAP */
	@Nonnull
	@NotEmpty
	private String seedAttribute;

	/** Username context for username **/
	@Nonnull
	@NotEmpty
	private UsernamePasswordContext upCtx;

	/** Token user context */
	@Nonnull
	@NotEmpty
	private TokenUserContext tokenCtx;

	/** Inject ldapTemplate */
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/** Inject token authenticator */
	public void setgAuth(@Nonnull @NotEmpty final GoogleAuthenticator gAuth) {
		this.gAuth = gAuth;
	}

	/** Constructor 
	 * Initialize user and seed attributes
	 * */
	public RegisterNewToken(String seedAttribute, String userAttribute) {
		log.debug("Construct RegisterNewToken with {} - {}", seedAttribute, userAttribute);
		this.userAttribute = userAttribute;
		this.seedAttribute = seedAttribute;
	}

	public void settokenCodeField(@Nonnull @NotEmpty final String fieldName) {
		log.debug("{} {} is tokencode field from the form", getLogPrefix(), fieldName);
		ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
		tokenCodeField = fieldName;
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

	protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) {
		log.debug("{} Entering RegisterNewToken", getLogPrefix());

		final HttpServletRequest request = getHttpServletRequest();
		final TotpUtils totpUtils = new TotpUtils();

		if (request == null) {
			log.debug("{} Empty request", getLogPrefix());
			ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
			return;
		}

		String token = StringSupport.trimOrNull(request.getParameter(tokenCodeField));

		if (!StringUtils.isNumeric(token) || Strings.isNullOrEmpty(token)) {
			log.debug("{} Empty or invalid tokenCode", getLogPrefix());
			tokenCtx.setState(AuthState.CANT_VALIDATE);
			
			ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
			return;

		} else {
			boolean tokenValidate = totpUtils.validateToken(tokenCtx.getSharedSecret(), Integer.parseInt(token));
			if (tokenValidate) {

				String dn = fetchDn(upCtx.getUsername());

				if (!Strings.isNullOrEmpty(dn)) {
					log.debug("{} User {} DN is {}", getLogPrefix(), upCtx.getUsername(), dn);
					boolean result = registerToken(dn, tokenCtx.getSharedSecret());
					
					if (!result) {
						ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.ACCOUNT_ERROR);
					}
				}

			}
			log.debug("Invalid token. Returning.");
			tokenCtx.setState(AuthState.CANT_VALIDATE);
			ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
		}
	}

	private boolean registerToken(String dn, String sharedSecret) {

		log.debug("Entering registerToken");
		
		try {
			Attribute attr = new BasicAttribute(seedAttribute, sharedSecret);
			log.debug("Created new BasicAttribute [{} - {}]", attr.getID(), attr.get(0));
			ModificationItem item = new ModificationItem(DirContext.ADD_ATTRIBUTE, attr);
			log.debug("{} Trying to write the changes to the LDAP", getLogPrefix());
			ldapTemplate.modifyAttributes(dn, new ModificationItem[] { item });
			return true;
		} catch (Exception e) {
			log.debug("{} registerToken error", getLogPrefix(), e);
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	private String fetchDn(String username) {

		String dn = "";
		EqualsFilter f = new EqualsFilter(userAttribute, username);
		log.debug("{} Trying to find user {} dn from ldap with filter {}", getLogPrefix(), username, f.encode());

		List result = ldapTemplate.search(DistinguishedName.EMPTY_PATH, f.toString(), new AbstractContextMapper() {
			protected Object doMapFromContext(DirContextOperations ctx) {
				return ctx.getDn().toString();
			}
		});

		if (result.size() == 1) {
			log.debug("{} User {} relative DN is: {}", getLogPrefix(), username, (String) result.get(0));
			dn = (String) result.get(0);
		} else {
			log.debug("{} User not found or not unique. DN size: {}", getLogPrefix(), result.size());
			throw new RuntimeException("User not found or not unique");
		}

		return dn;

	}

}