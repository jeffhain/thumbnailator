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
package net.coobird.thumbnailator.resizers.prl;

import java.awt.Graphics2D;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import net.coobird.thumbnailator.resizers.AbstractResizer;
import net.coobird.thumbnailator.util.ThbUtils;

/**
 * Abstract class to implement parallel resizers.
 * 
 * To ensure no actual parallelization, return Integer.MAX_VALUE
 * for both abstract thresholds methods (Integer.MAX_VALUE is a prime,
 * so no int product of width * height can be equal to it).
 */
public abstract class AbstractParallelResizer extends AbstractResizer implements ParallelResizer {
	
	/**
	 * To avoid over-splitting, since we don't use
	 * a split-as-needed parallelization engine.
	 * 
	 * Must not be too small else work will not be cut into small enough chunks
	 * to keep all workers busy until near completion.
	 * Must not be too large else might split into more chunks than needed
	 * and add useless overhead.
	 * 
	 * Our work load being split into approximately equal amounts,
	 * a value of 10 should allow an execution time usually not larger
	 * than "theoretical optimal * 1.1", with 1.1 = 1 + 1/10.
	 */
	private static final int MAX_RUNNABLE_COUNT_PER_CORE = 10;
	
	private static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
	
	/*
	 * 
	 */
	
	private class MyCmnData {
		final BufferedImage srcImage;
		final BufferedImage destImage;
		final CountDownLatch latch;
		public MyCmnData(
			BufferedImage srcImage,
			BufferedImage destImage,
			CountDownLatch latch) {
			this.srcImage = srcImage;
			this.destImage = destImage;
			this.latch = latch;
		}
	}
	
	private class MyPrlRunnable implements Runnable {
		private final MyCmnData cmn;
		private final int destStartRow;
		private final int destEndRow;
		public MyPrlRunnable(
			MyCmnData cmn,
			int destStartRow,
			int destEndRow) {
			this.cmn = cmn;
			this.destStartRow = destStartRow;
			this.destEndRow = destEndRow;
		}
		@Override
		public void run() {
			try {
				resizePart(
					this.cmn.srcImage,
					this.destStartRow,
					this.destEndRow,
					this.cmn.destImage);
			} finally {
				this.cmn.latch.countDown();
			}
		}
	}
	
	/*
	 * 
	 */
	
	protected AbstractParallelResizer(
		Object interpolationValue,
		Map<Key, Object> hints) {
		super(interpolationValue, hints);
	}
	
	/*
	 * 
	 */
	
	@Override
	public void resize(
		BufferedImage srcImage,
		BufferedImage destImage,
		Executor parallelExecutor) {
		
		this.performChecks(srcImage, destImage);
		
		final int sw = srcImage.getWidth();
		final int sh = srcImage.getHeight();
		final int dw = destImage.getWidth();
		final int dh = destImage.getHeight();
		
		/*
		 * Images spans can be assumed not to be zero,
		 * thanks to SampleModel constructor check.
		 */
		
		// 1 when going sequential, 2 or more when going parallel.
		final int partCount;
		if (parallelExecutor != null) {
			final int srcAreaThreshold = this.getSrcAreaThresholdForSplit();
			final int destAreaThreshold = this.getDestAreaThresholdForSplit();
			ThbUtils.requireSupOrEq(2, srcAreaThreshold, "getSrcAreaThresholdForSplit()");
			ThbUtils.requireSupOrEq(2, destAreaThreshold, "getDestAreaThresholdForSplit()");
			
			// Areas always < Integer.MAX_VALUE, since it's a prime.
			final int srcArea = sw * sh;
			final int destArea = dw * dh;
			
			final int partCountDueToSrc =
				ThbUtils.toRange(1, dh,
					(int) Math.ceil((srcArea + 1) / (double) srcAreaThreshold));
			final int partCountDueToDest =
				ThbUtils.toRange(1, dh,
					(int) Math.ceil((destArea + 1) / (double) destAreaThreshold));
			final int theoreticalPartCount = Math.max(partCountDueToSrc, partCountDueToDest);
			
			final int maxPartCount = CORE_COUNT * MAX_RUNNABLE_COUNT_PER_CORE;
			partCount = Math.min(maxPartCount, theoreticalPartCount);
		} else {
			partCount = 1;
		}
		
		final boolean prlElseSeq = (partCount >= 2);
		
		if (prlElseSeq) {
			parallelResize(
				partCount,
				srcImage,
				destImage,
				parallelExecutor);
		} else {
			/*
			 * Part is the whole images.
			 * 
			 * Not calling this.resize(_,_), since it might
			 * delegate to resize(_,_,_), and cause stack overflow.
			 * 
			 * Not calling super.resize(), since it would not allow
			 * for overriding sequential behavior, if we wanted to.
			 */
			final int destYStart = 0;
			final int destYEnd = dh - 1;
			resizePart(
				srcImage,
				destYStart,
				destYEnd,
				destImage);
		}
	}
	
