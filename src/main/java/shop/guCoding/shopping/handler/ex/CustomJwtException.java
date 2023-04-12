package shop.guCoding.shopping.handler.ex;

public class CustomJwtException extends RuntimeException {
    public CustomJwtException(String message) {
        super(message);
    }
}
