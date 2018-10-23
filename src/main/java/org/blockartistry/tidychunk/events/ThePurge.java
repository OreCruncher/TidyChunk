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

import java.util.List;

import org.blockartistry.tidychunk.Configuration;
import org.blockartistry.tidychunk.TidyChunk;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

/*
 * This logic tracks chunk generation and purges entity items that
 * fall within the chunk for a config defined tick interval.
 */

@EventBusSubscriber(value = Side.CLIENT, modid = TidyChunk.MOD_ID)
public class ThePurge {

	private static class WorldContext {

		public final Object2LongOpenHashMap<ChunkPos> chunks = new Object2LongOpenHashMap<>();

		public void searchAndDestroy(final World world) {
			if (this.chunks.size() > 0) {
				final List<EntityItem> theList = world.getEntities(EntityItem.class, t -> {
					return this.chunks.keySet().contains(new ChunkPos(t.getPosition()));
				});

				for (final EntityItem item : theList) {
					item.setDead();
				}

				if (theList.size() > 0) {
					TidyChunk.log().debug("Chunks checked: %d, entities wiped: %d", this.chunks.size(), theList.size());
				}
			}
		}

		public void removeOldContext(final World world) {
			// Remove any contexts that are older than 3 ticks
			this.chunks.entrySet().removeIf(ctx -> {
				return (world.getWorldTime() - ctx.getValue()) > Configuration.options.tickSpan;
			});
		}

	}

	private static Int2ObjectArrayMap<WorldContext> worldData = new Int2ObjectArrayMap<>();

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onWorldLoad(final WorldEvent.Load evt) {
		final World w = evt.getWorld();
		if (w.isRemote)
			return;

		worldData.put(w.provider.getDimension(), new WorldContext());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(final WorldEvent.Unload evt) {
		final World w = evt.getWorld();
		if (w.isRemote)
			return;
		// One final purge pass
		getWorldContext(w).searchAndDestroy(w);
		worldData.remove(w.provider.getDimension());

	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldTick(final TickEvent.WorldTickEvent evt) {
		if (evt.side != Side.SERVER || evt.phase != Phase.END)
			return;

		final WorldContext ctx = worldData.get(evt.world.provider.getDimension());
		if (ctx == null) {
			final String msg = String.format("WorldContext was not found for dimension %d!",
					evt.world.provider.getDimension());
			throw new IllegalStateException(msg);
		}

		ctx.searchAndDestroy(evt.world);
		ctx.removeOldContext(evt.world);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public static void onChunkPopulate(final PopulateChunkEvent.Pre evt) {
		final WorldContext ctx = getWorldContext(evt.getWorld());
		ctx.chunks.put(new ChunkPos(evt.getChunkX(), evt.getChunkZ()), evt.getWorld().getWorldTime());
	}

	private static WorldContext getWorldContext(final World w) {
		final WorldContext ctx = worldData.get(w.provider.getDimension());
		if (ctx == null) {
			final String msg = String.format("WorldContext was not found for dimension %d!", w.provider.getDimension());
			throw new IllegalStateException(msg);
		}
		return ctx;
	}

}
