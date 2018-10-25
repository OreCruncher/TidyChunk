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

package org.blockartistry.tidychunk.proxy;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public interface IProxy {

	/**
	 * Indicates if the mod is running on a dedicated server. Should be overridden
	 * and the correct information provided.
	 * 
	 * @return true if running on dedicated server; false otherwise
	 */
	boolean isDedicatedServer();

	/**
	 * Called during the mod's pre initialization phase. Override to provide logic.
	 * 
	 * @param event
	 *            The fired event
	 */
	default void preInit(@Nonnull final FMLPreInitializationEvent event) {
		// This method intentionally left blank
	}

	/**
	 * Called during the mod's initialization phase. Override to provide logic.
	 * 
	 * @param event
	 *            The fired event
	 */
	default void init(@Nonnull final FMLInitializationEvent event) {
		// This method intentionally left blank
	}

	/**
	 * Called during the mod's post initialization phase. Override to provide logic.
	 * 
	 * @param event
	 *            The fired event
	 */
	default void postInit(@Nonnull final FMLPostInitializationEvent event) {
		// This method intentionally left blank
	}

	/**
	 * Called when Forge has finished loading. Override to provide logic.
	 * 
	 * @param event
	 *            The fired event.
	 */
	default void loadCompleted(@Nonnull final FMLLoadCompleteEvent event) {
		// This method intentionally left blank
	}

}
