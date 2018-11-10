/*
 * This file is part of TidyChunk, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.tidychunk.events;

import javax.annotation.Nonnull;

import org.blockartistry.tidychunk.Configuration;
import org.blockartistry.tidychunk.TidyChunk;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

class WorldContext {

	private static final int DEFAULT_TICK_SPAN = 15;

	private final Object2LongOpenHashMap<ChunkPos> chunks = new Object2LongOpenHashMap<>();
	private int removeCount = 0;

	public static boolean isTargetEntity(@Nonnull final Entity e) {
		return e.getClass() == EntityItem.class;
	}

	public void add(final ChunkPos pos, final World world) {
		this.chunks.put(pos, world.getTotalWorldTime());
	}

	public void searchAndDestroy(@Nonnull final World world) {
		if (this.chunks.size() > 0) {
			world.getEntities(EntityItem.class, t -> isTargetEntity(t) && isContained(t)).forEach(e -> removeEntity(e));
			if (this.removeCount > 0) {
				TidyChunk.log().debug("Entities wiped: %d", this.removeCount);
				this.removeCount = 0;
			}
		}
	}

	public void removeOldContext(@Nonnull final World world) {
		final int span = Configuration.options.tickSpan == 0 ? DEFAULT_TICK_SPAN : Configuration.options.tickSpan;
		// Remove any contexts that are older than the configured number of ticks
		this.chunks.entrySet().removeIf(ctx -> {
			return (world.getTotalWorldTime() - ctx.getValue()) > span;
		});
	}

	public void removeEntity(@Nonnull final Entity entity) {
		entity.setDead();
		this.removeCount++;
	}

	public boolean isContained(@Nonnull final Entity entity) {
		return entity.isEntityAlive() && this.chunks.containsKey(new ChunkPos(entity.getPosition()));
	}
}
