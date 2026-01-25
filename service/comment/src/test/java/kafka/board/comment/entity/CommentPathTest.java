package kafka.board.comment.entity;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CommentPathTest {

	@Test
	void createChildCommentPathTest() {
		// 00000 <- 생성
		createChildCommentPathTest(CommentPath.create(""), null, "00000");

		// 00000
		// 		00000 <-생성
		createChildCommentPathTest(CommentPath.create("00000"), null, "0000000000");

		// 00000
		// 00001 <- 생성
		createChildCommentPathTest(CommentPath.create(""), "00000", "00001");

		// 0000z
		// 		abcdz
		// 			 zzzzz
		// 				  zzzzz
		// 		abce0 <- 생성
		createChildCommentPathTest(CommentPath.create("0000z"), "0000zabcdzzzzzzzzzz", "0000zabce0");
	}

	void createChildCommentPathTest(CommentPath commentPath, String descendantsTopPath, String expectedPath) {
		CommentPath childCommentPath = commentPath.createChildCommentPath(descendantsTopPath);
		assertEquals(expectedPath, childCommentPath.getPath());
	}

	@Test
	void createChildCommentPathIfMaxDepthTest() {
		assertThatThrownBy(() ->
			CommentPath.create("zzzzz".repeat(5)).createChildCommentPath(null)
		).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void createChildCommentPathIfChunkOverflowTest() {
		// given
		CommentPath commentPath = CommentPath.create("");

		// when, then
		assertThatThrownBy(()-> commentPath.createChildCommentPath("zzzzz"))
			.isInstanceOf(IllegalStateException.class);
	}
}
