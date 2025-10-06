package io.github.RaonJena99.financial_dashboard.controller;

import io.github.RaonJena99.financial_dashboard.domain.Transaction;
import io.github.RaonJena99.financial_dashboard.service.TransactionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  @Autowired private TransactionService transactionService;

  // 모든 거래 내역 조회 (GET /api/transactions)
  @GetMapping
  public List<Transaction> getAllTransactions() {
    return transactionService.getAllTransactions();
  }

  // 특정 거래 내역 조회 (GET /api/transactions/{id})
  @GetMapping("/{id}")
  public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
    return transactionService
        .getTransactionById(id)
        .map(ResponseEntity::ok) // 거래 내역이 있으면 200 OK 응답
        .orElse(ResponseEntity.notFound().build()); // 없으면 404 Not Found 응답
  }

  // 새 거래 내역 생성 (POST /api/transactions)
  @PostMapping
  public Transaction createTransaction(@RequestBody Transaction transaction) {
    return transactionService.createTransaction(transaction);
  }

  // 거래 내역 수정 (PUT /api/transactions/{id})
  @PutMapping("/{id}")
  public ResponseEntity<Transaction> updateTransaction(
      @PathVariable Long id, @RequestBody Transaction transactionDetails) {
    try {
      Transaction updatedTransaction = transactionService.updateTransaction(id, transactionDetails);
      return ResponseEntity.ok(updatedTransaction);
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  // 거래 내역 삭제 (DELETE /api/transactions/{id})
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
    try {
      transactionService.deleteTransaction(id);
      return ResponseEntity.noContent().build(); // 성공 시 204 No Content 응답
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
