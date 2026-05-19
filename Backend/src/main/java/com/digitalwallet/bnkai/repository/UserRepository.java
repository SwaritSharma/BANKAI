package com.digitalwallet.bnkai.repository;


import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.projection.UserDashboardProjection;
import org.springframework.data.jpa.repository.JpaRepository;
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
}