package shop.guCoding.shopping.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserEnum;
import shop.guCoding.shopping.domain.user.UserRepository;

import java.util.Date;
import java.util.Optional;


@RequiredArgsConstructor
@Transactional
@Service
public class JwtService {

    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET;

    @Value("${jwt.token_prefix:null}")
    private String TOKEN_PREFIX;

    @Value("${jwt.expiration_time:null}")
    private Integer EXPIRATION_TIME;

    public void saveRefreshToken(LoginUser loginUser, String refreshToken) {
        Optional<User> userOP = userRepository.findByEmail(loginUser.getEmail());
        if (userOP.isPresent()) {
            User user = userOP.get();
            user.saveRefreshToken(refreshToken);
        }
    }

    // access 토큰 생성
    public String accessTokenCreate(LoginUser loginUser) {

        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(new Date(System.currentTimeMillis() + 100))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole().name())
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }

    // refresh 토큰
    public String refreshTokenCreate() {
        String jwtToken = JWT.create()
                .withSubject("shopping")
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME * 4)) // integer도 잘되나 확인
                .sign(Algorithm.HMAC512(SECRET));

        return TOKEN_PREFIX + jwtToken;
    }

    // 토큰 검증
    public LoginUser verify(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(SECRET)).build().verify(token);
        Long id = decodedJWT.getClaim("id").asLong();
        String role = decodedJWT.getClaim("role").asString();
        User user = User.builder().id(id).role(UserEnum.valueOf(role)).build();
        LoginUser loginUser = new LoginUser(user);
        return loginUser;
    }

}
