package kafka.board.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kafka.board.comment.entity.ArticleCommentCount;

@Repository
public interface ArticleCommentCountRepository extends JpaRepository<ArticleCommentCount, Long> {
	/**
	 * 비관적 락 방법 1 (update)
	 * DB에서 원자적으로 +1 하는 방식이 더 단순/빠르고 충돌에 강하다.
	 */
	@Query(
		value = "update article_comment_count set comment_count = comment_count + 1 where article_id = :articleId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("articleId") Long articleId);

	@Query(
		value = "update article_comment_count set comment_count = comment_count - 1 where article_id = :articleId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("articleId") Long articleId);
}
