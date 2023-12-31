package com.example.backend.global.scheduler;

import com.example.backend.domain.budget.entity.Budget;
import com.example.backend.domain.budget.repository.BudgetRepository;
import com.example.backend.domain.common.exception.ResourceNotFoundException;
import com.example.backend.domain.common.redis.service.RedisService;
import com.example.backend.domain.payment.Payment;
import com.example.backend.domain.payment.PaymentType;
import com.example.backend.domain.payment.repository.PaymentRepository;
import com.example.backend.domain.plan.entity.Plan;
import com.example.backend.domain.plan.repository.PlanRepository;
import com.example.backend.domain.portfolio.dto.ShinhanTransactionRequestDto;
import com.example.backend.domain.portfolio.service.PortfolioService;
import com.example.backend.global.scheduler.dto.SOLPushNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BudgetScheduler {

    private final PlanRepository planRepository;
    private final BudgetRepository budgetRepository;
    private final PaymentRepository paymentRepository;
    private final RedisService redisService;
    private final PortfolioService portfolioService;

    @Scheduled(cron = "1 0 0 * * *")
    public void saveTodayBudgetToRedis() {
        LocalDate today = LocalDate.now();

        List<Plan> plans = planRepository.findByDate(today);

        for (Plan plan : plans) {
            // 여행있는 계좌의 오늘 예산 계획 불러오기
            List<Budget> budgets = budgetRepository.findAllByPlanAndTravelDate(plan, today);
            long totalAmount = budgets.stream().mapToLong(Budget::getAmount).sum();

            redisService.setValues(86500, "todayBudget_" + plan.getAccount().getNumber(), String.valueOf(totalAmount));
            redisService.setValues(86500, "todayBudgetOver_" + plan.getAccount().getNumber(), "N");
        }
    }

    @Scheduled(cron = "59 */10 * * * *")
    public void callAccountHistoryApi() {
        LocalDate today = LocalDate.now();

        List<Plan> plans = planRepository.findByDate(today);

        for (Plan plan : plans) {
            // 오늘 여행을 가야하는 계좌의 최근 조회 날짜 불러오기(redis)
            Map<Object, Object> hash = redisService.getHash(plan.getAccount().getUserNumber());

            ShinhanTransactionRequestDto shinhanTransactionRequestDto = portfolioService.transactionHistoryInquiry(plan.getAccount().getNumber());
            List<ShinhanTransactionRequestDto.DataBody.TransactionHistory> transactions = shinhanTransactionRequestDto.getDataBody().getTransactions();

            // 처음이면 불러온 값 다 저장
            List<Payment> payments;
            if (hash.isEmpty()) {
                payments = shinhanTransactionRequestDto.getDataBody().getTransactions().stream()
                        .map(transaction -> Payment.createPayment(
                                transaction.getDetail(),
                                transaction.getWithdrawalAmount(),
                                transaction.getStoreName(),
                                LocalDate.parse(transaction.getTransactionDate(), DateTimeFormatter.ofPattern("yyyyMMdd")),
                                LocalTime.parse(transaction.getTransactionTime(), DateTimeFormatter.ofPattern("HHmmss")),
                                PaymentType.CARD,
                                plan.getAccount()
                        )).collect(Collectors.toList());

                Optional<ShinhanTransactionRequestDto.DataBody.TransactionHistory> latestTransaction = shinhanTransactionRequestDto.getDataBody().getTransactions().stream()
                        .max(Comparator.comparing(ShinhanTransactionRequestDto.DataBody.TransactionHistory::getTransactionDate)
                                .thenComparing(ShinhanTransactionRequestDto.DataBody.TransactionHistory::getTransactionTime));

                latestTransaction.ifPresent(transaction -> {
                    Map<String, String> map = Map.of(
                            "latestDate", transaction.getTransactionDate(),
                            "latestTime", transaction.getTransactionTime());
                    redisService.setHash(plan.getAccount().getUserNumber(), map);
                });

            } else {
                payments = new ArrayList<>();
                String latestDate = hash.get("latestDate").toString();
                String latestTime = hash.get("latestTime").toString();
                for (ShinhanTransactionRequestDto.DataBody.TransactionHistory transaction : transactions) {
                    if (transaction.getTransactionDate().equals(latestDate) &&
                            transaction.getTransactionTime().equals(latestTime)) break;
                    if (transaction.getLocationClassification() == 1) continue;

                    payments.add(Payment.createPayment(
                            transaction.getDetail(),
                            transaction.getWithdrawalAmount(),
                            transaction.getStoreName(),
                            LocalDate.parse(transaction.getTransactionDate(), DateTimeFormatter.ofPattern("yyyyMMdd")),
                            LocalTime.parse(transaction.getTransactionTime(), DateTimeFormatter.ofPattern("HHmmss")),
                            PaymentType.CARD,
                            plan.getAccount()));
                }
            }
            paymentRepository.saveAll(payments);

            // 알림을 보낸 적 있으면 보내지 않기
            if (redisService.getValues("todayBudget_" + plan.getAccount().getNumber()).equals("Y")) continue;

            // 오늘의 예산과 사용내역을 가져와서 비교해서 초과 알림 여부 결정
            String redis = redisService.getValues("todayBudget_" + plan.getAccount().getNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Redis", "todayBudget_" + plan.getAccount().getNumber()));
            long todayBudget = Long.parseLong(redis);
            List<Payment> paymentList = paymentRepository.findByAccountAndTransactionDate(plan.getAccount(), today);

            long totalAmount = paymentList.stream()
                    .mapToLong(Payment::getAmount)
                    .sum();

            String userNumber = plan.getAccount().getUserNumber();
            String username = plan.getAccount().getUsername();
            if (todayBudget <= totalAmount) {
                SOLPushNotificationResponse solPushNotificationResponse = callPushNotificationApi(userNumber, username);
                if (solPushNotificationResponse.getDataBody().get수행결과().equals("Y")) {
                    redisService.setValues(86500, "todayBudgetOver_" + plan.getAccount().getNumber(), "Y");
                }
            }
        }
    }

    private SOLPushNotificationResponse callPushNotificationApi(String userNumber, String username) {
        Map<String, String> dataHeader = new HashMap<>();
        dataHeader.put("apikey", "2023_Shinhan_SSAFY_Hackathon");

        Map<String, String> dataBody = new HashMap<>();
        dataBody.put("제휴고객번호", userNumber);
        dataBody.put("발송메시지", username);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("dataHeader", dataHeader);
        requestBody.put("dataBody", dataBody);

        String url = "https://shbhack.shinhan.com/v1/notice/sol-push";

        // RestTemplate 사용
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<SOLPushNotificationResponse> response =
                restTemplate.postForEntity(url, entity, SOLPushNotificationResponse.class);

        return response.getBody();
    }
}
