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
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.handler.ex.CustomJwtException;
import shop.guCoding.shopping.service.JwtService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final Logger log = LoggerFactory.getLogger(getClass());

//    @Value("${jwt.access_header:null}")
//    private String ACCESS_HEADER;
//    @Value("${jwt.access_header:null}")
//    private String REFRESH_HEADER;
//    @Value("${jwt.token_prefix:null}")
//    private String TOKEN_PREFIX;
    private final JwtService jwtService;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtService jwtService) {
        super(authenticationManager);
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.debug("디버그 : 인가필터 호출");
        // header 검증
        // if로 안 묶으면 회원가입시 token 이 null 이라 오류뜸
        if (jwtService.checkHeaderVerify(request)) {
            String accessToken = request.getHeader("ACCESS_TOKEN").replace("Bearer ",""); // Bearer 앞에 없앤 순수한 토큰
            String refreshToken = request.getHeader("REFRESH_TOKEN").replace("Bearer ","");

            // accessToken 검증
            try {
                LoginUser loginUser = jwtService.accessTokenVerify(accessToken);
                Authentication authentication = new UsernamePasswordAuthenticationToken(loginUser,null,loginUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (TokenExpiredException e) {
                log.error("accessToken 오류"); // **이게 만료됐으면 다시 만들어주면 되는데, 조작돼서 오류난거면 어뜨케 해줘야 됨**
                // refreshToken 검증
                try {
                    jwtService.refreshTokenVerify(refreshToken);
                    LoginUser loginUser = jwtService.findUserWithRefreshToken(refreshToken);
                    jwtService.accessTokenCreate(loginUser);
                } catch (TokenExpiredException exception) {
                    throw new CustomJwtException(exception.getMessage());
                }

            }


            // refreshToken 이 7일 이내 만료 될 경우 refreshToken 재발급
            if (jwtService.reissueRefreshToken(refreshToken)) {
                refreshToken = jwtService.refreshTokenCreate();
                response.addHeader("REFRESH_TOKEN", refreshToken);
            }
        }


        chain.doFilter(request, response);
    }
}
