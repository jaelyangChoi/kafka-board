package kafka.board.comment.api;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import kafka.board.comment.service.response.CommentPageResponse;
import kafka.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class CommentApiTest {
	RestClient restClient = RestClient.create("http://localhost:9001");

	@Test
	void create() {
		CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment1", null, 1L));
		CommentResponse response2 = createComment(
			new CommentCreateRequest(1L, "my comment2", response1.getCommentId(), 1L));
		CommentResponse response3 = createComment(
			new CommentCreateRequest(1L, "my comment3", response1.getCommentId(), 1L));

		System.out.println("commentId=%s".formatted(response1.getCommentId()));
		System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
		System.out.println("\tcommentId=%s".formatted(response3.getCommentId()));

		/*
		  commentId=272992649486958592
		  	commentId=272992650388733952
		  	commentId=272992650451648512
		 */
	}

	CommentResponse createComment(CommentCreateRequest request) {
		return restClient.post()
			.uri("/v1/comments")
			.body(request)
			.retrieve()
			.body(CommentResponse.class);
	}

	@Test
	void read() {
		CommentResponse response = restClient.get()
			.uri("/v1/comments/{commentId}", 272992649486958592L)
			.retrieve()
			.body(CommentResponse.class);

		System.out.println("response = " + response);
	}

	@Test
	void delete() {
		/*
		  commentId=272992649486958592
		  	commentId=272992650388733952
		  	commentId=272992650451648512
		 */
		// restClient.delete()
		// 	.uri("/v1/comments/{commentId}", 272992649486958592L)
		// 	.retrieve();

		// restClient.delete()
		// 	.uri("/v1/comments/{commentId}", 272992650388733952L)
		// 	.retrieve();

		restClient.delete()
			.uri("/v1/comments/{commentId}", 272992650451648512L)
			.retrieve();
	}

	@Test
	void readAll() {
		CommentPageResponse response = restClient.get()
			.uri("/v1/comments?articleId={articleId}&page={page}&pageSize={pageSize}", 1L, 1L, 10L)
			.retrieve()
			.body(CommentPageResponse.class);

		System.out.println("response.getCommentCount() = " + response.getCommentCount());
		for (CommentResponse comment : response.getComments()) {
			if (!comment.getCommentId().equals(comment.getParentCommentId())) {
				System.out.print("\t");
			}
			System.out.println("comment.getCommentId() = " + comment.getCommentId());
		}
	}

	/**
	 * 1번 페이지 수행 결과
	 * comment.getCommentId() = 272996024433446912
	 * 	comment.getCommentId() = 272996024559276036
	 * comment.getCommentId() = 272996024437641216
	 * 	comment.getCommentId() = 272996024559276034
	 * comment.getCommentId() = 272996024437641217
	 * 	comment.getCommentId() = 272996024559276033
	 * comment.getCommentId() = 272996024437641218
	 * 	comment.getCommentId() = 272996024567664662
	 * comment.getCommentId() = 272996024437641219
	 * 	comment.getCommentId() = 272996024559276032
	 */
	@Test
	void readAllInfiniteScroll() {
		List<CommentResponse> response1 = restClient.get()
			.uri("/v1/comments/infinite-scroll?articleId={articleId}&pageSize={pageSize}", 1L, 5L)
			.retrieve()
			.body(new ParameterizedTypeReference<List<CommentResponse>>() {
			});

		System.out.println("first Page");
		for (CommentResponse response : response1) {
			if (!response.getCommentId().equals(response.getParentCommentId())) {
				System.out.print("\t");
			}
			System.out.println("comment.getCommentId() = " + response.getCommentId());
		}

		Long lastParentCommentId = response1.getLast().getParentCommentId();
		Long lastCommentId = response1.getLast().getCommentId();

		List<CommentResponse> response2 = restClient.get()
			.uri(
				"/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
					.formatted(lastParentCommentId, lastCommentId))
			.retrieve()
			.body(new ParameterizedTypeReference<List<CommentResponse>>() {
			});

		System.out.println("second Page");
		for (CommentResponse response : response2) {
			if (!response.getCommentId().equals(response.getParentCommentId())) {
				System.out.print("\t");
			}
			System.out.println("comment.getCommentId() = " + response.getCommentId());
		}
	}

	@Getter
	@AllArgsConstructor
	public class CommentCreateRequest {
		private Long articleId;
		private String content;
		private Long parentCommentId;
		private Long writerId;
	}
}
