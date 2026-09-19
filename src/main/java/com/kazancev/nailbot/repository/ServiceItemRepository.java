package com.kazancev.nailbot.repository;

import com.kazancev.nailbot.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

    List<ServiceItem> findAllByOrderByIdAsc();
}
