package org.beqz.butter;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

/**
 * Created by Jonathan on 19/05/2014.
 */
public class PinPrompt extends StringPrompt
{
    @Override
    public String getPromptText(ConversationContext conversationContext)
    {
        return "Enter twitter pin";
    }

    @Override
    public Prompt acceptInput(ConversationContext conversationContext, String s)
    {
        conversationContext.setSessionData("pin", s);
        return Prompt.END_OF_CONVERSATION;
    }
}
