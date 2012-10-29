import java.io.*;
import java.util.*;

/**
 * A log file tailer is designed to monitor a log file and send notifications
 * when new lines are added to the log file. This class has a notification
 * strategy similar to a SAX parser: implement the LogFileTailerListener interface,
 * create a LogFileTailer to tail your log file, add yourself as a listener, and
 * start the LogFileTailer. It is your job to interpret the results, build meaningful
 * sets of data, etc. This tailer simply fires notifications containing new log file lines, 
 * one at a time.
 *
 * @author Wei Zhuo <weizhuo@gmail.com>
 */
class LogFileTailer extends Thread
{
    private long sampling = 2000; // 2 seconds
    private File logfile;
    private boolean tailing = false;
    private Set<LogFileTailListener> listeners = new HashSet<LogFileTailListener>();

    public LogFileTailer(File file)
    {
        this.logfile = file;
    }

    public LogFileTailer(File file, long sampling)
    {
        this.logfile = file;
        this.sampling = sampling;
    }

    public void addListener(LogFileTailListener l)
    {
        this.listeners.add(l);
    }

    public void removeListener(LogFileTailListener l)
    {
        this.listeners.remove(l);
    }

    protected void fireNewLogFileLine(String line)
    {
        for(Iterator i = this.listeners.iterator(); i.hasNext(); )
        {
            LogFileTailListener l = (LogFileTailListener)i.next();
            l.newLogFileLine(line);
        }
    }

    public void stopTailing()
    {
        this.tailing = false;
    }

    public void run()
    {
        long pos = this.logfile.length();
        try
        {
            this.tailing = true;
            RandomAccessFile file = new RandomAccessFile(this.logfile, "r");
            while(this.tailing)
            {
                try
                {
                    long length = this.logfile.length();
                    if(length < pos)
                    {
                        file = new RandomAccessFile(this.logfile, "r");
                        pos = 0;
                    }
                    if(length > pos)
                    {
                        file.seek(pos);
                        String line = file.readLine();
                        while(line != null)
                        {
                            this.fireNewLogFileLine(line);
                            line = file.readLine();
                        }
                        pos = file.getFilePointer();
                    }
                    sleep(this.sampling);
                }
                catch(Exception e)
                {
                }
            }
            file.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

interface LogFileTailListener
{
    public void newLogFileLine(String line);
}
