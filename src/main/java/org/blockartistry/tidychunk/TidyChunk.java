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

import java.util.Arrays;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.blockartistry.tidychunk.proxy.Proxy;
import org.blockartistry.tidychunk.util.ForgeUtils;
import org.blockartistry.tidychunk.util.Localization;
import org.blockartistry.tidychunk.util.ModLog;
import org.blockartistry.tidychunk.util.VersionChecker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

@Mod(
		modid = TidyChunk.MOD_ID,
		useMetadata = true,
		dependencies = TidyChunk.DEPENDENCIES,
		version = TidyChunk.VERSION,
		acceptedMinecraftVersions = TidyChunk.MINECRAFT_VERSIONS,
		updateJSON = TidyChunk.UPDATE_URL,
		certificateFingerprint = TidyChunk.FINGERPRINT,
		acceptableRemoteVersions = "*"
)
public class TidyChunk {
	public static final String MOD_ID = "tidychunk";
	public static final String MOD_NAME = "TidyChunk";
	public static final String VERSION = "@VERSION@";
	public static final String MINECRAFT_VERSIONS = "[1.12.2,)";
	public static final String DEPENDENCIES = "required-after:forge@[14.23.1.2555,)";
	public static final String UPDATE_URL = "https://raw.githubusercontent.com/OreCruncher/TidyChunk/master/version.json";
	public static final String FINGERPRINT = "7a2128d395ad96ceb9d9030fbd41d035b435753a";

	@Instance(MOD_ID)
	protected static TidyChunk instance;

	@Nonnull
	public static TidyChunk instance() {
		return instance;
	}

	@SidedProxy(clientSide = "org.blockartistry.tidychunk.proxy.ProxyClient", serverSide = "org.blockartistry.tidychunk.proxy.Proxy")
	protected static Proxy proxy;
	protected static ModLog logger = ModLog.NULL_LOGGER;

	@Nonnull
	public static Proxy proxy() {
		return proxy;
	}

	@Nonnull
	public static ModLog log() {
		return logger;
	}

	public TidyChunk() {

	}

	@EventHandler
	public void preInit(@Nonnull final FMLPreInitializationEvent event) {

		logger = ModLog.setLogger(TidyChunk.MOD_ID, event.getModLog());

		MinecraftForge.EVENT_BUS.register(this);

		logger.setDebug(Configuration.logging.enableLogging);

		proxy.preInit(event);
	}

	@EventHandler
	public void init(@Nonnull final FMLInitializationEvent event) {
		proxy.init(event);
	}

	@EventHandler
	public void postInit(@Nonnull final FMLPostInitializationEvent event) {
		proxy.postInit(event);

		// Patch up metadata
		if (!proxy.isRunningAsServer()) {
			final ModMetadata data = ForgeUtils.getModMetadata(TidyChunk.MOD_ID);
			if (data != null) {
				data.name = Localization.format("tidychunk.metadata.Name");
				data.credits = Localization.format("tidychunk.metadata.Credits");
				data.description = Localization.format("tidychunk.metadata.Description");
				data.authorList = Arrays
						.asList(StringUtils.split(Localization.format("tidychunk.metadata.Authors"), ','));
			}
		}
	}

	@EventHandler
	public void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
		proxy.loadCompleted(event);
	}

	@EventHandler
	public void onFingerprintViolation(@Nonnull final FMLFingerprintViolationEvent event) {
		log().warn("Invalid fingerprint detected!");
	}

	@SubscribeEvent
	public void onConfigChangedEvent(final OnConfigChangedEvent event) {
		if (event.getModID().equals(MOD_ID)) {
			ConfigManager.sync(MOD_ID, Type.INSTANCE);
			log().setDebug(Configuration.logging.enableLogging);
		}
	}

	@SubscribeEvent
	public void playerLogin(final PlayerLoggedInEvent event) {
		if (Configuration.logging.enableVersionCheck)
			new VersionChecker(TidyChunk.MOD_ID, "tidychunk.msg.NewVersion").playerLogin(event);
	}

	////////////////////////
	//
	// Server state events
	//
	////////////////////////
	@EventHandler
	public void serverAboutToStart(@Nonnull final FMLServerAboutToStartEvent event) {
		proxy.serverAboutToStart(event);
	}

	@EventHandler
	public void serverStarting(@Nonnull final FMLServerStartingEvent event) {
		proxy.serverStarting(event);
	}

	@EventHandler
	public void serverStopping(@Nonnull final FMLServerStoppingEvent event) {
		proxy.serverStopping(event);
	}

	@EventHandler
	public void serverStopped(@Nonnull final FMLServerStoppedEvent event) {
		proxy.serverStopped(event);
	}

}
