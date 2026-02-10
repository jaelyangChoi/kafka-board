package kafka.board.view.service;

import org.springframework.stereotype.Service;

import kafka.board.view.repository.ArticleViewCountRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
	private final ArticleViewCountRepository articleViewCountRepository;
	private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor;
	private static final int BACK_UP_BATCH_SIZE = 100;

	public Long increase(Long articleId) {
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
