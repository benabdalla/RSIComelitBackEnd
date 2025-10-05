package com.rsi.comelit.repository;


import com.rsi.comelit.entity.Absence;
import com.rsi.comelit.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    // Check if absence exists for user and date
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
    // Find absence by user and date
    Optional<Absence> findByUserIdAndDate(Long userId, LocalDate date);
    // Find absences by date
    List<Absence> findByDate(LocalDate date);
    // Find absences by date with pagination
    Page<Absence> findByDate(LocalDate date, Pageable pageable);
    // Find absences by status
    Page<Absence> findByStatus(Absence.AbsenceStatus status, Pageable pageable);

    // Find absences by status
    Page<Absence> findByStatusAndUser(Absence.AbsenceStatus status, User user, Pageable pageable);
    // Find absences by user ID
    Page<Absence> findByUserId(Long userId, Pageable pageable);
    // Custom query for searching with user details
    @Query("SELECT a FROM Absence a JOIN a.user u WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:date IS NULL OR a.date = :date)")
    Page<Absence> findAbsencesWithFilters(
            @Param("search") String search,
            @Param("status") Absence.AbsenceStatus status,
            @Param("date") LocalDate date,
            Pageable pageable
    );

    // Count absences by status
    long countByStatus(Absence.AbsenceStatus status);
    // Count today's absences
    long countByDate(LocalDate date);
    // Find absences by date range
    @Query("SELECT a FROM Absence a WHERE a.date BETWEEN :startDate AND :endDate")
    List<Absence> findByDateRange(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);



    // Add this to your AbsenceRepository
    @Query("SELECT COUNT(a) > 0 FROM Absence a WHERE a.user.id = :userId AND a.date = :date")
    boolean existsByUserIdAndDateCustom(@Param("userId") Long userId, @Param("date") LocalDate date);
}
