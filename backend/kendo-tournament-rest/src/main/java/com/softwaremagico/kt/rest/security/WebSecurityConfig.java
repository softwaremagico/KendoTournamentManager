package com.softwaremagico.kt.rest.security;

import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {
    private static final String[] AUTH_WHITELIST = {
            // -- Swagger
            "/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**",
            // Others
            "/info/**",
            "/api/public/**"
    };

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final JwtTokenFilter jwtTokenFilter;

    @Value("${server.cors.domains:null}")
    private List<String> serverCorsDomains;

    @Autowired
    public WebSecurityConfig(AuthenticatedUserProvider authenticatedUserProvider, JwtTokenFilter jwtTokenFilter) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> authenticatedUserProvider
                .findByUsername(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                String.format("User '%s' not found!", username)
                        )
                ));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        // Set session management to stateless
        http = http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and();

        // Set unauthorized requests exception handler
        http = http.exceptionHandling().authenticationEntryPoint((request, response, ex) -> response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                ex.getMessage()
        )).and();

        //Block everything
        http.authorizeRequests().anyRequest().authenticated();

        // Add JWT token filter
        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
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
        }
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public WebSecurityCustomizer ignoreInfo() {
        return (webSecurity) -> webSecurity
                .ignoring().antMatchers(AUTH_WHITELIST);
    }

    @Bean
    public AuthenticationManager customAuthenticationManager() throws Exception {
        return authenticationManager();
    }
}
