// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LogFormatter.java

package org.jini.glyph.chalice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.*;

public class LogFormatter extends Formatter
{

    public LogFormatter()
    {
        lineSep = System.getProperty("line.separator");
        format = "{0} [{1,date,dd/MM/yy} {1,time,HH:mm:ss}] ({2}) {3}";
    }

    public String format(LogRecord logRecord)
    {
        int startIndex = logRecord.getSourceClassName().lastIndexOf(".") + 1;
        int endIndex = logRecord.getSourceClassName().length();
        String source = logRecord.getSourceClassName().substring(startIndex, endIndex);
        StringBuffer level;
        for(level = new StringBuffer(logRecord.getLevel().getName()); "warning".length() > level.length(); level.append(" "));
        Object args[] = {
            level.toString(), new Date(logRecord.getMillis()), source, logRecord.getMessage()
        };
        StringBuffer out = new StringBuffer(MessageFormat.format(format, args));
        if(logRecord.getThrown() != null)
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                logRecord.getThrown().printStackTrace(pw);
                out.append(lineSep);
                out.append("\t");
                out.append(sw.toString());
            }
            catch(Exception exception) { }
        out.append(lineSep);
        return out.toString();
    }

    private String lineSep;
    private String format;
}
