package com.rsi.comelit.repository;

import com.rsi.comelit.entity.CongeRequest;
import com.rsi.comelit.entity.CongeRequestStatus;
import com.rsi.comelit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CongeRequestRepository extends JpaRepository<CongeRequest, Long> {
    List<CongeRequest> findByRequester(User requester);

    List<CongeRequest> findByValidatorAndStatus(User validator, CongeRequestStatus status);
}

