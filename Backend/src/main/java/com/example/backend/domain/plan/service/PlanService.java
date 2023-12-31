package com.example.backend.domain.plan.service;

import com.example.backend.domain.account.Account;
import com.example.backend.domain.account.exception.UserNotFoundException;
import com.example.backend.domain.account.repository.AccountRepository;
import com.example.backend.domain.plan.dto.PlanDetailResponseDto;
import com.example.backend.domain.plan.dto.PlanGetResponseDto;
import com.example.backend.domain.plan.dto.PlanSaveRequestDto;
import com.example.backend.domain.plan.dto.PlanUpdateRequestDto;
import com.example.backend.domain.plan.entity.Plan;
import com.example.backend.domain.plan.exception.PlanNotFoundException;
import com.example.backend.domain.plan.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final AccountRepository accountRepository;

    public void planSave(PlanSaveRequestDto planSaveRequestDto, String userNumber) {
        //Header User-Number를 통해서 계좌 ID를 받아옴
        Account account = accountRepository.findAccountByUserNumber(userNumber)
                .orElseThrow(UserNotFoundException::new);


        Plan plan = new Plan(
                null,
                planSaveRequestDto.getDataBody().getStartDate(),
                planSaveRequestDto.getDataBody().getEndDate(),
                account,
                null,
                null
        );

        planRepository.save(plan);

    }

    public void planUpdate(PlanUpdateRequestDto planUpdateRequestDto, String userNumber, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("생성되지 않은 여행계획입니다."));

        System.out.println(plan.getId());
        // 해당 계획과 해당하는 예산 지우기
        planRepository.delete(plan);

        //Header User-Number를 통해서 계좌 ID를 받아옴
        Account account = accountRepository.findAccountByUserNumber(userNumber)
                .orElseThrow(UserNotFoundException::new);


        Plan updatePlan = new Plan(
                null,
                planUpdateRequestDto.getDataBody().getStartDate(),
                planUpdateRequestDto.getDataBody().getEndDate(),
                account,
                null,
                null
        );

        planRepository.save(updatePlan);

    }

    public PlanGetResponseDto planGet(String userNumber) {
        //Header User-Number를 통해서 계좌 ID를 받아옴
        Account account = accountRepository.findAccountByUserNumber(userNumber)
                .orElseThrow(UserNotFoundException::new);
        LocalDate today = LocalDate.now();

        PageRequest pageRequest = PageRequest.of(0, 1);
        Plan plan = planRepository.findNotEndPlan(account, today, pageRequest)
                .get()
                .findFirst()
                .orElseThrow(PlanNotFoundException::new);

        return PlanGetResponseDto.builder()
                .planId(plan.getId())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .build();
    }


    public PlanDetailResponseDto planDetail(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("생성되지 않은 여행계획입니다."));

        return PlanDetailResponseDto.builder()
                .accountId(plan.getAccount().getId())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .build();

    }


    public List<Plan> planList(Long accountId) {
        return planRepository.findAllByAccountId(accountId);
    }


}
