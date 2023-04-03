package shop.guCoding.shopping.domain.item;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;


@DiscriminatorValue("SHIRT")
@Getter
@NoArgsConstructor
@Entity
public class Shirt extends Item{

    // 길, 중간, 짧
    private String length;
    private String color;

    public Shirt(String length, String color) {
        this.length = length;
        this.color = color;
    }

    // 인텔리제이가 알아서 부모생성자 호출해주네
    @Builder
    public Shirt(Long id, String name, int price, int stockQuantity, LocalDateTime createdAt, LocalDateTime updatedAt, String length, String color) {
        super(id, name, price, stockQuantity, createdAt, updatedAt);
        this.length = length;
        this.color = color;
    }
}
