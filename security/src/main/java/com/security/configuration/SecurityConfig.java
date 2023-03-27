package com.security.configuration;

import com.security.handler.CustomAuthenticationFailureHandler;
import com.security.modal.ERole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String BCRYPT = "bcrypt";
    public static final String ADMIN = ERole.ADMIN.name();
    public static final String VIEW_ADMIN = ERole.VIEW_ADMIN.name();
    public static final String USER = ERole.USER.name();
    public static final String VIEW_INFO = ERole.VIEW_INFO.name();

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http, CustomAuthenticationFailureHandler authenticationFailureHandler) throws Exception {
        return http.authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers("/about", "/login*").permitAll()
                                .requestMatchers("/").permitAll()
                                .requestMatchers("/info").hasAnyAuthority(USER, VIEW_INFO, ADMIN, VIEW_ADMIN)
                                .requestMatchers("/admin").hasAnyAuthority(ADMIN, VIEW_ADMIN)
                                .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                        formLogin.loginPage("/login")
                                .failureHandler(authenticationFailureHandler)
                                .permitAll())
                .logout(formLogout -> formLogout.deleteCookies("JSESSIONID")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/logoutSuccess")
                        .permitAll())
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public DelegatingPasswordEncoder passwordEncoder() {
        Map<String, PasswordEncoder> passwordEncoders = new HashMap<>();
        passwordEncoders.put(BCRYPT, new BCryptPasswordEncoder());

        return new DelegatingPasswordEncoder(BCRYPT, passwordEncoders);
    }

}
