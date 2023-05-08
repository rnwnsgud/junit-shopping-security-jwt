package shop.guCoding.shopping.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.guCoding.shopping.domain.account.Account;
import shop.guCoding.shopping.domain.account.AccountRepository;
import shop.guCoding.shopping.domain.transaction.Transaction;
import shop.guCoding.shopping.domain.transaction.TransactionEnum;
import shop.guCoding.shopping.domain.transaction.TransactionRepository;
import shop.guCoding.shopping.domain.user.User;
import shop.guCoding.shopping.domain.user.UserRepository;
import shop.guCoding.shopping.dto.account.AccountReqDto;
import shop.guCoding.shopping.dto.account.AccountReqDto.*;
import shop.guCoding.shopping.dto.account.AccountRespDto;
import shop.guCoding.shopping.dto.account.AccountRespDto.*;
import shop.guCoding.shopping.handler.ex.CustomApiException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountSaveRespDto 계좌등록(AccountSaveReqDto accountSaveReqDto, Long userId) {
        User userPS = userRepository.findById(userId).orElseThrow(() -> new CustomApiException("유저를 찾을 수 없습니다."));
        Optional<Account> accountOP = accountRepository.findByNumber(accountSaveReqDto.getNumber());
        if (accountOP.isPresent()) {
            throw new CustomApiException("해당 계좌가 이미 존재합니다.");
        }

        Account accountPS = accountRepository.save(accountSaveReqDto.toEntity(userPS));

        return new AccountSaveRespDto(accountPS);
    }

    public AccountListRespDto 계좌목록보기_유저별(Long userId) {
        User userPS = userRepository.findById(userId).orElseThrow(
                () -> new CustomApiException("유저를 찾을 수 없습니다.")
        );

        // 유저의 모든 계좌 목록
        List<Account> accountListPS = accountRepository.findByUser_id(userId);
        return new AccountListRespDto(userPS, accountListPS);
    }

    public void 계좌삭제(Long number, Long userId) {
        Account accountPS = accountRepository.findByNumber(number).orElseThrow(
                () -> new CustomApiException("계좌를 찾을 수 없습니다.")
        );

        accountPS.checkOwner(userId);

        accountRepository.deleteById(accountPS.getId());

    }

    // 인증 필요 없음
    public AccountDepositRespDto 계좌입금(AccountDepositReqDto accountDepositReqDto) {
        if (accountDepositReqDto.getAmount() <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다.");
        }
        Account depositAccountPS = accountRepository.findByNumber(accountDepositReqDto.getNumber())
                .orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다."));
        depositAccountPS.deposit(accountDepositReqDto.getAmount());

        Transaction transaction = Transaction.builder()
                .withdrawAccount(null)
                .depositAccount(depositAccountPS)
                .withdrawAccountBalance(null)
                .depositAccountBalance(depositAccountPS.getBalance())
                .amount(accountDepositReqDto.getAmount())
                .division(TransactionEnum.DEPOSIT)
                .sender("ATM")
                .receiver(accountDepositReqDto.getNumber() + "") // long값 string으로 간단히 넣는 스킬
                .tel(accountDepositReqDto.getTel())
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);
        return new AccountDepositRespDto(depositAccountPS, transactionPS);


    }

    public AccountWithdrawRespDto 계좌출금(AccountWithdrawReqDto accountWithdrawReqDto, Long userId) {
        if (accountWithdrawReqDto.getAmount() <= 0) {
            throw new CustomApiException("0원 이하의 금액을 출금할 수 없습니다.");
        }

        Account withdrawAccountPS = accountRepository.findByNumber(accountWithdrawReqDto.getNumber())
                .orElseThrow(() -> new CustomApiException("계좌를 찾을 수 없습니다."));

        withdrawAccountPS.checkOwner(userId);

        withdrawAccountPS.checkSamePassword(accountWithdrawReqDto.getPassword());

        withdrawAccountPS.withdraw(accountWithdrawReqDto.getAmount()); // 출금계좌 잔액확인 메서드 포함

        Transaction transaction = Transaction.builder()
                .withdrawAccount(withdrawAccountPS)
                .depositAccount(null)
                .withdrawAccountBalance(withdrawAccountPS.getBalance())
                .depositAccountBalance(null)
                .amount(accountWithdrawReqDto.getAmount())
                .division(TransactionEnum.WITHDRAW)
                .sender(accountWithdrawReqDto.getNumber() + "")
                .receiver("ATM")
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);

        return new AccountWithdrawRespDto(withdrawAccountPS, transactionPS);

    }

    public AccountTransferRespDto 계좌이체(AccountTransferReqDto accountTransferReqDto, Long userId) {

        // 출금계좌 != 입금계좌
        if (accountTransferReqDto.getWithdrawNumber().longValue() == accountTransferReqDto.getDepositNumber().longValue()) {
            throw new CustomApiException("입출금계좌가 동일할 수 없습니다");
        }

        if (accountTransferReqDto.getAmount() <= 0L) {
            throw new CustomApiException("0원 이하의 금액을 입금할 수 없습니다");
        }

        Account withdrawAccountPS = accountRepository.findByNumber(accountTransferReqDto.getWithdrawNumber())
                .orElseThrow(() -> new CustomApiException("출금계좌를 찾을 수 없습니다"));

        Account depositAccountPS = accountRepository.findByNumber(accountTransferReqDto.getDepositNumber())
                .orElseThrow(() -> new CustomApiException("입금계좌를 찾을 수 없습니다."));

        withdrawAccountPS.checkOwner(userId);

        withdrawAccountPS.checkSamePassword(accountTransferReqDto.getWithdrawPassword());

        withdrawAccountPS.withdraw(accountTransferReqDto.getAmount());
        depositAccountPS.deposit(accountTransferReqDto.getAmount());

        Transaction transaction = Transaction.builder()
                .withdrawAccount(withdrawAccountPS)
                .depositAccount(depositAccountPS)
                .withdrawAccountBalance(withdrawAccountPS.getBalance())
                .depositAccountBalance(depositAccountPS.getBalance())
                .amount(accountTransferReqDto.getAmount())
                .division(TransactionEnum.TRANSFER)
                .sender(accountTransferReqDto.getWithdrawNumber() + "")
                .receiver(accountTransferReqDto.getDepositNumber() + "")
                .build();

        Transaction transactionPS = transactionRepository.save(transaction);

        return new AccountTransferRespDto(withdrawAccountPS, transactionPS);


    }

    public AccountDetailRespDto 계좌상세보기(Long number, Long userId, Integer page, String division) {

        Account accountPS = accountRepository.findByNumber(number).orElseThrow(
                () -> new CustomApiException("계좌를 찾을 수 없습니다."));

        accountPS.checkOwner(userId);

        List<Transaction> transactionList = transactionRepository.findTransactionList(accountPS.getId(), division, page);
        return new AccountDetailRespDto(accountPS, transactionList);
    }



}
