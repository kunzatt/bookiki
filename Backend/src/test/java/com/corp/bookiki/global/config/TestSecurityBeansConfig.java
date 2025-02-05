package com.corp.bookiki.global.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

@TestConfiguration
public class TestSecurityBeansConfig {

	@MockBean
	private UserDetailsService userDetailsService;


	@MockBean
	private ClientRegistrationRepository clientRegistrationRepository;

	@MockBean
	private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

	@MockBean
	private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
}
