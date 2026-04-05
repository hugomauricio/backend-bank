package com.account_service.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CustomerAccountReportResult {

    private final Long customerId;
    private final List<AccountReportItemResult> accounts;
}