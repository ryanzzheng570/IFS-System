package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.DBLogEntry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface DBLogEntryRepository extends JpaRepository<DBLogEntry, Long> {
    List<DBLogEntry> findDistinctByTablesColumnsAccessedIn(List<String> tablesAndColumnsAccessed);
}
