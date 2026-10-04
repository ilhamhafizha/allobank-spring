package com.allobanksplitbill.repository;

import com.allobanksplitbill.entity.BillGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillGroupRepository extends JpaRepository<BillGroup, Long> {
}