import java.io.*;
import java.net.*;
import java.util.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;

/**
 * Send new lines of a file to a GTalk account. This class setup a log watch on 
 * a file, any new lines in that file will be send to all the contacts attached to the
 * Google Talk account.
 *
 * In additional, contacts can send commands which will be execute on the server
 * and stdout is returned. See AdminCommandMessageListener.
 *
 * @author Wei Zhuo <weizhuo@gmail.com>
 */
class Log2Xmpp implements LogFileTailListener
{
    private XMPPConnection connection;
    private String account, password, filename, hostname;
    private AdminCommandMessageListener adminCommands;

    private int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

    public Log2Xmpp(String account, String password, String filename)
    {
        this.account = account;
        this.password = password;
        this.filename = filename;
    }

    /**
     * Connect to GTalk server and print a list of all contacts.
     */
    public void connect()
    {
        this.hostname = getHostName();
        ConnectionConfiguration cc = new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
        this.connection = new XMPPConnection(cc);
        if(this.adminCommands==null)
            this.adminCommands = new AdminCommandMessageListener(this.connection, this.hostname);
        try
        {
            connection.connect();
            SASLAuthentication.supportSASLMechanism("PLAIN", 0);
            this.connection.login(this.account, this.password, "resource");
            if(connection.isAuthenticated())
                System.out.println("Connected!");
            Roster roster = connection.getRoster();
            System.out.println("Numer of contacts: " + roster.getEntryCount());
            for(RosterEntry entry: roster.getEntries()) {
                System.out.println("User: " + entry.getUser());
            }
            getChatUsers();
        }
        catch(XMPPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start watching for file changes, runs LogFileTailer thread.
     */
    public void watch()
    {
        LogFileTailer log = new LogFileTailer(new File(this.filename));
        log.addListener(this);
        log.start();
    }

    /**
     * Get all contacts as Chat objects.
     */
    protected Collection<Chat> getChatUsers()
    {
        Collection<Chat> users = new LinkedList<Chat>();
        ChatManager chatmanager = this.connection.getChatManager();
        this.connection.getRoster().reload();
        for(RosterEntry entry: this.connection.getRoster().getEntries())
        {
            Chat chat = chatmanager.createChat(entry.getUser(), this.adminCommands);
            users.add(chat);
        }
        return users;
    }

    /**
     * Reconnect every hour.
     */
    protected void reconnect()
    {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (this.currentHour != hour)
        {
            this.connection.disconnect();
            this.currentHour = hour;
            this.connect();
        }
    }

    protected void sendMessage(String msg)
    {
        this.reconnect();
        for(Chat chat : getChatUsers())
        {
            try {
                chat.sendMessage("["+hostname+"]: "+msg);
                Thread.sleep(1000);
            }
            catch(XMPPException e) {
                e.printStackTrace();
            }
            catch(InterruptedException e) {
            }
        }
    }

    protected String getHostName()
    {
        try { return InetAddress.getLocalHost().getHostName(); }
        catch (UnknownHostException e) { }
        return "localhost";
    }

    public void newLogFileLine(String line)
    {
        if(this.connection == null || !this.connection.isConnected())
            connect();
        sendMessage(line);
    }
}
