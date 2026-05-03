package com.sdlctower.platform.support;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportRequestRepository extends JpaRepository<SupportRequestEntity, String> {

    long countByRequestDate(LocalDate requestDate);
}
