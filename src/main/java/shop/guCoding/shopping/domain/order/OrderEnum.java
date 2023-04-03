package shop.guCoding.shopping.domain.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderEnum {
    DELIVERY("배송중"), PROGRESS("처리중"), COMPLETE("배송완료"), CANCEL("주문취소");
    private String value;
}
