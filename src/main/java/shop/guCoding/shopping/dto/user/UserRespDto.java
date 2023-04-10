package shop.guCoding.shopping.dto.user;

import lombok.Getter;
import lombok.Setter;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.util.CustomDateUtil;

public class UserRespDto {

    @Getter
    @Setter
    public static class JoinRespDto {
        private Long id;
        private String username;
        private String fullname;

        public JoinRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullname = user.getFullname();
        }
    }

    @Getter
    @Setter
    public static class LoginRespDto {
        private Long id;
        private String username;
        private String email;
        private String createdAt;

        public LoginRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.createdAt = CustomDateUtil.toStringFormat(user.getCreatedAt());
        }
    }
}
