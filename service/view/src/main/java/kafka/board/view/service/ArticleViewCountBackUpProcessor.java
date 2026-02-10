package kafka.board.view.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kafka.board.view.entity.ArticleViewCount;
import kafka.board.view.repository.ArticleViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
	private final ArticleViewCountBackUpRepository articleViewCountBackUpRepository;

	@Transactional
	public void backUp(Long articleId, Long viewCount) {
		int result = articleViewCountBackUpRepository.updateViewCount(articleId, viewCount);
		//0이면 백업 데이터가 없거나, 작은 수로 업데이트 시도한 경우
		if (result == 0) {
			articleViewCountBackUpRepository.findById(articleId)
				.ifPresentOrElse(ignore -> {
					},
					() -> articleViewCountBackUpRepository.save(ArticleViewCount.init(articleId, viewCount))
				);
		}
	}
}
