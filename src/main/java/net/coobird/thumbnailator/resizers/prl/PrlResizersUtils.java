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

public class PrlResizersUtils {
	
	private PrlResizersUtils() {
	}
	
	/**
	 * Calls ParallelResizer resize method if the specified resizer
	 * implements it (and even if the specified executor is null).
	 * 
	 * @param parallelExecutor Can be null, even if resizer implements ParallelResizer.
	 */
	public static void resizeEventuallyInParallel(
		Resizer resizer,
		BufferedImage srcImage,
		BufferedImage destImage,
		Executor parallelExecutor) {
		if (resizer instanceof ParallelResizer) {
			final ParallelResizer prlResizer = (ParallelResizer) resizer;
			// Executor allowed to be null, so no need to check that here.
			prlResizer.resize(srcImage, destImage, parallelExecutor);
		} else {
			resizer.resize(srcImage, destImage);
		}
	}
}
