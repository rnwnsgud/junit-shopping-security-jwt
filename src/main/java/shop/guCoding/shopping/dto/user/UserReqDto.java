package shop.guCoding.shopping.dto.user;


import lombok.Getter;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserEnum;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserReqDto {

    @Getter
    @Setter
    public static class JoinReqDto {

        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성해주세요.")
        @NotEmpty
        private String username;
        @NotEmpty
        @Size(min = 4, max = 20)
        private String password;

        @Pattern(regexp = "^^[a-zA-Z0-9]{2,10}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}$", message = "이메일 형식으로 작성해주세요.")
        @NotEmpty
        private String email;

        @Pattern(regexp = "^[a-zA-Zㄱ-ㅎ가-힣]{1,20}$", message = "영문/한글 1~20자 이내로 작성해주세요.")
        @NotEmpty
        private String fullname;

        public User toEntity(BCryptPasswordEncoder bcryptPasswordEncoder) {
            return User.builder()
                    .username(username)
                    .password(bcryptPasswordEncoder.encode(password))
                    .email(email)
                    .fullname(fullname)
                    .role(UserEnum.CUSTOMER)
                    .build();
        }
    }
}
