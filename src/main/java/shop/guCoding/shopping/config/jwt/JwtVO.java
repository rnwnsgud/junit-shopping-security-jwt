package shop.guCoding.shopping.config.jwt;

public interface JwtVO {
    public static final String SECRET = "구코딩"; // HS256 대칭키
    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 1주일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ACCESS_HEADER = "ACCESS_HEADER";
    public static final String REFRESH_HEADER = "REFRESH_HEADER";
}
