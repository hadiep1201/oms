package com.example.aims.repository;

import com.example.aims.entity.DVD;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DVDRepository extends JpaRepository<DVD, Integer> {
}
