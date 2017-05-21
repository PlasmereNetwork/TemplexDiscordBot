package net.ddns.templex.discordbot;

import static net.ddns.templex.discordbot.Util.generateEmbedBuilder;

import java.awt.Color;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import de.btobastian.javacord.listener.channel.ChannelChangeNameListener;
import de.btobastian.javacord.listener.channel.ChannelChangePositionListener;
import de.btobastian.javacord.listener.channel.ChannelChangeTopicListener;
import de.btobastian.javacord.listener.channel.ChannelCreateListener;
import de.btobastian.javacord.listener.channel.ChannelDeleteListener;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import io.github.trulyfree.modular.module.Module;
import net.ddns.templex.discordbot.commands.Command;

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

public class Bot implements Module {

	private static final String version = "0.0.1-SNAPSHOT";

	private static Logger logger = LoggerFactory.getLogger(Bot.class);

	private final DiscordAPI api;
	private boolean exitOnDisconnect;
	private ExecutorService exec;
	private Channel headsChannel;
	private Server templexDiscord;
	private Calendar startTime;

	public Bot(String token, boolean exitOnDisconnect) {
		this.api = Javacord.getApi(token, true);
		this.exitOnDisconnect = exitOnDisconnect;
	}

	@Override
	public boolean destroy() {
		api.disconnect();
		headsChannel = null;
		if (exitOnDisconnect) {
			System.exit(0);
		}
		return true;
	}

	@Override
	public boolean isReady() {
		return exec != null && api != null && headsChannel != null;
	}

	@Override
	public boolean setup() {
		exec = Executors.newCachedThreadPool();
		startTime = Calendar.getInstance();
		api.connect(new FutureCallback<DiscordAPI>() {
			@Override
			public void onSuccess(DiscordAPI arg0) {
				templexDiscord = api.getServerById("162683952295837696");
				headsChannel = api.getChannelById("252217330254217217");
				EmbedBuilder emb = generateEmbedBuilder("Templex Bot",
						"Templex Bot version " + version + " initialized.", null, null, null, Color.GREEN);
				headsChannel.sendMessage("", emb);
				api.registerListener(new GeneralizedListener());
				logger.info("Templex bot version " + version + " initialized.");
			}

			@Override
			public void onFailure(Throwable arg0) {
				logger.error("Failed to setup the bot!", arg0);
				destroy();
			}
		});
		return true;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public static String getVersion() {
		return version;
	}

	private class GeneralizedListener implements MessageCreateListener, ChannelChangeNameListener,
			ChannelChangePositionListener, ChannelChangeTopicListener, ChannelCreateListener, ChannelDeleteListener {

		@Override
		public void onChannelDelete(DiscordAPI arg0, Channel arg1) {
			exec.submit(() -> {
				EmbedBuilder emb = generateEmbedBuilder("Templex Discord Server Warning",
						"Channel " + arg1.getName() + " was deleted.", null, null, null, Color.RED);
				headsChannel.sendMessage("", emb);
				return true;
			});
		}

		@Override
		public void onChannelCreate(DiscordAPI arg0, Channel arg1) {
			exec.submit(() -> {
				EmbedBuilder emb = generateEmbedBuilder("Templex Discord Server Warning",
						"Channel " + arg1.getName() + " was created.", null, null, null, Color.RED);
				headsChannel.sendMessage("", emb);
				return true;
			});
		}

		@Override
		public void onChannelChangeTopic(DiscordAPI arg0, Channel arg1, String arg2) {
			exec.submit(() -> {
				EmbedBuilder emb = generateEmbedBuilder("Templex Discord Server Warning",
						"Channel " + arg1.getName() + "'s topic was modified!", null, null, null, Color.RED);
				headsChannel.sendMessage("", emb);
				return true;
			});
		}

		@Override
		public void onChannelChangePosition(DiscordAPI arg0, Channel arg1, int arg2) {
			exec.submit(() -> {
				EmbedBuilder emb = generateEmbedBuilder("Templex Discord Server Warning",
						"Channel " + arg1.getName() + " was moved!", null, null, null, Color.RED);
				headsChannel.sendMessage("", emb);
				return true;
			});
		}

		@Override
		public void onChannelChangeName(DiscordAPI arg0, Channel arg1, String arg2) {
			exec.submit(() -> {
				EmbedBuilder emb = generateEmbedBuilder("Templex Discord Server Warning",
						"Channel " + arg1.getName() + " was renamed to " + arg2, null, null, null, Color.RED);
				headsChannel.sendMessage("", emb);
				return true;
			});
		}

		@Override
		public void onMessageCreate(DiscordAPI arg0, Message arg1) {
			Channel channel = arg1.getChannelReceiver();
			if (channel == null || templexDiscord.equals(arg1.getChannelReceiver().getServer())) {
				exec.submit(() -> {
					Command command = Command.getCommandFromMessage(arg1.getContent());
					if (command != null) {
						command.execute(arg1, Bot.this);
					}
					return true;
				});
			}
		}

	}

}
