package com.chirag.auth_in_one.auth_app_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public UserDetailsService users() {
//        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
//
//        UserDetails user1 = userBuilder.username("chirag").password("abc").roles("ADMIN").build();
//        UserDetails user2 = userBuilder.username("ankit").password("xyz").roles("USER").build();
//        return new InMemoryUserDetailsManager(user1, user2);
//    }

}
