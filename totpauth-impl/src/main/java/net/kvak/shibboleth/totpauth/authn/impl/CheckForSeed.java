package net.kvak.shibboleth.totpauth.authn.impl;

import javax.annotation.Nonnull;

import net.kvak.shibboleth.totpauth.api.authn.SeedFetcher;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext;
import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.UsernamePasswordContext;
import net.shibboleth.idp.profile.AbstractProfileAction;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import org.opensaml.profile.action.ActionSupport;
import net.shibboleth.idp.authn.AuthnEventIds;
import net.kvak.shibboleth.totpauth.api.authn.context.TokenUserContext.AuthState;


@SuppressWarnings("rawtypes")
public class CheckForSeed extends AbstractProfileAction {

	/** Class logger. */
	@Nonnull
	private final Logger log = LoggerFactory.getLogger(GenerateNewToken.class);

	TokenUserContext tokenUserCtx;

	UsernamePasswordContext upCtx;


	/** Seed Fetcher implementation **/
	@Nonnull
	@NotEmpty
	private SeedFetcher seedFetcher;

	/** Inject token authenticator **/
	public void setseedFetcher(@Nonnull @NotEmpty final SeedFetcher seedFetcher) {
		this.seedFetcher = seedFetcher;
	}

    @Override
    protected void doInitialize() throws ComponentInitializationException {
        super.doInitialize();
    }

	@Override
	protected boolean doPreExecute(ProfileRequestContext profileRequestContext) {
		log.debug("Entering CheckForSeed doPreExecute");

		try {
			tokenUserCtx = profileRequestContext.getSubcontext(AuthenticationContext.class)
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
    	log.debug("Entering CheckForSeed doExecute");

		String username = upCtx.getUsername();
    	seedFetcher.getSeed(username, tokenUserCtx);
    	if (tokenUserCtx.getState() != AuthState.OK) {
    		ActionSupport.buildEvent(profileRequestContext, AuthnEventIds.INVALID_CREDENTIALS);
    	}

	}
}
