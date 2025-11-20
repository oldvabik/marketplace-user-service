package org.oldvabik.userservice.repository;

import org.oldvabik.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.email = :email")
    Optional<User> findByEmailWithCards(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards WHERE u.id = :id")
    Optional<User> findByIdWithCards(Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cards")
    Page<User> findAllWithCards(Pageable pageable);
}
