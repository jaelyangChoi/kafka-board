package kafka.board.comment.service;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE) //유틸성 클래스이므로
public final class PageLimitCalculator {

	public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
		return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
	}
}
