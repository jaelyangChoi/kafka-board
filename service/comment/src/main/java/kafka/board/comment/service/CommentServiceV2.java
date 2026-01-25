package kafka.board.comment.service;

import static java.util.function.Predicate.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kafka.board.comment.entity.CommentPath;
import kafka.board.comment.entity.CommentV2;
import kafka.board.comment.repository.CommentRepositoryV2;
import kafka.board.comment.service.request.CommentCreateRequestV2;
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
}
