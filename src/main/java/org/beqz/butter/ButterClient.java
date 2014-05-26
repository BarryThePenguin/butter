package org.beqz.butter;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Jonathan on 18/05/2014.
 */
public class ButterClient implements ConversationAbandonedListener
{
    protected Butter plugin;

    private static TwitterFactory twitterFactory;
    private ConversationFactory conversationFactory;

    private RequestToken requestToken;
    private AccessToken accessToken;

    private static final String ConsumerKey = "mHJUhFCJSR9wicu5ps3IMHVu5";
    private static final String ConsumerSecret = "SCdcyXkdEjR0lqMMAAQzEUe8PXXMIqEkLjaOrW7FIPTRK3maxi";

    public ButterClient(Butter plugin)
    {
        this.plugin = plugin;

        ConfigurationSection twitterConfig = plugin.getConfig().getConfigurationSection("twitter");
        String accessToken = twitterConfig.getString("token");
        String accessTokenSecret = twitterConfig.getString("secret");

        conversationFactory = new ConversationFactory(plugin)
                .withFirstPrompt(new PinPrompt())
                .addConversationAbandonedListener(this);

        if (accessToken.equalsIgnoreCase("accessToken") || accessTokenSecret.equalsIgnoreCase("accessTokenSecret"))
        {
            plugin.getLogger().warning("*******************************************");
            plugin.getLogger().warning("* Butter Twitter is not configured  *");
            plugin.getLogger().warning("*******************************************");

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true).setOAuthConsumerKey(ConsumerKey).setOAuthConsumerSecret(ConsumerSecret);
            twitterFactory = new TwitterFactory(cb.build());
        }
        else
        {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true).setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret).setOAuthConsumerKey(ConsumerKey).setOAuthConsumerSecret(ConsumerSecret);
            twitterFactory = new TwitterFactory(cb.build());
        }
    }

    public void TweetPlayerJoin(Player player)
    {
        try
        {
            String playerName = player.getName();
            Map<String, Object> userHandleMap = plugin.getConfig().getConfigurationSection("playerHandles").getValues(false);
            if (userHandleMap.containsKey(player.getUniqueId().toString()))
                playerName = userHandleMap.get(player.getUniqueId().toString()).toString();

            Twitter twitter = twitterFactory.getInstance();
            Status status = twitter.updateStatus(String.format("%s joined the server", playerName));
        }
        catch (TwitterException e)
        {
            plugin.getLogger().severe("Butter: " + e.getMessage());
        }
    }

    public boolean Authenticate(CommandSender sender)
    {
        try
        {
            Twitter twitter = twitterFactory.getInstance();

            try
            {
                // get request token.
                // this will throw IllegalStateException if access token is already available
                requestToken = twitter.getOAuthRequestToken();

                sender.sendMessage("Open the following URL and grant access to Butter:");
                sender.sendMessage(requestToken.getAuthorizationURL());

                conversationFactory.buildConversation((Conversable)sender).begin();
            }
            catch (IllegalStateException ie)
            {
                // access token is already available, or consumer key/secret is not set.
                if (!twitter.getAuthorization().isEnabled())
                {
                    sender.sendMessage("OAuth consumer key/secret is not set.");
                    return false;
                }

                sender.sendMessage("Client is already registered");
                return true;
            }
        }
        catch (TwitterException te)
        {
            sender.sendMessage("Failed to get access: " + te.getMessage());
            sender.sendMessage("Try revoking access to the ServerEvents application from your Butter settings page.");
            return false;
        }

        return true;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent conversationAbandonedEvent)
    {
        Twitter twitter = twitterFactory.getInstance();
        String token;
        String secret;

        String pin = conversationAbandonedEvent.getContext().getSessionData("pin").toString();

        if(conversationAbandonedEvent.gracefulExit())
        {
            try
            {
                accessToken = twitter.getOAuthAccessToken(requestToken, pin);

                plugin.getLogger().info("Successfully connected to Twitter.");

                token = accessToken.getToken();
                secret = accessToken.getTokenSecret();

                ConfigurationSection twitterConfig = plugin.getConfig().getConfigurationSection("twitter");
                twitterConfig.set("token", token);
                twitterConfig.set("secret", secret);

                plugin.saveConfig();

                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true).setOAuthAccessToken(token).setOAuthAccessTokenSecret(secret).setOAuthConsumerKey(ConsumerKey).setOAuthConsumerSecret(ConsumerSecret);
                twitterFactory = new TwitterFactory(cb.build());
            }
            catch (TwitterException e)
            {
                if (e.getStatusCode() == 401)
                    plugin.getLogger().warning("Unable to get the access token. 401: Unauthorized.");
                else
                {
                    plugin.getLogger().warning("Unable to get the access token. Stack trace output to log.");
                    plugin.getLogger().warning(e.getMessage());
                }
            }
        }
    }
}