package com.account_service.infrastructure.input.rest;

import com.account_service.application.dto.AccountReportItemResult;
import com.account_service.application.dto.CustomerAccountReportResult;
import com.account_service.domain.port.in.GetReportUseCase;
import com.account_service.infrastructure.mapper.AccountContractMapper;
import com.bank.customer.generated.api.ReportsApi;
import com.bank.customer.generated.model.AccountReportItem;
import com.bank.customer.generated.model.CustomerReportResponse;
import com.bank.customer.generated.model.MovementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ReportController implements ReportsApi {

    private final GetReportUseCase getReportUseCase;
    private final AccountContractMapper mapper;

    @Override
    public Mono<ResponseEntity<CustomerReportResponse>> getCustomerReport(
            Long customerId,
            LocalDate startDate,
            LocalDate endDate,
            ServerWebExchange exchange) {

        return getReportUseCase.getReport(customerId, startDate, endDate)
                .map(this::toResponse)
                .map(ResponseEntity::ok);
    }

    private CustomerReportResponse toResponse(CustomerAccountReportResult result) {
        CustomerReportResponse response = new CustomerReportResponse();
        response.setCustomerId(result.getCustomerId());

        List<AccountReportItem> accounts = result.getAccounts()
                .stream()
                .map(this::toAccountReportItem)
                .toList();

        response.setAccounts(accounts);
        return response;
    }

    private AccountReportItem toAccountReportItem(AccountReportItemResult result) {
        AccountReportItem item = new AccountReportItem();
        item.setAccountId(result.getAccountId());
        item.setNumber(result.getNumber());
        item.setType(result.getType().name());
        item.setInitialBalance(result.getInitialBalance().doubleValue());
        item.setCurrentBalance(result.getCurrentBalance().doubleValue());

        List<MovementResponse> movements = result.getMovements()
                .stream()
                .map(mapper::toMovementResponse)
                .toList();

        item.setMovements(movements);
        return item;
    }
}