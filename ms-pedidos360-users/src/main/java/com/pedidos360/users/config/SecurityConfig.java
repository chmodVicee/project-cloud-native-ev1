package com.pedidos360.users.config;

import com.pedidos360.users.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class SecurityConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        OAuth2ClientProperties.Registration reg = properties.getRegistration().get("azure");
        if (reg == null) {
            throw new IllegalStateException("No se encontró spring.security.oauth2.client.registration.azure");
        }
        OAuth2ClientProperties.Provider prov = properties.getProvider().get("azure");
        if (prov == null) {
            throw new IllegalStateException("No se encontró spring.security.oauth2.client.provider.azure");
        }

        ClientAuthenticationMethod authMethod = ClientAuthenticationMethod.CLIENT_SECRET_POST;
        String method = reg.getClientAuthenticationMethod();
        if (method != null) {
            switch (method) {
                case "client_secret_basic" -> authMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
                case "client_secret_post" -> authMethod = ClientAuthenticationMethod.CLIENT_SECRET_POST;
                case "none" -> authMethod = ClientAuthenticationMethod.NONE;
                case "private_key_jwt" -> authMethod = ClientAuthenticationMethod.PRIVATE_KEY_JWT;
                case "client_secret_jwt" -> authMethod = ClientAuthenticationMethod.CLIENT_SECRET_JWT;
            }
        }

        ClientRegistration.Builder builder = ClientRegistrations.fromIssuerLocation(prov.getIssuerUri())
                .registrationId("azure")
                .clientId(reg.getClientId())
                .clientSecret(reg.getClientSecret())
                .clientAuthenticationMethod(authMethod)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(reg.getRedirectUri())
                .scope(reg.getScope().toArray(new String[0]))
                .clientSettings(ClientRegistration.ClientSettings.builder().requireProofKey(true).build());

        if (prov.getUserNameAttribute() != null) {
            builder.userNameAttributeName(prov.getUserNameAttribute());
        }

        return new InMemoryClientRegistrationRepository(List.of(builder.build()));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            OAuth2LoginFailureHandler oAuth2LoginFailureHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/login/**", "/oauth2/**", "/error").permitAll()
                        .requestMatchers("/api/users/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"Se requiere token JWT\"}");
                        }))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}