package com.rsi.comelit.controller;

import com.rsi.comelit.dto.AbsencePageResponseDto;
import com.rsi.comelit.dto.AbsenceRequestDto;
import com.rsi.comelit.dto.AbsenceResponseDto;
import com.rsi.comelit.dto.JustificationUpdateDto;
import com.rsi.comelit.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/absences") // This is the base path
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class AbsenceController {

    private final AbsenceService absenceService;

    /**
     * Create a new absence - This handles POST /api/v1/absences
     */
    @PostMapping
    public ResponseEntity<?> createAbsence(@Valid @RequestBody AbsenceRequestDto request) {
        log.info("REST request to create absence for user: {}", request.getUserId());
        try {
            AbsenceResponseDto result = absenceService.createAbsence(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);


        } catch (Exception e) {
//         log.error("Error in controller: ", e);
//         log.warn("Duplicate absence: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    /**
     * Create absence for today
     */
    @PostMapping("/today/{userId}")
    public ResponseEntity<AbsenceResponseDto> createTodayAbsence(
            @PathVariable Long userId) {
        log.info("REST request to create today's absence for user: {}", userId);
        AbsenceResponseDto result = absenceService.createTodayAbsence(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Get all absences with pagination and filters
     */
    @GetMapping
    public ResponseEntity<AbsencePageResponseDto> getAbsences(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("REST request to get absences - page: {}, limit: {}, search: {}, status: {}, date: {}",
                page, limit, search, status, date);

        AbsencePageResponseDto result = absenceService.getAbsences(page, limit, search, status, date);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all absences with pagination and filters
     */
    @GetMapping("needing-justification")
    public ResponseEntity<AbsencePageResponseDto> getMyAbsences(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("REST request to get absences - page: {}, limit: {}",
                page, limit);
        AbsencePageResponseDto result = absenceService.getMyAbsences(page, limit);
        return ResponseEntity.ok(result);
    }

    /**
     * Get today's absences
     */
    @GetMapping("/today")
    public ResponseEntity<List<AbsenceResponseDto>> getTodayAbsences() {
        log.info("REST request to get today's absences");
        List<AbsenceResponseDto> result = absenceService.getTodayAbsences();
        return ResponseEntity.ok(result);
    }

    /**
     * Get absence by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AbsenceResponseDto> getAbsenceById(
            @PathVariable Long id) {
        log.info("REST request to get absence: {}", id);
        AbsenceResponseDto result = absenceService.getAbsenceById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Update absence justification
     */
    @PatchMapping("/{id}/justify")
    public ResponseEntity<AbsenceResponseDto> updateJustification(
            @PathVariable Long id,
            @Valid @RequestBody JustificationUpdateDto request) {
        log.info("REST request to update justification for absence: {}", id);
        AbsenceResponseDto result = absenceService.updateJustification(id, request);
        return ResponseEntity.ok(result);
    }

    /**
     * Validate absence
     */
    @PatchMapping("/{id}/validate")

    public ResponseEntity<AbsenceResponseDto> validateAbsence(
            @PathVariable Long id) {
        log.info("REST request to validate absence: {}", id);
        AbsenceResponseDto result = absenceService.validateAbsence(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete absence
     */
    @DeleteMapping("/{id}")

    public ResponseEntity<Void> deleteAbsence(
            @PathVariable Long id) {
        log.info("REST request to delete absence: {}", id);
        absenceService.deleteAbsence(id);
        return ResponseEntity.noContent().build();
    }
}