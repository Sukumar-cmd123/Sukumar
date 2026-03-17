package com.bank.service.impl;

import com.bank.dto.response.*;
import com.bank.entity.Customer;
import com.bank.entity.Transaction;
import com.bank.entity.TransactionStatus;
import com.bank.entity.TransactionType;
import com.bank.exception.AccountNotFoundException;
import com.bank.exception.InsufficientBalanceException;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
	private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

	private final CustomerRepository customerRepository;
	private final TransactionRepository transactionRepository;

	@Override
	@Transactional
	public DepositResponse deposit(String accountNumber, BigDecimal amount) {
		log.info("Deposit request received for account {}", accountNumber);

		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Deposit amount must be positive and non-null");
		}

		Customer c = customerRepository.findByAccountNumberForUpdate(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

		BigDecimal newBalance = c.getBalance().add(amount);
		c.setBalance(newBalance);
		customerRepository.save(c);

		transactionRepository.save(Transaction.builder()
				.accountNumber(accountNumber)
				.transactionType(TransactionType.DEPOSIT)
				.amount(amount)
				.balanceAfterTransaction(newBalance)
				.status(TransactionStatus.SUCCESS)
				.build());

		return new DepositResponse("Deposit successful", newBalance);
	}

	@Override
	@Transactional
	public WithdrawResponse withdraw(String accountNumber, BigDecimal amount) {
		log.info("Withdraw request received for account {}", accountNumber);
		Customer c = customerRepository.findByAccountNumberForUpdate(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

		if (c.getBalance().compareTo(amount) < 0) {
			transactionRepository.save(Transaction.builder()
					.accountNumber(accountNumber)
					.transactionType(TransactionType.WITHDRAW)
					.amount(amount)
					.balanceAfterTransaction(c.getBalance())
					.status(TransactionStatus.FAILED)
					.build());
			throw new InsufficientBalanceException("Insufficient balance");
		}

		BigDecimal newBalance = c.getBalance().subtract(amount);
		c.setBalance(newBalance);
		customerRepository.save(c);

		transactionRepository.save(Transaction.builder()
				.accountNumber(accountNumber)
				.transactionType(TransactionType.WITHDRAW)
				.amount(amount)
				.balanceAfterTransaction(newBalance)
				.status(TransactionStatus.SUCCESS)
				.build());

		return new WithdrawResponse("Withdrawal successful", newBalance);
	}

	@Override
	@Transactional
	public TransferResponse transfer(String fromAccount, String toAccount, BigDecimal amount) {
		log.info("Transfer request received from {} to {}", fromAccount, toAccount);
		if (fromAccount.equals(toAccount)) {
			throw new IllegalArgumentException("fromAccount and toAccount must be different");
		}

		// Lock accounts in a stable order to reduce deadlock probability.
		List<String> ordered = List.of(fromAccount, toAccount).stream().sorted(Comparator.naturalOrder()).toList();
		Customer first = customerRepository.findByAccountNumberForUpdate(ordered.get(0))
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + ordered.get(0)));
		Customer second = customerRepository.findByAccountNumberForUpdate(ordered.get(1))
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + ordered.get(1)));

		Customer from = fromAccount.equals(first.getAccountNumber()) ? first : second;
		Customer to = toAccount.equals(first.getAccountNumber()) ? first : second;

		if (from.getBalance().compareTo(amount) < 0) {
			transactionRepository.save(Transaction.builder()
					.accountNumber(fromAccount)
					.transactionType(TransactionType.TRANSFER_OUT)
					.amount(amount)
					.balanceAfterTransaction(from.getBalance())
					.status(TransactionStatus.FAILED)
					.build());
			throw new InsufficientBalanceException("Insufficient balance");
		}

		BigDecimal fromNew = from.getBalance().subtract(amount);
		BigDecimal toNew = to.getBalance().add(amount);
		from.setBalance(fromNew);
		to.setBalance(toNew);
		customerRepository.save(from);
		customerRepository.save(to);

		transactionRepository.save(Transaction.builder()
				.accountNumber(fromAccount)
				.transactionType(TransactionType.TRANSFER_OUT)
				.amount(amount)
				.balanceAfterTransaction(fromNew)
				.status(TransactionStatus.SUCCESS)
				.build());
		transactionRepository.save(Transaction.builder()
				.accountNumber(toAccount)
				.transactionType(TransactionType.TRANSFER_IN)
				.amount(amount)
				.balanceAfterTransaction(toNew)
				.status(TransactionStatus.SUCCESS)
				.build());

		return new TransferResponse("Transfer successful");
	}

	@Override
	@Transactional(readOnly = true)
	public BalanceResponse balance(String accountNumber) {
		Customer c = customerRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
		return new BalanceResponse(accountNumber, c.getBalance());
	}

	@Override
	@Transactional(readOnly = true)
	public List<TransactionResponse> transactions(String accountNumber) {
		if (customerRepository.findByAccountNumber(accountNumber).isEmpty()) {
			throw new AccountNotFoundException("Account not found: " + accountNumber);
		}
		return transactionRepository.findByAccountNumberOrderByTransactionDateDesc(accountNumber)
				.stream()
				.map(t -> new TransactionResponse(
						t.getTransactionId(),
						t.getAccountNumber(),
						t.getTransactionType(),
						t.getAmount(),
						t.getBalanceAfterTransaction(),
						t.getTransactionDate(),
						t.getStatus()
				))
				.toList();
	}
}

