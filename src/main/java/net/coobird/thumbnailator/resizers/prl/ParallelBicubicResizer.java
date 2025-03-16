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

import java.awt.RenderingHints;
import java.util.Collections;
import java.util.Map;

/**
 * Parallel bicubic resizer.
 */
public class ParallelBicubicResizer extends AbstractParallelResizer {
	
	private static final int DEST_AREA_THRESHOLD_FOR_SPLIT = 4 * 1024;
	
	/**
	 * Instantiates a {@link ParallelBicubicResizer} with default
	 * rendering hints.
	 */
	public ParallelBicubicResizer() {
		this(Collections.<RenderingHints.Key, Object>emptyMap());
	}
	
	/**
	 * Instantiates a {@link ParallelBicubicResizer} with the specified
	 * rendering hints.
	 * 
	 * @param hints     Additional rendering hints to apply.
	 */
	public ParallelBicubicResizer(Map<RenderingHints.Key, Object> hints) {
		super(RenderingHints.VALUE_INTERPOLATION_BICUBIC, hints);
	}
	
	@Override
	protected int getSrcAreaThresholdForSplit() {
		return Integer.MAX_VALUE;
	}
	
	@Override
	protected int getDestAreaThresholdForSplit() {
		return DEST_AREA_THRESHOLD_FOR_SPLIT;
	}
}
