package kafka.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "article_comment_count")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ArticleCommentCount {
	@Id
	private Long articleId; // shard key를 Comment 테이블과 맞춰 단일 트랜잭션으로
	private Long commentCount;

	public static ArticleCommentCount init(Long articleId, Long commentCount) {
		ArticleCommentCount articleCommentCount = new ArticleCommentCount();
		articleCommentCount.articleId = articleId;
		articleCommentCount.commentCount = commentCount;
		return articleCommentCount;
	}
}
