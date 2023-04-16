package shop.guCoding.shopping.config.jwt;

import com.auth0.jwt.exceptions.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.handler.ex.CustomJwtException;
import shop.guCoding.shopping.service.JwtService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${jwt.access_header:null}")
    private String ACCESS_HEADER;

    @Value("${jwt.refresh_header:null}")
    private String REFRESH_HEADER;

    private final JwtService jwtService;


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        super(authenticationManager);
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("디버그 : 인가필터 호출");
        log.debug("@value 찍힘? " + ACCESS_HEADER);
        // header 검증
        // if로 안 묶으면 회원가입시 token 이 null 이라 오류뜸, 테스트마다 access(실제값), refresh 넣어줘야하네()
        if (jwtService.checkHeaderVerify(request)) {

            String accessToken = request.getHeader(ACCESS_HEADER).replace("Bearer ",""); // Bearer 앞에 없앤 순수한 토큰
            String refreshToken = request.getHeader(REFRESH_HEADER).replace("Bearer ","");

            // accessToken 검증
            try {
                LoginUser loginUser = jwtService.accessTokenVerify(accessToken);
                Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser,null,loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

//                log.debug("securityContext" + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            } catch (TokenExpiredException e) {
                log.error("accessToken 오류");
                // refreshToken 검증
                try {
                    jwtService.refreshTokenVerify(refreshToken);
                    LoginUser loginUser = jwtService.findUserWithRefreshToken(refreshToken);
                    String reissuedAccessToken = jwtService.accessTokenCreate(loginUser);
                    response.addHeader(ACCESS_HEADER, reissuedAccessToken);
                } catch (TokenExpiredException exception) {
                    throw new CustomJwtException(exception.getMessage());
                }

            }


            // refreshToken 이 7일 이내 만료 될 경우 refreshToken 재발급
            if (jwtService.reissueRefreshToken(refreshToken)) {
                refreshToken = jwtService.refreshTokenCreate();
                response.addHeader(REFRESH_HEADER, refreshToken);
            }
        }


        chain.doFilter(request, response);
    }
}
