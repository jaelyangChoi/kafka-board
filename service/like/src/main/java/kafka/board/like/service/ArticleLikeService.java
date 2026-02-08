package kafka.board.like.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kafka.board.like.entity.ArticleLike;
import kafka.board.like.entity.ArticleLikeCount;
import kafka.board.like.repository.ArticleLikeCountRepository;
import kafka.board.like.repository.ArticleLikeRepository;
import kafka.board.like.service.response.ArticleLikeResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
	private final Snowflake snowflake = new Snowflake();
	private final ArticleLikeRepository articleLikeRepository;
	private final ArticleLikeCountRepository articleLikeCountRepository;

	public ArticleLikeResponse read(Long articleId, Long userId) {
		return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
			.map(ArticleLikeResponse::from)
			.orElseThrow();
	}

	/**
	 * update (자동으로 비관적 쓰기 락 걸림)
	 */
	@Transactional
	public void likePessimisticLock1(Long articleId, Long userId) {
		articleLikeRepository.save(
			ArticleLike.create(
				snowflake.nextId(),
				articleId,
				userId
			)
		);

		int result = articleLikeCountRepository.increase(articleId);
		// 최초 요청 시에는 update되는 레코드가 없으므로, 1로 초기화한다.
		// 트래픽이 순식간에 몰릴 수 있는 상황에서는 유실될 수 있으므로, 게시글 생성 시점에 미리 0으로 초기화해둘 수도 있다.
		if (result == 0) {
			articleLikeCountRepository.save(
				ArticleLikeCount.init(articleId, 1L)
			);
		}
	}

	@Transactional
	public void unlikePessimisticLock1(Long articleId, Long userId) {
		articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
			.ifPresent(articleLike -> {
				articleLikeRepository.delete(articleLike);
				articleLikeCountRepository.decrease(articleId);
			});
	}

	/**
	 * select ... for update + update(dirty check)
	 */
	@Transactional
	public void likePessimisticLock2(Long articleId, Long userId) {
		articleLikeRepository.save(
			ArticleLike.create(
				snowflake.nextId(),
				articleId,
				userId
			)
		);
		ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
			.orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
		articleLikeCount.increase();
		articleLikeCountRepository.save(articleLikeCount); //최초 저장 케이스 대비
	}

	@Transactional
	public void unlikePessimisticLock2(Long articleId, Long userId) {
		articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
			.ifPresent(articleLike -> {
				articleLikeRepository.delete(articleLike);
				ArticleLikeCount articleLikeCount = articleLikeCountRepository.findLockedByArticleId(articleId)
					.orElseThrow();
				articleLikeCount.decrease();
			});
	}

	/**
	 * version + 충돌 시 후 처리(JPA가 자동 롤백)
	 */
	@Transactional
	public void likeOptimisticLock(Long articleId, Long userId) {
		articleLikeRepository.save(
			ArticleLike.create(
				snowflake.nextId(),
				articleId,
				userId
			)
		);

		ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId)
			.orElseGet(() -> ArticleLikeCount.init(articleId, 0L));
		articleLikeCount.increase();
		articleLikeCountRepository.save(articleLikeCount);
	}

	@Transactional
	public void unlikeOptimisticLock(Long articleId, Long userId) {
		articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
			.ifPresent(articleLike -> {
				articleLikeRepository.delete(articleLike);
				ArticleLikeCount articleLikeCount = articleLikeCountRepository.findById(articleId).orElseThrow();
				articleLikeCount.decrease();
			});
	}

	public Long count(Long articleId){
		return articleLikeCountRepository.findById(articleId)
			.map(ArticleLikeCount::getLikeCount)
			.orElse(0L);
	}
}
