class Log2XmppClient
{
    public static void main(String args[])
    {
        if( args.length < 3 )
        {
          System.out.println( "Usage: Log2XmppClient <someone@gmail.com> <password> <test.log>" );
          System.exit( 0 );
        }        
        Log2Xmpp log = new Log2Xmpp(args[0], args[1], args[2]);
        log.connect();
        log.watch();
    }
}
