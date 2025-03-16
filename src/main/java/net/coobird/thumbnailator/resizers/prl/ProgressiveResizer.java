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

import java.awt.image.BufferedImage;
import java.util.concurrent.Executor;

import net.coobird.thumbnailator.resizers.Resizer;
import net.coobird.thumbnailator.util.ThbUtils;

/**
 * Wraps itself around a resizer or parallel resizer,
 * and uses it in a progressive way for downscaling.
 */
public class ProgressiveResizer implements ParallelResizer {
	
	private static final double MAX_STEP_DOWNSCALING = 2.0;
	
	private final Resizer resizer;
	
	/*
	 * 
	 */
	
	/**
	 * If the specified resizer implements ParallelResizer,
	 * each resizing step is done using its resize method
	 * taking an eventual parallel executor as argument.
	 * 
	 * @param resizer Must not be null.
	 * @throws NullPointerException if resizer is null.
	 */
	public ProgressiveResizer(Resizer resizer) {
		this.resizer = ThbUtils.requireNonNull(resizer);
	}
	
	@Override
	public void resize(
		BufferedImage srcImage,
		BufferedImage destImage) {
		this.resize(srcImage, destImage, null);
	}
	
	@Override
	public void resize(
		BufferedImage srcImage,
		BufferedImage destImage,
		Executor parallelExecutor) {
		
		final int sw = srcImage.getWidth();
		final int sh = srcImage.getHeight();
		final int dw = destImage.getWidth();
		final int dh = destImage.getHeight();
		
		BufferedImage tmpSrcImage = srcImage;
		int tmpSw = sw;
		int tmpSh = sh;
		while (true) {
			final int tmpDw = this.computeDownscaledSpan(tmpSw, dw);
			final int tmpDh = this.computeDownscaledSpan(tmpSh, dh);
			if ((tmpDw <= dw)
				&& (tmpDh <= dh)) {
				/*
				 * We got too far down,
				 * or just reached destination spans:
				 * will finish with destination image.
				 */
				break;
			}
			
			/*
			 * Not reusing temporary image array to draw it into itself,
			 * for it creates dynamic artifacts in case of parallelization,
			 * and possibly harder to detect static ones in sequential case.
			 * Not bothering to reuse array in case of 3 or more temporary
			 * images, because the third one should be relatively small
			 * already (at most 1/8th of source image).
			 */
			final BufferedImage tmpDestImage =
				new BufferedImage(
					tmpDw,
					tmpDh,
					BufferedImage.TYPE_INT_ARGB_PRE);
			
			PrlResizersUtils.resizeEventuallyInParallel(
				this.resizer,
				tmpSrcImage,
				tmpDestImage,
				parallelExecutor);
			
			// For next round or post-loop scaling.
			tmpSrcImage = tmpDestImage;
			tmpSw = tmpDw;
			tmpSh = tmpDh;
		}
		
		PrlResizersUtils.resizeEventuallyInParallel(
			this.resizer,
			tmpSrcImage,
			destImage,
			parallelExecutor);
	}
	
	/*
	 * 
	 */
	
	private int computeDownscaledSpan(int previousSpan, int destSpan) {
		/*
		 * Ceil to make sure span is never divided
		 * by more than maxStepDownscaling,
		 * unless factor is too close to 1
		 * in which case we force downscaling.
		 */
		int ret = Math.max(destSpan, (int) Math.ceil(previousSpan * (1.0 / MAX_STEP_DOWNSCALING)));
		if ((ret > destSpan) && (ret == previousSpan)) {
			// Did not downscale, but could: forcing downscaling.
			ret--;
		}
		return ret;
	}
}
