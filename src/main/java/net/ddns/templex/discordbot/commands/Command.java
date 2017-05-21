package net.ddns.templex.discordbot.commands;

import static net.ddns.templex.discordbot.Util.generateEmbedBuilder;

import java.awt.Color;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.embed.EmbedBuilder;
import net.ddns.templex.discordbot.Bot;

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

public enum Command {
	HELP {
		@Override
		public boolean execute(Message message, Bot bot) {
			String[] arguments = getArguments(message);
			
			if (arguments.length == 0) {
				StringBuilder sb = new StringBuilder();
				Command[] commands = values();
				for (int i = 0; i < commands.length - 1; i++) {
					sb.append(COMMAND_PREFIX);
					sb.append(commands[i].toString().toLowerCase());
					sb.append("\n");
				}
				sb.append("View info about each command using `!help <command>`.");
				EmbedBuilder emb = generateEmbedBuilder("Available Commands:", sb.toString(), null, null,
						null, Color.GREEN);
				message.reply("", emb);
			} else {
				Command targetCommand = getCommandFromMessage("!" + arguments[0]);
				EmbedBuilder emb;
				if (targetCommand == UNKNOWN) {
					emb = generateEmbedBuilder("Help:", "Unknown command. List commands with `!help`", null, null, null, Color.RED);
				} else {
					emb = generateEmbedBuilder(targetCommand.name(), null, null, null, null, Color.GREEN);
					emb.addField("Description:", targetCommand.getDescription(), false);
					emb.addField("Syntax:", targetCommand.getUsage(), true);
				}
				message.reply("", emb);
			}
			return true;
		}

		@Override
		public String getDescription() {
			return "Returns information about commands implemented by this bot.";
		}

		@Override
		public String getUsage() {
			return COMMAND_PREFIX + "help\n" + COMMAND_PREFIX + "help <command>";
		}
	},
	STATUS {
		@Override
		public boolean execute(Message message, Bot bot) {
			boolean success = true;
			EmbedBuilder emb;
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress("templex.ddns.net", 25565), 10000);
				emb = generateEmbedBuilder("Status:", "Templex is online!", null, null, null, Color.GREEN);
			} catch (IOException e) {
				success = false;
				logger.warn("Status could not be retrieved.", e);
				emb = generateEmbedBuilder("Status:", "Status could not be fetched.", null, null, null, Color.RED);
			}
			message.reply("", emb);
			return success;
		}

		@Override
		public String getDescription() {
			return "Fetches and returns information about the Minecraft server's status.";
		}

		@Override
		public String getUsage() {
			return COMMAND_PREFIX + "status";
		}
	},
	UPTIME {
		@Override
		public boolean execute(Message message, Bot bot) {
			long hours = ChronoUnit.HOURS.between(bot.getStartTime().toInstant(), Calendar.getInstance().toInstant());
			long minutes = ChronoUnit.MINUTES.between(bot.getStartTime().toInstant(),
					Calendar.getInstance().toInstant()) % 60;
			long seconds = ChronoUnit.SECONDS.between(bot.getStartTime().toInstant(),
					Calendar.getInstance().toInstant()) % 60;
			StringBuilder sb = new StringBuilder();
			if (hours != 0) {
				sb.append(hours);
				sb.append(" hours, ");
				sb.append(minutes);
				sb.append(" minutes, and ");
				sb.append(seconds);
				sb.append(" seconds.");
			} else if (minutes != 0) {
				sb.append(minutes);
				sb.append(" minutes and ");
				sb.append(seconds);
				sb.append(" seconds.");
			} else {
				sb.append(seconds);
				sb.append(" seconds.");
			}
			EmbedBuilder emb = generateEmbedBuilder("Uptime:", sb.toString(), null, null, null, Color.GREEN);
			message.reply("", emb);
			return true;
		}

		@Override
		public String getDescription() {
			return "Returns the uptime of this bot.";
		}

		@Override
		public String getUsage() {
			return COMMAND_PREFIX + "uptime";
		}
	},
	VERSION {
		@Override
		public boolean execute(Message message, Bot bot) {
			EmbedBuilder emb = generateEmbedBuilder("Version:", Bot.class.getPackage().getImplementationVersion(), null, null, null, Color.GREEN);
			message.reply("", emb);
			return true;
		}

		@Override
		public String getDescription() {
			return "Returns the version of this bot.";
		}

		@Override
		public String getUsage() {
			return COMMAND_PREFIX + "version";
		}
	},
	UNKNOWN {
		@Override
		public boolean execute(Message message, Bot bot) {
			EmbedBuilder emb = generateEmbedBuilder("Unknown command:", "Use " + COMMAND_PREFIX + "help for help.",
					null, null, null, Color.GREEN);
			message.reply("", emb);
			return true;
		}

		@Override
		public String getDescription() {
			return "Unknown command.";
		}

		@Override
		public String getUsage() {
			return "No usage available.";
		}
	};

	private static final Logger logger = LoggerFactory.getLogger(Command.class);

	private static final String COMMAND_PREFIX = "!";

	public abstract boolean execute(Message message, Bot bot);

	public String getDisplayName() {
		String name = name();
		char[] nameArray = name.toLowerCase().toCharArray();
		nameArray[0] = name.charAt(0);
		return new String(nameArray);
	}

	public abstract String getDescription();

	public abstract String getUsage();

	public static Command getCommandFromMessage(String content) {
		String command;
		if (!content.startsWith(COMMAND_PREFIX)) {
			return null;
		}
		int spaceIndex = content.indexOf(' ');
		if (spaceIndex == -1) {
			command = content.substring(1);
		} else {
			command = content.substring(1, spaceIndex);
		}
		Command toReturn = Command.valueOf(command.toUpperCase());
		if (toReturn == null) {
			toReturn = UNKNOWN;
		}
		return toReturn;
	}

	public static String[] getArguments(Message message) {
		String[] orig = message.getContent().split(" ");
		if (orig.length == 1) {
			return new String[0];
		}
		return Arrays.copyOfRange(orig, 1, orig.length);
	}

}
