package shop.guCoding.shopping.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByNumber(Long number);
    List<Account> findByUser_id(Long id);
}
