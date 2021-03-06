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

package org.blockartistry.tidychunk;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = TidyChunk.MOD_ID)
@Config(modid = TidyChunk.MOD_ID, type = Type.INSTANCE, name = TidyChunk.MOD_ID)
//@LangKey("config.tidychunk.title")
public class Configuration {

	@LangKey("config.tidychunk.logging")
	public static Logging logging = new Logging();

	@LangKey("config.tidychunk.options")
	public static Options options = new Options();

	public static class Logging {
		@LangKey("config.tidychunk.logging.enableLogging")
		@Comment({ "Enables debug logging output for diagnostics" })
		public boolean enableLogging = false;

		@LangKey("config.tidychunk.logging.enableVersionCheck")
		@Comment({ "Enables display of chat messages related to newer versions", "of the mod being available." })
		public boolean enableVersionCheck = true;
	}

	public static class Options {
		@LangKey("config.tidychunk.options.tickspan")
		@Comment({ "Number of ticks post chunk generation to check for EntityItems",
				"0 means use internal default (10 ticks)" })
		@RangeInt(min = 0, max = 100)
		public int tickSpan = 0;

		@LangKey("config.tidychunk.options.fakeplayers")
		@Comment({ "List of fake players from which to remove block drops",
				"May not work in all circumstances due to Forge swiss army knife interfaces" })
		public String[] fakePlayers = new String[] {};
	}

	@SubscribeEvent
	public static void onConfigChangedEvent(final OnConfigChangedEvent event) {
		if (event.getModID().equals(TidyChunk.MOD_ID)) {
			ConfigManager.sync(TidyChunk.MOD_ID, Type.INSTANCE);
			TidyChunk.log().setDebug(Configuration.logging.enableLogging);
		}
	}
}
