package kafka.board.comment.entity;

import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class CommentPath {
	private String path;

	private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final int DEPTH_CHUNK_SIZE = 5;
	private static final int MAX_DEPTH = 5;

	// MIN_CHUNK = "00000"
	private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
	// MAX_CHUNK = "zzzzz"
	private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

	public static CommentPath create(String path) {
		if (isDepthOverflow(path)) {
			throw new IllegalStateException("depth overflowed");
		}
		CommentPath commentPath = new CommentPath();
		commentPath.path = path;
		return commentPath;
	}

	private static boolean isDepthOverflow(String path) {
		return calDepth(path) > MAX_DEPTH;
	}

	private static int calDepth(String path) {
		return path.length() / DEPTH_CHUNK_SIZE;
	}

	public int getDepth() {
		return calDepth(path);
	}

	public boolean isRoot() {
		return calDepth(path) == 1;
	}

	public String getParentPath() {
		if (isRoot()) {
			return null;
		}
		return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
	}

	public CommentPath createChildCommentPath(String descendantsTopPath) {
		if (descendantsTopPath == null) {
			return CommentPath.create(path + MIN_CHUNK);
		}

		String childrenTopPath = findChildrenTopPath(descendantsTopPath);
		return CommentPath.create(increase(childrenTopPath));
	}

	private String findChildrenTopPath(String descendantsTopPath) {
		return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
	}

	private String increase(String path) {
		String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
		if (isChunkOverflowed(lastChunk)) {
			throw new IllegalStateException("chunk overflowed");
		}

		int charsetLength = CHARSET.length();

		int value = 0; //십진수로 변환해 1증가 시킨 후 다시 charset으로 변환
		for (char ch : lastChunk.toCharArray()) {
			value = value * charsetLength + CHARSET.indexOf(ch);
		}

		value = value + 1;

		String result = "";
		for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
			result = CHARSET.charAt(value % charsetLength) + result;
			value = value / charsetLength;
		}

		return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
	}

	private boolean isChunkOverflowed(String lastChunk) {
		return MAX_CHUNK.equals(lastChunk);
	}
}
