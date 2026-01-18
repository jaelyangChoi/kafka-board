package kafka.board.article.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kafka.board.article.service.ArticleService;
import kafka.board.article.service.request.ArticleCreateRequest;
import kafka.board.article.service.request.ArticleUpdateRequest;
import kafka.board.article.service.response.ArticlePageResponse;
import kafka.board.article.service.response.ArticleResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/articles")
@RequiredArgsConstructor
public class ArticleController {
	private final ArticleService articleService;

	@GetMapping("/{articleId}")
	public ArticleResponse read(@PathVariable Long articleId) {
		return articleService.read(articleId);
	}

	@GetMapping
	public ArticlePageResponse readAll(
		@RequestParam("boardId") Long boardId,
		@RequestParam("page") Long page,
		@RequestParam("pageSize") Long pageSize
	) {
		return articleService.readAll(boardId, page, pageSize);
	}

	@PostMapping
	public ArticleResponse create(@RequestBody ArticleCreateRequest request) {
		return articleService.create(request);
	}

	@PutMapping("/{articleId}")
	public ArticleResponse update(@PathVariable Long articleId, @RequestBody ArticleUpdateRequest request) {
		return articleService.update(articleId, request);
	}

	@DeleteMapping("/{articleId}")
	public void delete(@PathVariable Long articleId) {
		articleService.delete(articleId);
	}




	@GetMapping("/infinite-scroll")
	public List<ArticleResponse> readAllInfiniteScroll(
		@RequestParam("boardId") Long boardId,
		@RequestParam("pageSize") Long pageSize,
		@RequestParam(value = "lastArticleId", required = false) Long lastArticleId
	) {
		return articleService.readAllInfiniteScroll(boardId, pageSize, lastArticleId);
	}
}
