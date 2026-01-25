package kafka.board.comment.service;

import static java.util.function.Predicate.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kafka.board.comment.entity.CommentPath;
import kafka.board.comment.entity.CommentV2;
import kafka.board.comment.repository.CommentRepositoryV2;
import kafka.board.comment.service.request.CommentCreateRequestV2;
import kafka.board.comment.service.response.CommentPageResponse;
import kafka.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
	private final Snowflake snowflake = new Snowflake();
	private final CommentRepositoryV2 commentRepository;

	@Transactional
	public CommentResponse create(CommentCreateRequestV2 request) {
		CommentV2 parent = findParent(request);
		CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
		CommentV2 comment = commentRepository.save(
			CommentV2.create(
				snowflake.nextId(),
				request.getContent(),
				request.getArticleId(),
				request.getWriterId(),
				parentCommentPath.createChildCommentPath(
					commentRepository.findDescendantTopPath(request.getArticleId(), parentCommentPath.getPath())
						.orElse(null))
			)
		);

		return CommentResponse.from(comment);
	}

	private CommentV2 findParent(CommentCreateRequestV2 request) {
		String parentPath = request.getParentPath();
		if (parentPath == null) {
			return null;
		}
		return commentRepository.findByArticleIdAndPath(request.getArticleId(), parentPath)
			.filter(not(CommentV2::getDeleted))
			.orElseThrow(() -> new IllegalArgumentException("Invalid parent comment path: " + parentPath));
	}

	public CommentResponse read(Long commentId) {
		return CommentResponse.from(
			commentRepository.findById(commentId).orElseThrow()
		);
	}

	@Transactional
	public void delete(Long commentId) {
		commentRepository.findById(commentId)
			.filter(not(CommentV2::getDeleted))
			.ifPresent(comment -> {
				if (hasChilren(comment)) {
					comment.delete(); //삭제 '상태'로만
				} else {
					delete(comment);
				}
			});
	}

	private void delete(CommentV2 comment) {
		commentRepository.delete(comment);
		if (!comment.isRoot()) {
			commentRepository.findByArticleIdAndPath(comment.getArticleId(), comment.getCommentPath().getParentPath())
				.filter(CommentV2::getDeleted)
				.filter(not(this::hasChilren))
				.ifPresent(this::delete);
		}
	}

	private boolean hasChilren(CommentV2 comment) {
		return commentRepository.findDescendantTopPath(
			comment.getArticleId(),
			comment.getCommentPath().getPath()
		).isPresent();
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
	 * 댓글 목록 조회(무한 스크롤 방식)
	 */
	public List<CommentResponse> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
		List<CommentV2> comments = lastPath == null ?
			commentRepository.findAllInfiniteScroll(articleId, pageSize) :
			commentRepository.findAllInfiniteScroll(articleId, lastPath, pageSize);
		return comments.stream()
			.map(CommentResponse::from)
			.toList();
	}
}
