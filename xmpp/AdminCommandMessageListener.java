import java.io.*;
import java.util.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;

/**
 * Executes a predefined command and return result to user via chat.
 * The commands are defined in the command hash map. Each command should be
 * identified by key. Pipes and redirection are not supported.
 * 
 * @author Wei Zhuo <weizhuo@gmail.com>
 */
class AdminCommandMessageListener implements MessageListener
{
    private XMPPConnection connection;
    private String hostname;

    private HashMap<String,String> commands = new HashMap<String,String>(){{
        put("uptime",   "uptime");
        put("date",     "date");
    }};

    public AdminCommandMessageListener(XMPPConnection connection, String hostname)
    {
        this.connection = connection;
        this.hostname = hostname;
    }

    /**
     * Incoming message, check if username matches the contact list, execute
     * command if possible.
     */
    public void processMessage(Chat chat, Message message) 
    {
        if(message.getType() != Message.Type.error)
        {
            String msg = message.getBody();
            String user = chat.getParticipant();
            System.out.println("MSG: ["+user+"]: "+msg);
            if(msg != null && user != null && this.connection.getRoster().contains(user))
            {
                if(commands.containsKey(msg))
                {
                    try {
                        chat.sendMessage("["+hostname+"]: "+execute(commands.get(msg)));
                    }
                    catch(XMPPException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else            
        {
            String user = chat.getParticipant();
            XMPPError error = message.getError();
            System.out.println("ERROR: ["+user+"]: ["+error.getCode()+" "+error.getType()+ " "+error.getCondition()+"] "+error.getMessage());
        }
    }

    protected String execute(String cmd)
    {
        String s, result = "";
        try
        {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((s = stdInput.readLine()) != null) {
                result += s;
            }
            while ((s = stdError.readLine()) != null) {
                result += s;
            }
        }
        catch(IOException e) {
            result = e.getMessage();
        }
        return result;
    }
}
