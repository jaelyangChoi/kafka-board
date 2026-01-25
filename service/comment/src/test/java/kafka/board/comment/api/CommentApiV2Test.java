package kafka.board.comment.api;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import kafka.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class CommentApiV2Test {
	RestClient restClient = RestClient.create("http://localhost:9001");

	@Test
	void create() {
		CommentResponse response1 = create(new CommentCreateRequestV2(1L, "댓글 내용1", null, 1L));
		CommentResponse response2 = create(new CommentCreateRequestV2(1L, "댓글 내용2", response1.getPath(), 1L));
		CommentResponse response3 = create(new CommentCreateRequestV2(1L, "댓글 내용3", response2.getPath(), 1L));

		System.out.println("response1.getCommentId() = " + response1.getCommentId());
		System.out.println("response1.getPath() = " + response1.getPath());
		System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
		System.out.println("\tresponse2.getPath() = " + response2.getPath());
		System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());
		System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
	}

	/**
	 response1.getCommentId() = 273742689025339392
	 response1.getPath() = 00003
	 	response2.getCommentId() = 273742689948086272
	 	response2.getPath() = 0000300000
	 		response3.getCommentId() = 273742690115858432
	 		response3.getPath() = 000030000000000
	 */

	CommentResponse create(CommentCreateRequestV2 request) {
		return restClient.post()
			.uri("/v2/comments")
			.body(request)
			.retrieve()
			.body(CommentResponse.class);
	}

	@Test
	void read(){
		CommentResponse response = restClient.get()
			.uri("/v2/comments/{commentId}", 273742689025339392L)
			.retrieve()
			.body(CommentResponse.class);

		System.out.println("response = " + response);
	}

	@Test
	void delete(){
		restClient.delete()
			.uri("/v2/comments/{commentId}", 273741956708249600L)
			.retrieve();
	}

	@Getter
	@AllArgsConstructor
	public static class CommentCreateRequestV2 {
		private Long articleId;
		private String content;
		private String parentPath;
		private Long writerId;
	}

}
