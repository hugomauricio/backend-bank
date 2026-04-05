package com.account_service.domain.port.in;

import com.account_service.application.dto.CustomerAccountReportResult;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface GetReportUseCase {

    Mono<CustomerAccountReportResult> getReport(Long customerId, LocalDate startDate, LocalDate endDate);
}