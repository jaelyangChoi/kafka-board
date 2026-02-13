package kafka.board.view.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import kafka.board.view.repository.ArticleViewCountRepository;
import kafka.board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
	private final ArticleViewCountRepository articleViewCountRepository;
	private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository;
	private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;
	private static final int BACK_UP_BATCH_SIZE = 100;
	private static final Duration TTL = Duration.ofMinutes(10);

	public Long increase(Long articleId, Long userId) {
		if (!articleViewDistributedLockRepository.lock(articleId, userId, TTL)) {
			return articleViewCountRepository.read(articleId);
		}
		Long count = articleViewCountRepository.increase(articleId);
		if (count % BACK_UP_BATCH_SIZE == 0) {
			articleViewCountBackUpProcessor.backUp(articleId, count);
		}
		return count;
	}

	public Long count(Long articleId) {
		return articleViewCountRepository.read(articleId);
	}
}
