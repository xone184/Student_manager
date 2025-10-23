package com.example.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.example.app.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		logger.info("Configuring security filter chain");

		http.cors().and().csrf().disable() // tắt CSRF (phù hợp cho API REST).
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // không dùng session để lưu
																							// user → chỉ dùng JWT.
				.and()				.authorizeHttpRequests(authorize -> authorize
						// Public endpoints
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/actuator/health").permitAll()
						.requestMatchers("/actuator/info").permitAll()
						.requestMatchers("/api/gemini/**").permitAll()
						.requestMatchers("/api/payments/vnpay/debug").permitAll() // Debug endpoint
						.requestMatchers("/api/payments/vnpay/test-repo").permitAll() // Test repository endpoint
						.requestMatchers("/api/payments/vnpay/test-convert").permitAll() // Test convert endpoint
						.requestMatchers("/api/payments/vnpay/simple-test").permitAll() // Simple test endpoint
						.requestMatchers("/api/payments/vnpay/test-step").permitAll() // Test step endpoint
						.requestMatchers("/api/payments/vnpay/initiate-public").permitAll() // Public VNPay endpoint for testing
						
						// Student endpoints - accessible by all authenticated users
						.requestMatchers("/api/student/**").hasAuthority("ROLE_SINH_VIÊN")
						
						// Student thanh toán VNPay - accessible by students
						.requestMatchers("/api/payments/vnpay/**").hasAuthority("ROLE_SINH_VIÊN")

						// Lecturer endpoints - accessible by lecturers and principals
						.requestMatchers("/api/teacher/**").hasAuthority("ROLE_GIẢNG_VIÊN")

						// Principal endpoints - only principals (excluding VNPay which is handled above)
						.requestMatchers("/api/payments/**").hasAuthority("ROLE_HIỆU_TRƯỞNG")
						.requestMatchers("/api/**").hasAuthority("ROLE_HIỆU_TRƯỞNG")

						// All other requests need authentication
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		logger.info("Security filter chain configured successfully");
		return http.build();
	}

	@Bean
	public CorsFilter corsFilter() {
		logger.info("Configuring CORS filter");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);

		// Cho phép tất cả FE bạn đang dùng (Angular 4200, 4201, React 3000,...)
		config.addAllowedOrigin("http://localhost:4200");
		config.addAllowedOrigin("http://localhost:4201");
		config.addAllowedOrigin("http://127.0.0.1:4200");
		config.addAllowedOrigin("http://127.0.0.1:4201");
		config.addAllowedOrigin("http://localhost:3000");
		config.addAllowedOrigin("http://127.0.0.1:3000");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setMaxAge(3600L);
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		logger.info("Configuring BCrypt password encoder");
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		logger.info("Configuring authentication manager");
		return authConfig.getAuthenticationManager();
	}
}
