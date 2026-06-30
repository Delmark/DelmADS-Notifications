package ru.delmark.dads.notifications.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AnonymousConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Objects;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${security.x-api-key}")
    private String apiToken;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                .oauth2ResourceServer(config ->
                        config.opaqueToken(tokenConfig ->
                                tokenConfig.authenticationManager(opaqueTokenAuthenticationManager())
                        )
                )
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .anonymous(AnonymousConfigurer::disable)
                .build();
    }

    private AuthenticationManager opaqueTokenAuthenticationManager() {
        return authentication -> {
            BearerTokenAuthenticationToken bearerTokenAuthentication = (BearerTokenAuthenticationToken) authentication;
            String givenToken = bearerTokenAuthentication.getToken();
            boolean authorize = StringUtils.isBlank(givenToken) || !Objects.equals(givenToken, apiToken);
            authentication.setAuthenticated(authorize);
            return authentication;
        };
    }
}
