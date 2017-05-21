package net.ddns.templex.discordbot;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.btobastian.javacord.entities.message.embed.EmbedBuilder;

/* TemplexDiscordBot: A Discord bot for the Templex Discord server.
 * Copyright (C) 2017  VTCAKAVSMoACE
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public final class Util {

	private Util() {
	}

	/**
	 * Builds an embedded message (modified from Moudoux/DiscordSelfBot)
	 * 
	 * @param title
	 * @param description
	 * @param footer
	 * @param image
	 * @param thumbnail
	 * @param color
	 * @return EmbedBuilder
	 */
	public static EmbedBuilder generateEmbedBuilder(final String title, final String description, final String footer,
			final String image, final String thumbnail, final Color color) {
		final EmbedBuilder emb = new EmbedBuilder();
		if (title == null) {
			return null;
		}
		emb.setTitle(title);
		if (description != null)
			emb.setDescription(description);
		if (footer != null)
			emb.setFooter(footer);
		else
			setDefaultEmbedFooter(emb);
		if (image != null)
			emb.setImage(image);
		if (thumbnail != null)
			emb.setThumbnail(thumbnail);
		if (color != null)
			emb.setColor(color);
		else
			emb.setColor(Color.RED);
		return emb;
	}

	public static void setDefaultEmbedFooter(EmbedBuilder emb) {
		emb.setFooter(
				"Templex Discord Bot | Message sent "
						+ new SimpleDateFormat("MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()),
				"http://templex.ddns.net/assets/images/e-mail-clipart-clipart-primary-email4-2-128x128-21.png");

	}

}
