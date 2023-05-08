package shop.guCoding.shopping.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.service.JwtService;
import shop.guCoding.shopping.util.CustomResponseUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;


import static shop.guCoding.shopping.dto.user.UserReqDto.*;
import static shop.guCoding.shopping.dto.user.UserRespDto.*;

@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;



    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        super(authenticationManager);
        setFilterProcessesUrl("/api/login");
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

//    @Value("${jwt.access_header:null}")
//    private String ACCESS_HEADER;
//
//    @Value("${jwt.refresh_header:null}")
//    private String REFRESH_HEADER;
//
//    @Value("${jwt.token_prefix:null}")
//    private String TOKEN_PREFIX;
//
//    @Value("${jwt.secret}")
//    private String SECRET;


    // Post : /login
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.debug("디버그 : attemptAuthentication 호출됨");
//        log.debug("TOKEN_PREFIX " + token_prefix);

        try {
            // request 의 json 데이터 꺼내기
            ObjectMapper om = new ObjectMapper();
            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            // 강제 로그인
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginReqDto.getEmail(), loginReqDto.getPassword()
            );
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            return authentication;
        } catch (Exception e) {
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.debug("디버그 : successfulAuthentication 호출됨");

        LoginUser loginUser = (LoginUser) authResult.getPrincipal();


        String jwtAccessToken = jwtService.accessTokenCreate(loginUser);
//        String jwtAccessToken = jwtService.testAccessTokenCreate(loginUser);
        String jwtRefreshToken = jwtService.refreshTokenCreate(); // 다시 돌리기
//        String jwtRefreshToken = jwtService.testRefreshTokenCreate();

//
//        log.debug("디버그 : jwtAccTK " + jwtAccessToken);
//        log.debug("디버그 : jwtRefTK " + jwtRefreshToken);

        response.addHeader("ACCESS_TOKEN", jwtAccessToken);
        response.addHeader("REFRESH_TOKEN", jwtRefreshToken);

//        log.debug("TOKEN_PREFIX " + token_prefix);
        String refreshTokenNotBearer = jwtRefreshToken.replace("Bearer ","");
        log.debug("디버그 : refreshTokenNotBearer " + refreshTokenNotBearer);

        jwtService.saveRefreshToken(loginUser,refreshTokenNotBearer);

        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());
        CustomResponseUtil.success(response, loginRespDto);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        CustomResponseUtil.fail(response, "로그인실패", HttpStatus.UNAUTHORIZED);
    }
}
