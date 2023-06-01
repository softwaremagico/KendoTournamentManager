package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private static final String[] AUTH_WHITELIST = {
            // ***REMOVED***Swagger
            "/v3/api-docs/**", "/swagger-ui/**",
            // Own
            "/",
            "/info/**",
            "/auth/public/**"
    };

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final JwtTokenFilter jwtTokenFilter;
    private final KendoUserDetailsService kendoUserDetailsService;
    @Value("${server.cors.domains:null}")
    private List<String> serverCorsDomains;

    @Autowired
    public WebSecurityConfig(AuthenticatedUserProvider authenticatedUserProvider, JwtTokenFilter jwtTokenFilter,
                             KendoUserDetailsService kendoUserDetailsService) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.jwtTokenFilter = jwtTokenFilter;
        this.kendoUserDetailsService = kendoUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(kendoUserDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(httpSecuritySessionManagementConfigurer ->
                        httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                        httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint((request, response, ex) -> response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                ex.getMessage()
                        )))
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Used by spring security if CORS is enabled.
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        if (serverCorsDomains == null || serverCorsDomains.contains("*")) {
            config.setAllowedOriginPatterns(Collections.singletonList("*"));
        } else {
            config.setAllowedOrigins(serverCorsDomains);
            config.setAllowCredentials(true);
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader(HttpHeaders.AUTHORIZATION);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
