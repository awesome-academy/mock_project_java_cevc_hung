package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CommentCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiCommentResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CommentEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CommentRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.ReviewRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CommentService commentService;

    private UserEntity testUser;
    private ReviewEntity testReview;
    private CommentEntity testComment;
    private CommentCreateRequest commentRequest;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .name("Test User")
                .build();
        testUser.setId(1L);

        testReview = ReviewEntity.builder()
                .rating(5)
                .content("Great!")
                .build();
        testReview.setId(1L);

        testComment = CommentEntity.builder()
                .review(testReview)
                .user(testUser)
                .content("I agree!")
                .build();
        testComment.setId(1L);

        commentRequest = CommentCreateRequest.builder()
                .reviewId(1L)
                .content("I agree!")
                .build();

        lenient().when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Message");
    }

    @Test
    @DisplayName("Should create comment successfully")
    void createComment_ValidRequest_ShouldCreateComment() {
        // Given
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.of(testReview));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(testComment);

        // When
        ApiCommentResponse result = commentService.createComment(commentRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("I agree!");
        assertThat(result.getUserName()).isEqualTo("Test User");
        verify(commentRepository, times(1)).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when review not found")
    void createComment_NonExistingReview_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.createComment(commentRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get comments by review")
    void getCommentsByReview_ValidReviewId_ShouldReturnComments() {
        // Given
        Page<CommentEntity> page = new PageImpl<>(List.of(testComment));
        Pageable pageable = PageRequest.of(0, 10);
        when(commentRepository.findByReviewId(anyLong(), any())).thenReturn(page);

        // When
        Page<ApiCommentResponse> result = commentService.getCommentsByReview(1L, pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("I agree!");
    }

    @Test
    @DisplayName("Should get comment by id")
    void getCommentById_ExistingComment_ShouldReturnComment() {
        // Given
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));

        // When
        ApiCommentResponse result = commentService.getCommentById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("I agree!");
    }

    @Test
    @DisplayName("Should delete comment successfully")
    void deleteComment_ValidComment_ShouldDeleteComment() {
        // Given
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));
        doNothing().when(commentRepository).delete((CommentEntity) any());

        // When
        commentService.deleteComment(1L, 1L, true); // Admin can delete

        // Then
        verify(commentRepository, times(1)).delete((CommentEntity) any());
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to delete comment")
    void deleteComment_NonOwner_ShouldThrowException() {
        // Given
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(testComment));

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(1L, 999L, false)) // Different user, not admin
                .isInstanceOf(BusinessException.class);

        verify(commentRepository, never()).delete((CommentEntity) any());
    }
}
