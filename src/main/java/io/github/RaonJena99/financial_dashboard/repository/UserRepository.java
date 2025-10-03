package io.github.RaonJena99.financial_dashboard.repository;

import io.github.RaonJena99.financial_dashboard.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
