package com.allobanksplitbill.repository;

import com.allobanksplitbill.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findAllByGroupId(Long groupId);
}