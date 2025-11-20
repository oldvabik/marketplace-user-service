package org.oldvabik.userservice.repository;

import org.oldvabik.userservice.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByNumber(String number);

    @Query("SELECT c FROM Card c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Card> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT c FROM Card c JOIN FETCH c.user u LEFT JOIN FETCH u.cards WHERE c.id = :id")
    Optional<Card> findByIdWithUserWithCards(@Param("id") Long id);

    Page<Card> findAll(Pageable pageable);
}
