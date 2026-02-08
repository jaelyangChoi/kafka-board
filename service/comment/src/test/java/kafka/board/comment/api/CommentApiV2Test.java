package kafka.board.comment.api;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import kafka.board.comment.service.response.CommentPageResponse;
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
	void read() {
		CommentResponse response = restClient.get()
			.uri("/v2/comments/{commentId}", 273742689025339392L)
			.retrieve()
			.body(CommentResponse.class);

		System.out.println("response = " + response);
	}

	@Test
	void delete() {
		restClient.delete()
			.uri("/v2/comments/{commentId}", 273741956708249600L)
			.retrieve();
	}

	@Test
	void readAll() {
		CommentPageResponse response = restClient.get()
			.uri("/v2/comments?articleId=1&pageSize=10&page=50000")
			.retrieve()
			.body(CommentPageResponse.class);

		System.out.println("response = " + response);
		for (CommentResponse comment : response.getComments()) {
			System.out.println("comment.getCommentId() = " + comment.getCommentId());
		}
	}
	/*
	first scroll
	response.getCommentId() = 273776580476026884
	response.getCommentId() = 273776580627021829
	response.getCommentId() = 273776580631216132
	response.getCommentId() = 273776580631216136
	response.getCommentId() = 273776580631216139
	second scroll
	response.getCommentId() = 273776580635410433
	response.getCommentId() = 273776580635410435
	response.getCommentId() = 273776580635410437
	response.getCommentId() = 273776580635410443
	response.getCommentId() = 273776580635410444
	 */

	@Test
	void readAllInfiniteScroll() {
		List<CommentResponse> responses1 = restClient.get()
			.uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
			.retrieve()
			.body(new ParameterizedTypeReference<List<CommentResponse>>() {
			});

		System.out.println("first scroll");
		for (CommentResponse response : responses1) {
			System.out.println("response.getCommentId() = " + response.getCommentId());
		}

		String lastPath = responses1.get(responses1.size() - 1).getPath();
		List<CommentResponse> responses2 = restClient.get()
			.uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
			.retrieve()
			.body(new ParameterizedTypeReference<List<CommentResponse>>() {
			});

		System.out.println("second scroll");
		for (CommentResponse response : responses2) {
			System.out.println("response.getCommentId() = " + response.getCommentId());
		}
	}

	@Test
	void countTest() {
		CommentResponse commentResponse = create(new CommentCreateRequestV2(2L, "my comment1", null, 1L));

		Long count1 = restClient.get()
			.uri("/v2/comments/articles/{articleId}/count", 2L)
			.retrieve()
			.body(Long.class);
		System.out.println("count1 = " + count1); // 1

		restClient.delete()
			.uri("/v2/comments/{commentId}", commentResponse.getCommentId())
			.retrieve();

		Long count2 = restClient.get()
			.uri("/v2/comments/articles/{articleId}/count", 2L)
			.retrieve()
			.body(Long.class);
		System.out.println("count2 = " + count2); // 0
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
