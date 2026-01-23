package kafka.board.comment.service;

import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import kafka.board.comment.entity.Comment;
import kafka.board.comment.repository.CommentRepository;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
	@InjectMocks
	CommentService commentService;
	@Mock
	CommentRepository commentRepository;

	@Test
	@DisplayName("삭제할 댓글의 자식이 있으면, 삭제만 표시한다.")
	void deleteShouldMarkAsDeletedWhenHasChildren() {
		//given
		Long articleId = 1L;
		Long commentId = 1L;
		Comment comment = createComment(articleId, commentId);
		given(commentRepository.findById(commentId))
			.willReturn(Optional.of(comment));
		given(commentRepository.countBy(articleId, commentId, 2L))
			.willReturn(2L); //자식이 있다

		//when
		commentService.delete(commentId);

		//then
		verify(comment).delete();
	}

	@Test
	@DisplayName("하위 댓글이 삭제되고 삭제되지 않은 부모면, 하위댓글만 삭제한다.")
	void deleteShouldDeleteChildOnlyWhenNotDeletedParent() {
		//given
		Long articleId = 1L;
		Long commentId = 2L;
		Long parentCommentId = 1L;

		Comment comment = createComment(articleId, commentId, parentCommentId);
		given(comment.isRoot()).willReturn(false);

		Comment parentComment = mock(Comment.class);
		given(parentComment.getDeleted()).willReturn(false);

		given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
		given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L); //자식이 없다.
		given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));

		//when
		commentService.delete(commentId);

		//then
		verify(commentRepository).delete(comment);
		verify(commentRepository, never()).delete(parentComment);
	}

	@Test
	@DisplayName("하위 댓글이 삭제되고 삭제된 부모면, 재귀적으로 모두 삭제한다.")
	void deleteShouldDeleteAllWhenDeletedParent() {
		//given
		Long articleId = 1L;
		Long commentId = 2L;
		Long parentCommentId = 1L;

		Comment comment = createComment(articleId, commentId, parentCommentId);
		given(comment.isRoot()).willReturn(false);

		Comment parentComment = createComment(articleId, parentCommentId);
		given(parentComment.isRoot()).willReturn(true);
		given(parentComment.getDeleted()).willReturn(true);

		given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
		given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L); //자식이 없다.
		given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));
		given(commentRepository.countBy(articleId, parentCommentId, 2L)).willReturn(1L); //자식이 없다.

		//when
		commentService.delete(commentId);

		//then
		verify(commentRepository).delete(comment);
		verify(commentRepository).delete(parentComment);
	}

	private Comment createComment(Long articleId, Long commentId) {
		Comment comment = mock(Comment.class);
		given(comment.getArticleId()).willReturn(articleId);
		given(comment.getCommentId()).willReturn(commentId);
		return comment;
	}

	private Comment createComment(Long articleId, Long commentId, Long parentCommentId) {
		Comment comment = createComment(articleId, commentId);
		given(comment.getParentCommentId()).willReturn(parentCommentId);
		return comment;
	}

}
