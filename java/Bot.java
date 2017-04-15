import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Bot {

	/**
	 * Your password (PASS) should be an OAuth token authorized through our API with the chat_login scope.
	 * The token must have the prefix oauth:. For example, if your token is abcd, you send oauth:abcd.
	 * To quickly get a token for your account, use this Twitch Chat OAuth Password Generator.
	 */
	private final String OAUTH;

	/**
	 * Your nickname (NICK) must be your Twitch user name in lowercase.
	 */
	private final String NICK;

	/**
	 * All references to channel and user names are actual names (not IDs, as in the API endpoints).
	 * Always enter the channel name in lowercase.
	 */
	private final String CHANNEL;

	private Socket socket;
	private BufferedWriter writer;
	private BufferedReader reader;

	private List<String> commands = new ArrayList<>();

	public Bot(String channel, String nick, String oauth) {
		this.CHANNEL = channel;
		this.NICK = nick;
		this.OAUTH = oauth;
		try {
			this.socket = new Socket("irc.chat.twitch.tv", 6667);
			this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.commands.add("!hi");
		this.commands.add("!slap");
	}

	public void connect() throws IOException {
		//Login
		sendServerMessage("PASS", OAUTH);
		sendServerMessage("NICK", NICK);

		//Request Capabilities
		sendServerMessage("CAP REQ", ":twitch.tv/membership");
		sendServerMessage("CAP REQ", ":twitch.tv/tags");
		sendServerMessage("CAP REQ", ":twitch.tv/commands");

		//Wait until we are connected
		String line;
		//The server sends 004 when you are successfully registered
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			if (line.contains("004")) break;
		}

		joinChannel(CHANNEL);

		//Now we just have to listen and respond!
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			if (line.toLowerCase().startsWith("PING ")) {
				//We MUST respond to this whenever this is sent or else you will be disconnected
				sendServerMessage("PONG", line.substring(5));
			} else {
				if (line.contains("PRIVMSG"))
					parseMessage(line);
			}
		}
	}

	/**
	 * A Twitch message contains a LOT of valuable information.
	 * What we can do is separate that information so we can easily access what we need.
	 * A raw Twitch message w/o bits looks like this:
	 * @badges=global_mod/1,turbo/1;color=#0D4200;display-name=dallas;emotes=25:0-4,12-16/1902:6-10;mod=0;room-id=1337;subscriber=0;turbo=1;user-id=1337;user-type=global_mod :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #dallas :Kappa Keepo Kappa
	 * A raw Twitch message w/ bits looks like this:
	 * @badges=staff/1,bits/1000;bits=100;color=;display-name=dallas;emotes=;id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;mod=0;room-id=1337;subscriber=0;turbo=1;user-id=1337;user-type=staff :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #dallas :cheer100
	 */
	private void parseMessage(String line) throws IOException {
		//First we split it into the major chunks 0: tags 1: user 2: command 3: channel 4+: message
		String[] parts = line.split(" ");
		//Lets put the message back together so its easier to parse
		StringBuilder messageBuilder = new StringBuilder();
		for (int c = 4; c < parts.length; c++) {
			messageBuilder.append(" ").append(parts[c]);
		}
		String message = messageBuilder.toString().substring(2);
		String tags = parts[0];
		String channel = parts[3];
		String user = tags.split(";")[2].substring(13);

		//Now lets have some dummy commands here which can easily be moved elsewhere
		if (message.startsWith("!hi")) sendMessage(channel, "Hi! " + user);
		if (message.startsWith("!slap")) sendMessage(channel, String.format("/me slaps %s with a big trout!", user));
	}

	/**
	 * Remember channels must start with # and be lowercase
	 * ex. #twitch
	 *
	 * @param channel
	 */
	private void joinChannel(String channel) throws IOException {
		sendServerMessage("JOIN", channel);
	}

	/**
	 * Remember channels must start with # and be lowercase
	 * ex. #twitch
	 *
	 * @param channel
	 */
	private void leaveChannel(String channel) throws IOException {
		sendServerMessage("PART", channel);
	}

	/**
	 * You can use this to send IRC Commands to TMI (Twitch Message Interface
	 *
	 * @param command
	 * @param message
	 */
	private void sendServerMessage(String command, String message) throws IOException {
		writer.write(String.format("%s %s\r\n", command, message));
		writer.flush();
	}

	private void sendMessage(String channnel, String message) throws IOException {
		sendServerMessage("PRIVMSG", channnel + " :" + message);
	}
}