package net.kvak.shibboleth.totpauth.authn.impl;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.shibboleth.idp.authn.AbstractExtractionAction;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentSupport;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

/**
 * An action that extracts a token code from an HTTP form, creates a
 * {@link TokenUserContext}, and attaches it to the
 * {@link AuthenticationContext}.
 * 
 * @author korteke
 */
@SuppressWarnings("rawtypes")
public class ExtractTokenFromForm extends AbstractExtractionAction {

	/** Class logger. */
	@Nonnull
	private final Logger log = LoggerFactory.getLogger(ExtractTokenFromForm.class);

	@Nonnull
	@NotEmpty
	private String tokenCodeField;

	/** Parameter name for TokenNumber */
	@Nonnull
	@NotEmpty
	private String tokenNumberField;

	/**
	 * Constructor.
	 */
	public ExtractTokenFromForm() {
		super();
	}

	public void settokenCodeField(@Nonnull @NotEmpty final String fieldName) {
		log.debug("{} {} is tokencode field from the form", getLogPrefix(), fieldName);
		ComponentSupport.ifInitializedThrowUnmodifiabledComponentException(this);
		tokenCodeField = fieldName;
	}

	@Override
	protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext,
			@Nonnull final AuthenticationContext authenticationContext) {

		final HttpServletRequest request = getHttpServletRequest();

		if (request == null) {
			log.debug("{} Empty request", getLogPrefix());
			ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.NO_CREDENTIALS);
			return;
		}

		try {

			TokenUserContext tokenCtx = authenticationContext.getSubcontext(TokenUserContext.class, true);

			/** get tokencode from request **/
			String value = StringSupport.trimOrNull(request.getParameter(tokenCodeField));

			if (!isNumeric(value) && !Strings.isNullOrEmpty(value)) {
				log.debug("{} Empty or invalid tokenCode", getLogPrefix());
				ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
				return;
			} else {
				log.debug("{} TokenCode: {}", getLogPrefix(), Integer.parseInt(value));

				/** set tokencode to TokenCodeContext **/
				tokenCtx.setTokenCode(Integer.parseInt(value));
				log.debug("Put Token code to the TokenCodeCtx");
				return;
			}

		} catch (Exception e) {
			log.warn("{} Login by {} produced exception", getLogPrefix(), e);
		}
	}

	@SuppressWarnings("unused")
	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

}
