package kafka.board.article.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClient;

import kafka.board.article.service.response.ArticlePageResponse;
import kafka.board.article.service.response.ArticleResponse;

public class ArticleApiTest {
	RestClient restClient = RestClient.create("http://localhost:9000");

	@Test
	void createTest() {
		ArticleResponse response = create(new ArticleCreateRequest("hi", "content", 1L, 1L));
		System.out.println("response = " + response);
	}

	@Test
	void readTest() {
		ArticleResponse response = read(265774697418973184L);
		System.out.println("response = " + response);
	}

	@Test
	void updateTest() {
		update(265774697418973184L, new ArticleUpdateRequest("updated title", "updated content"));
		ArticleResponse response = read(265774697418973184L);
		System.out.println("response = " + response);
		assertEquals("updated title", response.getTitle());
	}

	@Test
	void deleteTest() {
		delete(265774697418973184L);
		assertThrows(InternalServerError.class, ()-> read(265774697418973184L));
	}


	@Test
	void readAllTEst(){
		ArticlePageResponse response = restClient.get()
			.uri("/v1/articles?boardId={boardId}&page={page}&pageSize={pageSize}", 1L, 50000L, 30L)
			.retrieve()
			.body(ArticlePageResponse.class);

		System.out.println("response.getArticleCount() = " + response.getArticleCount());
		for(ArticleResponse articleResponse : response.getArticles()) {
			System.out.println("articleId = " + articleResponse.getArticleId());
		}
	}

	void delete(long articleId) {
		restClient.delete()
			.uri("/v1/articles/{articleId}", articleId)
			.retrieve();
	}

	ArticleResponse update(long articleId, ArticleUpdateRequest request) {
		return restClient.put()
			.uri("/v1/articles/{articleId}", articleId)
			.body(request)
			.retrieve()
			.body(ArticleResponse.class);
	}

	ArticleResponse read(long articleId) {
		return restClient.get()
			.uri("/v1/articles/{articleId}", articleId)
			.retrieve()
			.body(ArticleResponse.class);
	}

	ArticleResponse create(ArticleCreateRequest request) {
		return restClient.post()
			.uri("/v1/articles")
			.body(request)
			.retrieve() //응답을 가져온다
			.body(ArticleResponse.class);
	}

	record ArticleCreateRequest(String title, String content, Long writerId, Long boardId) {
	}

	record ArticleUpdateRequest(String title, String content) {
	}
}
