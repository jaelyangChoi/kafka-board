package kafka.board.article.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "board_article_count")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class BoardArticleCount {
	@Id
	private Long boardId; // shard key를 Article table과 맞춰 단일 트랜잭션으로 처리
	private Long articleCount;

	public static BoardArticleCount init(Long boardId, Long articleCount) {
		BoardArticleCount boardArticleCount = new BoardArticleCount();
		boardArticleCount.boardId = boardId;
		boardArticleCount.articleCount = articleCount;
		return boardArticleCount;
	}
}
