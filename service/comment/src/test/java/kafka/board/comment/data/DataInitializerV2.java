package kafka.board.comment.data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import kafka.board.comment.entity.CommentPath;
import kafka.board.comment.entity.CommentV2;
import kuke.board.common.snowflake.Snowflake;

@SpringBootTest
public class DataInitializerV2 {
	@PersistenceContext
	EntityManager entityManager;
	@Autowired
	TransactionTemplate transactionTemplate;
	Snowflake snowflake = new Snowflake();
	CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT); //여러 스레드의 작업 완료를 기다리는 동기화 도구

	static final int BULK_INSERT_SIZE = 2000;
	static final int EXECUTE_COUNT = 6000;

	@Test
	void initialze() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (int i = 0; i < EXECUTE_COUNT; i++) {
			int start = i * BULK_INSERT_SIZE;
			int end = (i + 1) * BULK_INSERT_SIZE;
			executorService.submit(() -> {
				insert(start, end); //멀티스레드에서 path가 unique하도록 범위 지정
				latch.countDown();
				System.out.println("latch.getCount() = " + latch.getCount());
			});
		}
		latch.await(); //모든 스레드가 작업을 마칠 때까지 대기
		executorService.shutdown();
	}

	void insert(int start, int end) {
		transactionTemplate.executeWithoutResult(status -> { //하나의 트랜잭션으로
			for (int i = start; i < end; i++) {
				CommentV2 comment = CommentV2.create(
					snowflake.nextId(),
					"content",
					1L,
					1L,
					toPath(i)
				);
				entityManager.persist(comment);
			}
		});
	}

	private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final int DEPTH_CHUNK_SIZE = 5;

	CommentPath toPath(int value) {
		String path = "";
		for (int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
			path = CHARSET.charAt(value % CHARSET.length()) + path;
			value = value / CHARSET.length();
		}
		return CommentPath.create(path);
	}

}
