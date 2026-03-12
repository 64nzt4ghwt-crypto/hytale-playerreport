package com.howlstudio.playerreport;
import java.time.*; import java.time.format.DateTimeFormatter;
public class Report {
    private static int nextId=1;
    private final int id;
    private final String reporter, reported, reason;
    private final long timestamp;
    private boolean resolved;
    public Report(String reporter,String reported,String reason){this.id=nextId++;this.reporter=reporter;this.reported=reported;this.reason=reason;this.timestamp=System.currentTimeMillis();this.resolved=false;}
    public int getId(){return id;} public String getReporter(){return reporter;} public String getReported(){return reported;}
    public String getReason(){return reason;} public boolean isResolved(){return resolved;} public void resolve(){resolved=true;}
    public String getTime(){return Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));}
    public String toConfig(){return id+"|"+reporter+"|"+reported+"|"+reason+"|"+timestamp+"|"+(resolved?"1":"0");}
    public static Report fromConfig(String s){String[]p=s.split("\\|",6);if(p.length<6)return null;Report r=new Report(p[1],p[2],p[3]);if("1".equals(p[5]))r.resolve();return r;}
}
