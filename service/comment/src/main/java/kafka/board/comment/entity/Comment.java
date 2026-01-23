package kafka.board.comment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "comment")
@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
public class Comment {
	@Id
	private Long commentId;
	private String content;
	private Long parentCommentId;
	private Long articleId;
	private Long writerId;
	private Boolean deleted;
	private LocalDateTime createdAt;

	public static Comment create(Long commentId, String content, Long parentCommentId, Long articleId, Long writerId) {
		Comment comment = new Comment();
		comment.commentId = commentId;
		comment.content = content;
		comment.parentCommentId = parentCommentId == null ? commentId : parentCommentId;
		comment.articleId = articleId;
		comment.writerId = writerId;
		comment.deleted = false;
		comment.createdAt = LocalDateTime.now();
		return comment;
	}

	public boolean isRoot() {
		return this.parentCommentId.longValue() == this.commentId;
	}

	public void delete() {
		this.deleted = true;
	}
}
