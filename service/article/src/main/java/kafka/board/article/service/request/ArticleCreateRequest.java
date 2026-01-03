package kafka.board.article.service.request;

import lombok.Getter;
import lombok.ToString;

//JSON 바인딩(@RequestBody): Jackson은 기본 생성자가 있으면 리플렉션으로 private 필드에 값을 채울 수 있어 보통 setter 없이도 동작
@Getter
@ToString
public class ArticleCreateRequest {
	private String title;
	private String content;
	private Long writerId;
	private Long boardId;
}
