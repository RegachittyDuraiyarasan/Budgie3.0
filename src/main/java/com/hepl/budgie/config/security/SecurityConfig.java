package com.hepl.budgie.config.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = false, jsr250Enabled = false)
public class SecurityConfig {

	// Use this for asymmetric key
	@Value("${rsa.public-key}")
	RSAPublicKey publicKey;

	@Value("${rsa.private-key}")
	RSAPrivateKey privateKey;

	private final CustomAuthenticationEntryPoint entryPoint;

	public SecurityConfig(CustomAuthenticationEntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@Conditional(ProfileDevCondition.class)
	SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

		MvcRequestMatcher swaggerUIMatcher = new MvcRequestMatcher(introspector,
				"/ui");
		MvcRequestMatcher swaggerUIDefaultMatcher = new MvcRequestMatcher(introspector, "/swagger-ui/**");
		MvcRequestMatcher swaggerDocsMatcher = new MvcRequestMatcher(introspector,
				"/docs/**");
		MvcRequestMatcher signUpMatcher = new MvcRequestMatcher(introspector,
				"/v1/users/signup");
		MvcRequestMatcher profilePicMatcher = new MvcRequestMatcher(introspector,
				"/v1/users/profile-pic/**");
		MvcRequestMatcher resetLinkMatcher = new MvcRequestMatcher(introspector,
				"/v1/users/reset-link/**");
		MvcRequestMatcher forgotPasswordMatcher = new MvcRequestMatcher(introspector,
				"/v1/users/forgot-password");
		MvcRequestMatcher isSignUpMatcher = new MvcRequestMatcher(introspector,
				"/users/auth/login");
		MvcRequestMatcher forgotPassword = new MvcRequestMatcher(introspector, "/forgot-password/getEmailId");
			

		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(swaggerUIMatcher).permitAll()
						.requestMatchers(swaggerDocsMatcher).permitAll()
						.requestMatchers(swaggerUIDefaultMatcher).permitAll()
						.requestMatchers(profilePicMatcher).permitAll()
						.requestMatchers(resetLinkMatcher).permitAll()
						.requestMatchers(forgotPasswordMatcher).permitAll()
						.requestMatchers(signUpMatcher).permitAll()
						.requestMatchers(isSignUpMatcher).permitAll()
						.requestMatchers(forgotPassword).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
				.exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint));
		return http.build();
	}

	@Bean
	@Conditional(ProfileProdCondition.class)
	SecurityFilterChain filterChainProd(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {

		MvcRequestMatcher signUpMatcher = new MvcRequestMatcher(introspector, "/v1/users/signup");
		MvcRequestMatcher profilePicMatcher = new MvcRequestMatcher(introspector, "/v1/users/profile-pic/**");
		MvcRequestMatcher resetLinkMatcher = new MvcRequestMatcher(introspector, "/v1/users/reset-link/**");
		MvcRequestMatcher forgotPasswordMatcher = new MvcRequestMatcher(introspector, "/v1/users/forgot-password");
		MvcRequestMatcher isSignUpMatcher = new MvcRequestMatcher(introspector, "/v1/users/auth");

		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> Customizer.withDefaults())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(profilePicMatcher).permitAll()
						.requestMatchers(resetLinkMatcher).permitAll()
						.requestMatchers(forgotPasswordMatcher).permitAll()
						.requestMatchers(signUpMatcher).permitAll()
						.requestMatchers(isSignUpMatcher).permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
				.exceptionHandling(exception -> exception.authenticationEntryPoint(entryPoint));
		return http.build();
	}

	@Bean
	JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
	}

	@Bean
	JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
		JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		grantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

}
