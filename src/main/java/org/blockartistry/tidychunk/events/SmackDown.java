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

import java.util.Arrays;

import org.blockartistry.tidychunk.Configuration;
import org.blockartistry.tidychunk.TidyChunk;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/*
 * This handler attempts to discard drops from a fake player.  Sometimes
 * a mod implements world gen outside the normal framework and uses a
 * fake player to do the work (example: AncientWarfare Structures).
 */
@EventBusSubscriber(modid = TidyChunk.MOD_ID)
public class SmackDown {

	@SubscribeEvent
	public static void onBlockHarvestEvent(final HarvestDropsEvent evt) {
		if (evt.getWorld().isRemote)
			return;
		final EntityPlayer entity = evt.getHarvester();
		if (entity instanceof FakePlayer && isEntityBlacklisted(entity)) {
			evt.getDrops().clear();
		}
	}

	private static boolean isEntityBlacklisted(final EntityPlayer player) {
		final String[] fakes = Configuration.options.fakePlayers;
		return fakes.length > 0 && Arrays.asList(Configuration.options.fakePlayers).contains(player.getName());
	}
}
