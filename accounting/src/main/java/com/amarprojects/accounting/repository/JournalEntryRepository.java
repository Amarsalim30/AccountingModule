package com.amarprojects.accounting.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.amarprojects.accounting.model.JournalEntry;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    Optional<JournalEntry> findByDescriptionContaining(String invoiceNumber);
    List<JournalEntry> findByDescriptionStartsWith(String prefix);
}
