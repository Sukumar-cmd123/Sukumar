package com.bank.controller;

import java.util.List;
import com.bank.dto.response.*;
import com.bank.audit.Auditable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.bank.service.AccountService;
import com.bank.dto.request.DepositRequest;
import com.bank.dto.request.TransferRequest;
import com.bank.dto.request.WithdrawRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

	@PostMapping("/deposit")
  //  @PreAuthorize("hasAnyAuthority('CUSTOMER')")
	@Auditable(action = "DEPOSIT")
	public ResponseEntity<DepositResponse> deposit(@Valid @RequestBody DepositRequest request) {
		return ResponseEntity.ok(accountService.deposit(request.getAccountNumber(), request.getAmount()));
	}

	@PostMapping("/withdraw")
	@Auditable(action = "WITHDRAW")
	public ResponseEntity<WithdrawResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
		return ResponseEntity.ok(accountService.withdraw(request.getAccountNumber(), request.getAmount()));
	}

	@PostMapping("/transfer")
	@Auditable(action = "TRANSFER")
	public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
		return ResponseEntity.ok(accountService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount()));
	}

	@GetMapping("/balance/{accountNumber}")
	public ResponseEntity<BalanceResponse> balance(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.balance(accountNumber));
	}

	@GetMapping("/transactions/{accountNumber}")
	public ResponseEntity<List<TransactionResponse>> transactions(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.transactions(accountNumber));
	}
}

