package shop.guCoding.shopping;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class jwtPropertyTest {

    @Value("${jwt.secret:null}")
    String secret;

    @Value("${jwt.token_prefix:null}")
    String token_prefix;

    @Value("${jwt.expiration_time:null}")
    Integer expiration_time;

    @Test
    public void jwtProperty_test() throws Exception {
        //given
        System.out.println("테스트 : " + secret);
        System.out.println("테스트 : " + token_prefix + "공백확인");
        System.out.println("테스트 : " + expiration_time);
        assertThat(expiration_time).isEqualTo(1000 * 60 * 24 * 7);
        //when

        //then
    }
}
