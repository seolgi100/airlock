package com.airlock.backend.domain.repository;

import com.airlock.backend.domain.entity.PasskeyCredential;
import com.airlock.backend.domain.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface PasskeyCredentialRepository extends CrudRepository<PasskeyCredential, Long> {
    
    Optional<PasskeyCredential> findByCredentialId(String credentialId);
    boolean existsByCredentialId(String credentialId);
    List<PasskeyCredential> findByUser(User user);
}
