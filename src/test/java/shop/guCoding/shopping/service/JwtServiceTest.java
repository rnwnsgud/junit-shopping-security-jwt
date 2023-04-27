package shop.guCoding.shopping.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import shop.guCoding.shopping.config.auth.LoginUser;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserEnum;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;



    @Value("${jwt.token_prefix:null}")
    private String TOKEN_PREFIX;

    @Value("${jwt.secret}")
    private String SECRET;

    private String createAccessToken() {
        User user = User.builder().id(1L).role(UserEnum.CUSTOMER).build();
        LoginUser loginUser = new LoginUser(user);

        String accessToken = jwtService.accessTokenCreate(loginUser);
        return accessToken;
    }

    private String createRefreshToken() {

        String refreshToken = jwtService.refreshTokenCreate();
        return refreshToken;
    }

    @Test
    public void createToken_test() throws Exception {
        //given

        //when
        String accessToken = createAccessToken();
        String refreshToken = createRefreshToken();

        //then
        assertTrue(accessToken.startsWith(TOKEN_PREFIX));
        assertTrue(refreshToken.startsWith(TOKEN_PREFIX));

    }

    @Test
    public void verify_test() throws Exception {
        //given
        String token = createAccessToken();
        String accessToken = token.replace(TOKEN_PREFIX, "");
        //when
        LoginUser loginUser = jwtService.accessTokenVerify(accessToken);

        //then
        assertThat(loginUser.getUser().getId()).isEqualTo(1L);
        assertThat(loginUser.getUser().getRole()).isEqualTo(UserEnum.CUSTOMER);
    }
    
    @Test
    public void reissueRefreshToken_test() throws Exception {
        //given
        
        //when
        
        //then
    }

}