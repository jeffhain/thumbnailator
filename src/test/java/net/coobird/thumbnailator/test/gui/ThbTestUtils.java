/*
 * Copyright 2025 Jeff Hain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.coobird.thumbnailator.test.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

/**
 * Utils for tests.
 */
public class ThbTestUtils {
	
	private static class MyDefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger THREAD_NUM_PROVIDER =
			new AtomicInteger();
		public MyDefaultThreadFactory() {
		}
		@Override
		public Thread newThread(Runnable runnable) {
			final int threadNum = THREAD_NUM_PROVIDER.incrementAndGet();
			final String threadName =
				ThbTestUtils.class.getClass().getSimpleName()
				+ "-PRL-"
				+ threadNum;
			
			final Thread thread = new Thread(runnable, threadName);
			thread.setDaemon(true);
			
			return thread;
		}
	}
	
	/*
	 * 
	 */
	
	public static final int DEFAULT_PARALLELISM =
		Runtime.getRuntime().availableProcessors();
	public static final Executor DEFAULT_TPE =
		newPrlExec(DEFAULT_PARALLELISM);
	
	/*
	 * 
	 */
	
	private ThbTestUtils() {
	}
	
	/*
	 * 
	 */
	
	/**
	 * @return A TPE using daemon threads.
	 */
	public static Executor newPrlExec(int parallelism) {
		/*
		 * ForkJoinPool also works, might eb slower in some cases though,
		 * especially when used from a non-worker thread,
		 * since it's not optimized for latency but for throughput.
		 */
		return new ThreadPoolExecutor(
			parallelism,
			parallelism,
			0L,
			TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(),
			new MyDefaultThreadFactory());
	}
	
	public static void shutdownNow(Executor parallelExecutor) {
		// Best effort.
		if (parallelExecutor instanceof ExecutorService) {
			((ExecutorService) parallelExecutor).shutdownNow();
		}
	}
	
	public static BufferedImage loadImage(File file) {
		final BufferedImage image;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return image;
	}
}