	/*
	 * 
	 */
	
	/**
	 * Useful for boxsampling (workload for other algorithms typically
	 * only depends on destination area (other than due to src memory load)).
	 * 
	 * @return The source area, in number of source pixels,
	 *         from which it's worth to split in two for parallelization.
	 *         Must be >= 2.
	 */
	protected abstract int getSrcAreaThresholdForSplit();
	
	/**
	 * Normally useful for all algorithms.
	 * 
	 * @return The destination area, in number of destination pixels,
	 *         from which it's worth to split in two for parallelization.
	 *         Must be >= 2.
	 */
	protected abstract int getDestAreaThresholdForSplit();
	
	/*
	 * 
	 */
	
	/**
	 * Called in case of parallel resizing.
	 * 
	 * @param partCount Always >= 2.
	 * @param parallelExecutor Never null.
	 */
	private void parallelResize(
		int partCount,
		BufferedImage srcImage,
		BufferedImage destImage,
		Executor parallelExecutor) {
		
		final int dh = destImage.getHeight();
		
		final Runnable[] runnableArr = new Runnable[partCount];
		
		final CountDownLatch latch =
			new CountDownLatch(partCount);
		
		final MyCmnData cmn =
			new MyCmnData(
				srcImage,
				destImage,
				latch);
		
		int prevDestEndRowIndex = -1;
		for (int i = 0; i < partCount; i++) {
			int destStartRowIndex = prevDestEndRowIndex + 1;
			// in ]0,1]
			final double endRowRatio = (i + 1) / (double) partCount;
			// Increments of at least 1 per round,
			// since partCount <= targetHeight.
			// Not using round(), which behavior depends on JDK version.
			int destEndRowIndex = (int) Math.rint(endRowRatio * (dh - 1));
			runnableArr[i] = new MyPrlRunnable(
				cmn,
				destStartRowIndex,
				destEndRowIndex);
			prevDestEndRowIndex = destEndRowIndex;
		}
		
		ThbPrlEngine.parallelRun(
			runnableArr,
			parallelExecutor,
			latch);
	}
	
	/**
	 * @param srcImage Source image.
	 * @param destYStart Destination "y" to start from (inclusive).
	 * @param destYEnd Destination "y" to end at (inclusive).
	 * @param destImage Destination image.
	 */
	private void resizePart(
		BufferedImage srcImage,
		//
		int destYStart,
		int destYEnd,
		BufferedImage destImage) {
		
		final int dw = destImage.getWidth();
		final int dh = destImage.getHeight();
		
		/*
		 * We assume we can do that concurrently,
		 * which seems to work.
		 */
		final Graphics2D g = this.createGraphics(destImage);
		try {
			/*
			 * This clip is what allows parallel work chunks
			 * not to step on each other's toes.
			 * We only pass here when we have actual parts,
			 * so we always need to make this clip.
			 */
			final int destYSpan = (destYEnd - destYStart + 1);
			g.setClip(0, destYStart, dw, destYSpan);
			
			g.drawImage(srcImage, 0, 0, dw, dh, null);
		} finally {
			g.dispose();
		}
	}
}
