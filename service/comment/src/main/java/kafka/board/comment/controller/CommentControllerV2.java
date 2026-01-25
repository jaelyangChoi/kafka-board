package kafka.board.comment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kafka.board.comment.service.CommentServiceV2;
import kafka.board.comment.service.request.CommentCreateRequestV2;
import kafka.board.comment.service.response.CommentPageResponse;
import kafka.board.comment.service.response.CommentResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v2/comments")
@RequiredArgsConstructor
public class CommentControllerV2 {
	private final CommentServiceV2 commentService;

	@GetMapping("/{commentId}")
	public CommentResponse read(@PathVariable("commentId") Long commentId) {
		return commentService.read(commentId);
	}

	@PostMapping
	public CommentResponse create(@RequestBody CommentCreateRequestV2 request) {
		return commentService.create(request);
	}

	@DeleteMapping("/{commentId}")
	public void delete(@PathVariable("commentId") Long commentId) {
		commentService.delete(commentId);
	}

	@GetMapping
	public CommentPageResponse readAll(
		@RequestParam("articleId") Long articleId,
		@RequestParam("page") Long page,
		@RequestParam("pageSize") Long pageSize
	) {
		return commentService.readAll(articleId, page, pageSize);
	}

	@GetMapping("/infinite-scroll")
	public List<CommentResponse> readAllInfiniteScroll(
		@RequestParam("articleId") Long articleId,
		@RequestParam(value = "lastPath", required = false) String lastPath,
		@RequestParam("pageSize") Long pageSize
	) {
		return commentService.readAllInfiniteScroll(articleId, lastPath, pageSize);
	}
}
