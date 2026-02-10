package kafka.board.view.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kafka.board.view.service.ArticleViewService;
import lombok.RequiredArgsConstructor;

@RequestMapping("/v1/article-views")
@RestController
@RequiredArgsConstructor
public class ArticleViewController {
	private final ArticleViewService articleViewService;

	@PostMapping("/articles/{articleId}")
	public Long increase(@PathVariable("articleId") Long articleId) {
		return articleViewService.increase(articleId);
	}

	@GetMapping("/articles/{articleId}/count")
	public Long count(@PathVariable("articleId") Long articleId) {
		return articleViewService.count(articleId);
	}
}
