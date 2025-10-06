package io.github.RaonJena99.financial_dashboard.service;

import io.github.RaonJena99.financial_dashboard.domain.Transaction;
import java.util.List;
import java.util.Optional;

// 인터페이스
public interface TransactionService {
  Transaction createTransaction(Transaction transaction);

  List<Transaction> getAllTransactions();

  Optional<Transaction> getTransactionById(Long id);

  Transaction updateTransaction(Long id, Transaction transactionDetails);

  void deleteTransaction(Long id);
}
