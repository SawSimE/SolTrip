package com.example.backend.domain.payment.service;

import com.example.backend.domain.account.Account;
import com.example.backend.domain.account.exception.UserNotFoundException;
import com.example.backend.domain.account.repository.AccountRepository;
import com.example.backend.domain.common.exception.ResourceNotFoundException;
import com.example.backend.domain.common.redis.service.RedisService;
import com.example.backend.domain.payment.Payment;
import com.example.backend.domain.payment.dto.LatestDateTimeResponseDto;
import com.example.backend.domain.payment.dto.TransactionHistoryRequestDto;
import com.example.backend.domain.payment.dto.TransactionHistoryResponseDto;
import com.example.backend.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    private final RedisService redisService;
    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void registerTransactionHistory(String userNumber, TransactionHistoryRequestDto transactionHistoryRequestDto) {
        Account account = getAccount(userNumber);

        List<TransactionHistoryRequestDto.Transaction> transactionHistory = transactionHistoryRequestDto.getDataBody().getTransactionHistory();

        Optional<TransactionHistoryRequestDto.Transaction> latestTransaction = transactionHistory.stream()
                .max(Comparator.comparing(TransactionHistoryRequestDto.Transaction::getTransactionDate)
                        .thenComparing(TransactionHistoryRequestDto.Transaction::getTransactionTime));

        // 확인된 가장 최신 결제 시간 redis 저장
        latestTransaction.ifPresent(transaction -> updateLatestDateTimeInRedis(userNumber, transaction));

        List<Payment> payments = convertTransactionsToPayments(transactionHistory, account);

        paymentRepository.saveAll(payments);
    }

    public List<TransactionHistoryResponseDto> getTransactionHistory(String userNumber) {
        Account account = getAccount(userNumber);

        List<Payment> payments = paymentRepository.findByAccountId(account.getId());
        return payments.stream()
                .map(payment -> TransactionHistoryResponseDto.builder()
                        .id(payment.getId())
                        .content(payment.getContent())
                        .amount(payment.getAmount())
                        .storeName(payment.getStoreName())
                        .transactionDate(payment.getTransactionDate())
                        .transactionTime(payment.getTransactionTime())
                        .paymentType(payment.getPaymentType()).build())
                .collect(Collectors.toList());
    }

    private List<Payment> convertTransactionsToPayments(List<TransactionHistoryRequestDto.Transaction> transactionHistory, Account account) {
        return transactionHistory.stream()
                .map(transaction -> Payment.createPayment(
                        transaction.getContent(),
                        transaction.getAmount(),
                        transaction.getStoreName(),
                        transaction.getTransactionDate(),
                        transaction.getTransactionTime(),
                        transaction.getPaymentType(),
                        account))
                .collect(Collectors.toList());
    }

    private Account getAccount(String userNumber) {
        return accountRepository.findAccountByUserNumber(userNumber)
                .orElseThrow(UserNotFoundException::new);
    }

    private void updateLatestDateTimeInRedis(String userNumber, TransactionHistoryRequestDto.Transaction transaction) {
        Map<String, String> map = Map.of(
                "latestDate", transaction.getTransactionDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                "latestTime", transaction.getTransactionTime().format(DateTimeFormatter.ofPattern("HHmmss")));
        redisService.setHash(userNumber, map);
    }

    public LatestDateTimeResponseDto getLatestDateTime(String userNumber) {
        Map<Object, Object> hash = redisService.getHash(userNumber);
        if (hash.isEmpty()) {
            throw new ResourceNotFoundException("Payment.getLatestDateTime", userNumber);
        }
        String latestDate = (String) hash.get("latestDate");
        String latestTime = (String) hash.get("latestTime");

        return LatestDateTimeResponseDto.builder()
                .transactionDate(LocalDate.parse(latestDate, DateTimeFormatter .ofPattern("yyyyMMdd")))
                .transactionTime(LocalTime.parse(latestTime, DateTimeFormatter .ofPattern("HHmmss"))).build();
    }

    @Transactional
    public void updateTransactionContent(String userNumber, Long transactionId, String content) {
        Account account = getAccount(userNumber);

        Payment payment = paymentRepository.findByAccountAndId(account, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", transactionId));

        payment.updateContent(content);
    }
}
