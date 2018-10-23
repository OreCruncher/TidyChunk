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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.blockartistry.tidychunk.TidyChunk;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT, modid = TidyChunk.MOD_ID)
public class ThePurge {

	public static Set<ChunkPos> chunks = new HashSet<>();
	private static World check;

	public static long chunksChecked = 0;
	public static long entitiesWiped = 0;

	@SubscribeEvent
	public static void onWorldTick(final TickEvent.WorldTickEvent evt) {
		if (evt.side != Side.SERVER)
			return;

		if (evt.phase == Phase.START) {
			if (check != null)
				searchAndDestroy(check);
			chunks = new HashSet<>();
			check = evt.world;
		} else {
			worldCheck(evt.world);
			searchAndDestroy(evt.world);
			check = null;
		}
	}

	@SubscribeEvent
	public static void onChunkPopulate(final PopulateChunkEvent.Pre evt) {
		// Possible that it is null because of initial world load
		if (check == null) {
			check = evt.getWorld();
		} else {
			worldCheck(evt.getWorld());
		}
		chunks.add(new ChunkPos(evt.getChunkX(), evt.getChunkZ()));
	}

	private static void searchAndDestroy(final World world) {
		if (chunks.size() > 0) {
			chunksChecked += chunks.size();
			final List<EntityItem> theList = world.getEntities(EntityItem.class, t -> {
				return chunks.contains(new ChunkPos(t.getPosition()));
			});

			for (final EntityItem item : theList) {
				item.setDead();
			}
			entitiesWiped += theList.size();
			
			TidyChunk.log().debug("Chunks checked: %d, entities wiped: %d", chunks.size(), theList.size());
		}

	}

	private static void worldCheck(final World w) {
		if (check != w)
			throw new IllegalStateException("World doesnt match!");
	}
}
