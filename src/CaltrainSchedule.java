import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CaltrainSchedule extends MIDlet implements CommandListener {
    private Display display;
    private Form props;
    private StringBuffer propbuf;
    private Command exitCommand = new Command ("Exit", Command.EXIT, 1);
    private boolean firstTime;

    public CaltrainSchedule () {
        display = Display.getDisplay (this);
        firstTime = true;
        props = new Form ("Caltrain Schedule");
    }

    public void startApp () {
        Runtime runtime = Runtime.getRuntime ();
        runtime.gc ();

        long free = runtime.freeMemory ();

        if (firstTime) {
            long total = runtime.totalMemory ();

            propbuf = new StringBuffer (50);

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("US/Pacific"));
            Date currentDate = calendar.getTime();
            props.append(currentDate + "");

            props.append("Free Memory = " + free);
            props.append("Total Memory = " + total);

            props.append(showProp ("microedition.platform"));
            props.append(showProp ("microedition.locale"));
            props.append(showProp ("microedition.encoding"));

            props.addCommand (exitCommand);
            props.setCommandListener (this);
            display.setCurrent (props);
            firstTime = false;
        }
        else {
            props.set (0, new StringItem ("", "Free Memory = " + free + "\n"));
        }

        display.setCurrent (props);
    }

    public void commandAction (Command c, Displayable s) {
        if (c == exitCommand) {
            destroyApp (false);
            notifyDestroyed ();
        }
    }

    String showProp (String prop) {
        String value = System.getProperty (prop);
        propbuf.setLength (0);
        propbuf.append (prop);
        propbuf.append (" = ");

        if (value == null) {
            propbuf.append ("<undefined>");
        } else {
            propbuf.append ("\"");
            propbuf.append (value);
            propbuf.append ("\"");
        }
        return propbuf.toString ();
    }

    public void pauseApp () {
    }

    public void destroyApp (boolean unconditional) {
    }
}
