package io.github.RaonJena99.financial_dashboard.service;

import io.github.RaonJena99.financial_dashboard.domain.Transaction;
import io.github.RaonJena99.financial_dashboard.repository.TransactionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // 이 클래스가 서비스 계층의 컴포넌트임을 명시
public class TransactionServiceImpl implements TransactionService {

  @Autowired // TransactionRepository 의존성 주입
  private TransactionRepository transactionRepository;

  @Override
  public Transaction createTransaction(Transaction transaction) {
    return transactionRepository.save(transaction);
  }

  @Override
  public List<Transaction> getAllTransactions() {
    return transactionRepository.findAll();
  }

  @Override
  public Optional<Transaction> getTransactionById(Long id) {
    return transactionRepository.findById(id);
  }

  @Override
  public Transaction updateTransaction(Long id, Transaction transactionDetails) {
    // ID로 기존 거래 내역을 찾습니다.
    Transaction transaction =
        transactionRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

    // 받은 정보로 필드를 업데이트합니다.
    transaction.setTransactionDate(transactionDetails.getTransactionDate());
    transaction.setAmount(transactionDetails.getAmount());
    transaction.setMerchantName(transactionDetails.getMerchantName());
    transaction.setType(transactionDetails.getType());
    transaction.setCategory(transactionDetails.getCategory());

    // 업데이트된 거래 내역을 저장합니다.
    return transactionRepository.save(transaction);
  }

  @Override
  public void deleteTransaction(Long id) {
    Transaction transaction =
        transactionRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    transactionRepository.delete(transaction);
  }
}
