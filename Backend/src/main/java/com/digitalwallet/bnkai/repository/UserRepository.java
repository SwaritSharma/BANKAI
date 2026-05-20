package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.projection.UserDashboardProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(
        path = "users",
        excerptProjection =
                UserDashboardProjection.class
)
public interface UserRepository
        extends JpaRepository<User, Integer> {

    boolean existsByEmail(
            String email
    );

    Optional<User> findByEmail(
            String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :userId")
    Optional<User> findByUserIdForUpdate(
            @Param("userId") Integer userId
    );
}