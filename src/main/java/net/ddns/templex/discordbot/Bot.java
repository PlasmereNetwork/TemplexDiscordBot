package net.ddns.templex.discordbot;

import static net.ddns.templex.discordbot.Util.generateEmbedBuilder;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

public class Bot {

	private static Logger logger = LoggerFactory.getLogger(Bot.class);
	private static final String gameString = "with minds";

	private final JsonParser parser;
	private final DiscordAPI api;
	private final String cleverBotToken;
	private final HashMap<Channel, CleverBotResponse> conversations;
	private boolean exitOnDisconnect;
	private ExecutorService exec;
	private Channel headsChannel;
	private Server templexDiscord;
	private Calendar startTime;

	public Bot(String token, String cleverBotToken, boolean exitOnDisconnect) {
		this.parser = new JsonParser();
		this.api = Javacord.getApi(token, true);
		this.cleverBotToken = cleverBotToken;
		this.conversations = new HashMap<>();
		this.exitOnDisconnect = exitOnDisconnect;
	}

	public boolean destroy() {
		api.disconnect();
		headsChannel = null;
		if (exitOnDisconnect) {
			System.exit(0);
		}
		return true;
	}

	public boolean isReady() {
		return exec != null && api != null && headsChannel != null;
	}

	public boolean setup() {
		exec = Executors.newCachedThreadPool();
		startTime = Calendar.getInstance();
		api.connect(new FutureCallback<DiscordAPI>() {
			@Override
			public void onSuccess(DiscordAPI arg0) {
				templexDiscord = api.getServerById("162683952295837696");
				headsChannel = api.getChannelById("327123182190460928");
				String version = getVersion();
				EmbedBuilder emb = generateEmbedBuilder("Templex Bot",
						"Templex Bot version " + version + " initialized.", null, null, null, Color.GREEN);
				headsChannel.sendMessage("", emb);
				api.registerListener(new GeneralizedListener());
				api.setGame(gameString);
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

	public HashMap<Channel, CleverBotResponse> getConversations() {
		return conversations;
	}

	public String getVersion() {
		return getClass().getPackage().getImplementationVersion();
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
			if (channel != null && templexDiscord.equals(channel.getServer())) {
				exec.submit(() -> {
					Command command = Command.getCommandFromMessage(arg1.getContent());
					if (command != null) {
						command.execute(arg1, Bot.this);
					} else if (arg1.getMentions().contains(api.getYourself())) {
						CleverBotResponse current = conversations.get(channel);
						if (current == null) {
							current = new CleverBotResponse(true);
							conversations.put(channel, current);
						}
						if (current.isAllowed()) {
							current.getNextResponse(arg1.getContent().replaceAll(api.getYourself().getName(), ""));
							if (current.getOutput() != null) {
								arg1.reply(current.getOutput());
							}
						}
					}
					return true;
				});
			}
		}
	}

	public class CleverBotResponse {

		private boolean allowed;
		private String cs;
		private String output;

		public CleverBotResponse(boolean allowed) {
			this.setAllowed(allowed);
			this.cs = null;
			this.output = null;
		}

		public String getOutput() {
			return output;
		}

		public boolean isAllowed() {
			return allowed;
		}

		public void setAllowed(boolean allowed) {
			this.allowed = allowed;
		}

		public void getNextResponse(String input) {
			if (cleverBotToken == null) {
				return;
			}
			StringBuilder address = new StringBuilder("https://www.cleverbot.com/getreply?key=");
			address.append(cleverBotToken);
			address.append("&input=");
			try {
				address.append(URLEncoder.encode(input, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.warn("Unable to encode input for CleverBot response!", e);
				this.output = null;
			}
			if (cs != null) {
				address.append("&cs=");
				address.append(cs);
			}
			try {
				URL url = new URL(address.toString());
				URLConnection connection = url.openConnection();
				InputStream inputStream = connection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[4096];
				int len;
				while ((len = inputStream.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				JsonObject responseObj = (JsonObject) parser.parse(baos.toString());
				this.cs = responseObj.get("cs").getAsString();
				this.output = responseObj.get("output").getAsString();
			} catch (Throwable e) {
				logger.warn("Unable to fetch CleverBot response!", e);
				this.output = null;
			}
		}
	}

}
