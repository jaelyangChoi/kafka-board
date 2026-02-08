package kafka.board.like.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kafka.board.like.entity.ArticleLikeCount;

public interface ArticleLikeCountRepository extends JpaRepository<ArticleLikeCount, Long> {

	/**
	 * 비관적 락 방법 1 (update)
	 * DB에서 원자적으로 +1 하는 방식이 더 단순/빠르고 충돌에 강하다.
	 */
	@Query(
		value = "update article_like_count set like_count = like_count + 1 where article_id = :articleId",
		nativeQuery = true
	)
	@Modifying
	int increase(@Param("articleId") Long articleId);

	@Query(
		value = "update article_like_count set like_count = like_count - 1 where article_id = :articleId",
		nativeQuery = true
	)
	@Modifying
	int decrease(@Param("articleId") Long articleId);


	/**
	 * 비관적 락 방법 2 (select ... for update)
	 * 조회된 객체을 증감하고 dirty check
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE) //select ... for update
	Optional<ArticleLikeCount> findLockedByArticleId(Long articleId);
}
