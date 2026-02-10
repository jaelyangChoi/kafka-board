package kafka.board.view.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ArticleViewCountRepository {
	private final StringRedisTemplate redisTemplate; //기본적으로 만들어줌. 이걸 통해 redis와 통신

	// view::article::{article_id}::view_count
	private static final String KEY_FORMAT = "view::article::%s::view_count";

	public Long read(Long articleId) {
		String result = redisTemplate.opsForValue().get(generateKey(articleId));
		return result == null ? 0L : Long.valueOf(result);
	}

	public Long increase(Long articleId) {
		return redisTemplate.opsForValue().increment(generateKey(articleId));
	}

	private String generateKey(Long articleId) {
		return KEY_FORMAT.formatted(articleId);
	}
}
