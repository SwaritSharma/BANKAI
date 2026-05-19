package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "addresses")
public interface AddressRepository extends JpaRepository<Address, Integer> {
}