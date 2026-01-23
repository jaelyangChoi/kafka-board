package kafka.board.comment.service;

import static java.util.function.Predicate.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kafka.board.comment.entity.Comment;
import kafka.board.comment.repository.CommentRepository;
import kafka.board.comment.service.request.CommentCreateRequest;
import kafka.board.comment.service.response.CommentPageResponse;
import kafka.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final Snowflake snowflake = new Snowflake();
	private final CommentRepository commentRepository;

	@Transactional
	public CommentResponse create(CommentCreateRequest request) {
		Comment parent = findParent(request);
		Comment comment = commentRepository.save(
			Comment.create(
				snowflake.nextId(),
				request.getContent(),
				parent != null ? parent.getCommentId() : null,
				request.getArticleId(),
				request.getWriterId()
			)
		);
		return CommentResponse.from(comment);
	}

	private Comment findParent(CommentCreateRequest request) {
		Long parentCommentId = request.getParentCommentId();
		if (parentCommentId == null) {
			return null;
		}
		return commentRepository.findById(parentCommentId)
			.filter(not(Comment::getDeleted))
			.filter(Comment::isRoot)
			.orElseThrow(() -> new IllegalArgumentException("Invalid parent comment ID: " + parentCommentId));
	}

	public CommentResponse read(Long commentId) {
		return CommentResponse.from(
			commentRepository.findById(commentId).orElseThrow()
		);
	}

	@Transactional
	public void delete(Long commentId) {
		commentRepository.findById(commentId)
			.filter(not(Comment::getDeleted))
			.ifPresent(comment -> {
				if ((hasChildren(comment))) {
					comment.delete(); //삭제 '상태'로만
				} else {
					delete(comment); //물리적 삭제
				}
			});
	}

	private boolean hasChildren(Comment comment) {
		return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;
	}

	//물리적 삭제
	private void delete(Comment comment) {
		commentRepository.delete(comment);
		//상위 댓글도 삭제 상태('삭제되었습니다')라면 재귀적으로 실제 삭제 처리
		if (!comment.isRoot()) {
			commentRepository.findById(comment.getParentCommentId())
				.filter(Comment::getDeleted)
				.filter(not(this::hasChildren))
				.ifPresent(this::delete);
		}
	}

	/**
	 * 댓글 목록 조회 (페이지 번호 방식)
	 */
	public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
		return CommentPageResponse.of(
			commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
				.map(CommentResponse::from)
				.toList(),
			commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
		);
	}

	/**
	 * 댓글 목록 조회 (무한 스크롤 방식)
	 */
	public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
		List<Comment> comments = lastCommentId == null || lastParentCommentId == null ?
			commentRepository.findAllInfiniteScroll(articleId, limit) :
			commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);
		return comments.stream()
			.map(CommentResponse::from)
			.toList();
	}
}
