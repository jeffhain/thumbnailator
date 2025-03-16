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

/**
 * Interface for parallel resizers.
 */
public interface ParallelResizer extends Resizer {
	
	/**
	 * Resizes an image using the specified parallel executor if any.
	 * <p>
	 * The source image is resized to fit the dimensions of the destination
	 * image and drawn.
	 * 
	 * @param srcImage      The source image.
	 * @param destImage     The destination image.
	 * @param parallelExecutor     The executor used for parallelization. Can be null.
	 */
	public void resize(
		BufferedImage srcImage,
		BufferedImage destImage,
		Executor parallelExecutor);
}
