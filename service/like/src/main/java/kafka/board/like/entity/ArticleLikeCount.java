package kafka.board.like.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "article_like_count")
@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Entity
public class ArticleLikeCount {
	@Id
	private Long articleId; //shard key
	private Long likeCount;
	@Version //JPA가 UPDATE 시점에 version 값을 자동으로 비교/증가시켜 낙관적 락(Optimistic Lock) 을 구현. 충돌 시 롤백.
	private Long version;

	public static ArticleLikeCount init(Long articleId, Long likeCount) {
		ArticleLikeCount articleLikeCount = new ArticleLikeCount();
		articleLikeCount.articleId = articleId;
		articleLikeCount.likeCount = likeCount;
		// articleLikeCount.version = 0L; JPA가 INSERT 시 자동으로 세팅
		return articleLikeCount;
	}

	/**
	 * 비관적 락 방법 2 - select ... for update
	 */
	public void increase() {
		this.likeCount++;
	}

	public void decrease() {
		this.likeCount--;
	}
}
