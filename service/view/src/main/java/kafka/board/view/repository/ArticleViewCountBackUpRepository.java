package kafka.board.view.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kafka.board.view.entity.ArticleViewCount;

@Repository
public interface ArticleViewCountBackUpRepository extends JpaRepository<ArticleViewCount, Long> {

	@Query(
		value = "update article_view_count set view_count = :viewCount " +
			"where article_id = :articleId and view_count < :viewCount", //동시 요청이 많아서 업데이터 쿼리 꼬이는 경우 방어
		nativeQuery = true
	)
	@Modifying
	int updateViewCount(@Param("articleId") Long articleId, @Param("viewCount") Long viewCount);
}
