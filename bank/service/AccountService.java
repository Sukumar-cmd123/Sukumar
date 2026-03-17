package com.bank.service;
import com.bank.dto.response.BalanceResponse;
import com.bank.dto.response.DepositResponse;
import com.bank.dto.response.TransferResponse;
import com.bank.dto.response.WithdrawResponse;
import com.bank.dto.response.TransactionResponse;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
	DepositResponse deposit(String accountNumber, BigDecimal amount);

	WithdrawResponse withdraw(String accountNumber, BigDecimal amount);

	TransferResponse transfer(String fromAccount, String toAccount, BigDecimal amount);

	BalanceResponse balance(String accountNumber);

	List<TransactionResponse> transactions(String accountNumber);
}

