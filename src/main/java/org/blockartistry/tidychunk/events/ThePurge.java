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

import org.blockartistry.tidychunk.TidyChunk;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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

@EventBusSubscriber(modid = TidyChunk.MOD_ID)
public class ThePurge {

	private static Int2ObjectArrayMap<WorldContext> worldData = new Int2ObjectArrayMap<>();

	/*
	 * Need to clean up our world state when the world unloads. Do a final pass
	 * getting rid of EntityItems as needed and remove the context from our tracking
	 * map.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(@Nonnull final WorldEvent.Unload evt) {
		final World w = evt.getWorld();
		if (w.isRemote)
			return;
		// One final purge pass
		getWorldContext(w).searchAndDestroy(w);
		worldData.remove(w.provider.getDimension());

	}

	/*
	 * At the end of each world tick process the loaded entities looking for
	 * candidates for removal. Remove any chunk tracking data that has expired.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldTick(@Nonnull final TickEvent.WorldTickEvent evt) {
		if (evt.side != Side.SERVER || evt.phase != Phase.END)
			return;

		final WorldContext ctx = getWorldContext(evt.world);
		ctx.searchAndDestroy(evt.world);
		ctx.removeOldContext(evt.world);
	}

	/*
	 * When a chunk is populated it is considered new. Add tracking data so that
	 * entities can be appropriately handled when joining the world.
	 */
	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
	public static void onChunkPopulate(@Nonnull final PopulateChunkEvent.Pre evt) {
		// This should never happen on a client thread, but double check
		// to be sure
		if (evt.getWorld().isRemote)
			return;
		final WorldContext ctx = getWorldContext(evt.getWorld());
		ctx.add(new ChunkPos(evt.getChunkX(), evt.getChunkZ()), evt.getWorld());
	}

	/*
	 * Check the entity type and it's position against the chunks in our tracking
	 * list. If the entity is within the set it is killed and the join event
	 * cancelled.
	 */
	@SubscribeEvent
	public static void onEntityJoin(@Nonnull final EntityJoinWorldEvent evt) {
		final Entity entity = evt.getEntity();
		final World world = entity.getEntityWorld();
		if (world == null || world.isRemote || !WorldContext.isTargetEntity(entity))
			return;

		final WorldContext ctx = getWorldContext(world);
		if (ctx.isContained(entity)) {
			ctx.removeEntity(entity);
			evt.setCanceled(true);
		}
	}

	@Nonnull
	private static WorldContext createWorldContext(final World w) {
		WorldContext ctx = null;
		worldData.put(w.provider.getDimension(), ctx = new WorldContext());
		return ctx;
	}

	@Nonnull
	private static WorldContext getWorldContext(@Nonnull final World w) {
		final WorldContext ctx = worldData.get(w.provider.getDimension());
		return ctx == null ? createWorldContext(w) : ctx;
	}

}
