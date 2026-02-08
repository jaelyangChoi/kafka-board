package kafka.board.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kafka.board.article.entity.BoardArticleCount;

@Repository
public interface BoardArticleCountRepository extends JpaRepository<BoardArticleCount, Long> {
	/**
	 * 비관적 락 방법 1 (update)
	 * DB에서 원자적으로 +1 하는 방식이 더 단순/빠르고 충돌에 강하다.
	 */
	@Query(
		value = "update board_article_count set article_count = article_count + 1 where board_id = :boardId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("boardId") Long boardId);

	@Query(
		value = "update board_article_count set article_count = article_count - 1 where board_id = :boardId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("boardId") Long boardId);
}
