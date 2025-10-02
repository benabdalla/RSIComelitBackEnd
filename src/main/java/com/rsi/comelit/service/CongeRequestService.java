package com.rsi.comelit.service;

import com.rsi.comelit.entity.CongeRequest;
import com.rsi.comelit.entity.CongeRequestComment;
import com.rsi.comelit.entity.CongeRequestStatus;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.repository.CongeRequestCommentRepository;
import com.rsi.comelit.repository.CongeRequestRepository;
import com.rsi.comelit.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CongeRequestService {
    private final CongeRequestRepository congeRequestRepository;
    private final CongeRequestCommentRepository commentRepository;
    private final UserRepository userRepository;

    public CongeRequestService(CongeRequestRepository congeRequestRepository, CongeRequestCommentRepository commentRepository,UserRepository userRepository) {
        this.congeRequestRepository = congeRequestRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public CongeRequest createDraft(CongeRequest request) {
        request.setStatus(CongeRequestStatus.DRAFT);
        return congeRequestRepository.save(request);
    }
    public User adminValidator() {
        return userRepository.findAdmin();
    }

    private static void validateRequest(boolean isOk, String message) {
        if (isOk) {
            throw new IllegalStateException(message);
        }
    }

    public CongeRequest updateDraft(Long id, CongeRequest updatedRequest) {
        CongeRequest request = congeRequestRepository.findById(id).orElseThrow();
        validateRequest(!CongeRequestStatus.DRAFT.equals(request.getStatus()) || !request.getRequester().getId().equals(updatedRequest.getRequester().getId()), "Modification non autorisée");
        request.setStartDate(updatedRequest.getStartDate());
        request.setEndDate(updatedRequest.getEndDate());
        request.setReason(updatedRequest.getReason());
        request.setValidator(updatedRequest.getValidator());
        return congeRequestRepository.save(request);
    }

    public CongeRequest sendRequest(Long id) {
        CongeRequest request = congeRequestRepository.findById(id).orElseThrow();
        validateRequest(!CongeRequestStatus.DRAFT.equals(request.getStatus()), "Envoi non autorisé");
        request.setStatus(CongeRequestStatus.SENT);
        congeRequestRepository.save(request);
        return request;
    }

    public CongeRequest validateRequest(Long id, User validator, String comment) {
        CongeRequest request = congeRequestRepository.findById(id).orElseThrow();
        validateRequest(!CongeRequestStatus.SENT.equals(request.getStatus()) || !request.getValidator().getId().equals(validator.getId()), "Validation non autorisée");
        request.setStatus(CongeRequestStatus.ACCEPTED);
        congeRequestRepository.save(request);
        addComment(request, validator, comment);
        return request;
    }

    public CongeRequest rejectRequest(Long id, User validator, String comment) {
        CongeRequest request = congeRequestRepository.findById(id).orElseThrow();
        validateRequest(!CongeRequestStatus.SENT.equals(request.getStatus()) || !request.getValidator().getId().equals(validator.getId()), "Rejet non autorisé");
        request.setStatus(CongeRequestStatus.REJECTED);
        congeRequestRepository.save(request);
        addComment(request, validator, comment);
        return request;
    }

    public List<CongeRequest> getMyRequests(User requester) {
        return congeRequestRepository.findByRequester(requester);
    }

    public List<CongeRequest> getRequestsToValidate(User validator) {
        return congeRequestRepository.findByValidatorAndStatus(validator, CongeRequestStatus.SENT);
    }

    private void addComment(CongeRequest request, User author, String commentText) {
        if (commentText != null && !commentText.isEmpty()) {
            CongeRequestComment comment = new CongeRequestComment();
            comment.setCongeRequest(request);
            comment.setAuthor(author);
            comment.setComment(commentText);
            comment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(comment);
        }
    }
}

