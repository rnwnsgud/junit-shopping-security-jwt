package shop.guCoding.shopping.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import shop.guCoding.shopping.config.jwt.JwtAuthenticationFilter;
import shop.guCoding.shopping.config.jwt.JwtAuthorizationFilter;
import shop.guCoding.shopping.domain.user.UserEnum;
import shop.guCoding.shopping.service.JwtService;
import shop.guCoding.shopping.util.CustomResponseUtil;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JwtService jwtService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그 : BCryptPasswordEncoder 등록");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("디버그 : filterChain 빈 등록");
        http.headers().frameOptions().disable(); // iframe(html안에 html 불러오기) 허용 x
        http.csrf().disable(); // enable이면 post맨 작동안함
        http.cors().configurationSource(configurationSource()); // 자바스크립트 요청거부하는 cors이슈 해결
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션 안써
        http.formLogin().disable();
        http.httpBasic().disable(); // 팝업창을 이용해 인증진행하는걸 끔

        http.apply(new CustomSecurityFilterManger()); // 필터적용

        http.exceptionHandling().authenticationEntryPoint((request, response, authenticationException) -> {
            CustomResponseUtil.fail(response, "로그인을 해주세요", HttpStatus.UNAUTHORIZED);
        });

        http.authorizeRequests()
                .antMatchers("/api/s/**").authenticated()
                .antMatchers("/api/admin/**").hasRole("" + UserEnum.ADMIN) // ROLE_ADMIN
                .anyRequest().permitAll();

        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        log.debug("디버그 : configurationSource cors 설정이 SecurityFilterChain에 등록됨");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedOriginPattern("*"); // 모든 ip 주소 허용 (프론트 엔드 IP만 허용 react) , app 같은 경우는 모든 사용자가 다 다른 ip를
        // 사용해서 허용하고 안하고 할 수가 없음, 자바스크립트도 아님
        configuration.setAllowCredentials(true); // 클라이언트의 쿠키 요청 허용
        configuration.addExposedHeader("Authorization"); // 브라우저에 Authorization 헤더 노출, 클라이언트가 저장하기 위해서
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 주소요청에 위 설정을 넣어주겠다.
        return source;
    }

    public class CustomSecurityFilterManger extends AbstractHttpConfigurer<CustomSecurityFilterManger, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            builder.addFilter(new JwtAuthenticationFilter(authenticationManager, jwtService));
//            builder.addFilter(new JwtAuthorizationFilter(authenticationManager, jwtService));
            super.configure(builder);
        }
    }


}
