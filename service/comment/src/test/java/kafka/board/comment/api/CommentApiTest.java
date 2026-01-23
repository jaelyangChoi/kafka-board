package kafka.board.comment.api;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

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

	@Getter
	@AllArgsConstructor
	public class CommentCreateRequest {
		private Long articleId;
		private String content;
		private Long parentCommentId;
		private Long writerId;
	}
}
