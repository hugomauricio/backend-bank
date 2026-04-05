package com.account_service.application.service;

import com.account_service.application.dto.AccountReportItemResult;
import com.account_service.application.dto.CustomerAccountReportResult;
import com.account_service.application.dto.MovementResult;
import com.account_service.domain.port.in.GetReportUseCase;
import com.account_service.domain.port.out.AccountQueryPort;
import com.account_service.domain.port.out.MovementQueryPort;
import com.account_service.infrastructure.utput.persistence.AccountPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportApplicationService implements GetReportUseCase {

    private final AccountQueryPort accountQueryPort;
    private final MovementQueryPort movementQueryPort;
    private final AccountPersistenceMapper mapper;

    @Override
    public Mono<CustomerAccountReportResult> getReport(Long customerId, LocalDate startDate, LocalDate endDate) {
        return Mono.fromCallable(() -> {
                    LocalDateTime startDateTime = startDate.atStartOfDay();
                    LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

                    List<AccountReportItemResult> accounts = accountQueryPort.findAllByCustomerId(customerId)
                            .stream()
                            .map(account -> {
                                List<MovementResult> movements = movementQueryPort
                                        .findByAccountIdAndDateBetween(account.getId(), startDateTime, endDateTime)
                                        .stream()
                                        .map(mapper::toMovementResult)
                                        .toList();

                                return AccountReportItemResult.builder()
                                        .accountId(account.getId())
                                        .number(account.getNumber())
                                        .type(account.getType())
                                        .initialBalance(account.getInitialBalance())
                                        .currentBalance(account.getCurrentBalance())
                                        .status(account.getStatus())
                                        .movements(movements)
                                        .build();
                            })
                            .toList();

                    return CustomerAccountReportResult.builder()
                            .customerId(customerId)
                            .accounts(accounts)
                            .build();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}