package com.howlstudio.playerreport;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
import java.util.stream.Collectors;
public class ReportManager {
    private final Path dataDir;
    private final List<Report> reports=new ArrayList<>();
    public ReportManager(Path d){this.dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}load();}
    public long getPendingCount(){return reports.stream().filter(r->!r.isResolved()).count();}
    public void addReport(Report r){reports.add(r);save();
        // Notify all staff/online players with staff flag
        for(PlayerRef p:Universe.get().getPlayers())
            p.sendMessage(Message.raw("§c[Report] §e"+r.getReporter()+"§r reported §e"+r.getReported()+"§r: "+r.getReason()+" (ID: #"+r.getId()+")"));
    }
    public void save(){try{StringBuilder sb=new StringBuilder();for(Report r:reports)sb.append(r.toConfig()).append("\n");Files.writeString(dataDir.resolve("reports.txt"),sb.toString());}catch(Exception e){}}
    private void load(){try{Path f=dataDir.resolve("reports.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){Report r=Report.fromConfig(l);if(r!=null)reports.add(r);}}catch(Exception e){}}
    public AbstractPlayerCommand getReportCommand(){
        return new AbstractPlayerCommand("report","Report a player. /report <player> <reason>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                if(args.length<2){playerRef.sendMessage(Message.raw("Usage: /report <player> <reason>"));return;}
                // Rate limit: 3 reports per player
                long myReports=reports.stream().filter(r->r.getReporter().equalsIgnoreCase(playerRef.getUsername())&&!r.isResolved()).count();
                if(myReports>=3){playerRef.sendMessage(Message.raw("[Report] You have too many open reports. Wait for admins to review them."));return;}
                Report r=new Report(playerRef.getUsername(),args[0],args[1]);
                addReport(r);playerRef.sendMessage(Message.raw("[Report] Submitted #"+r.getId()+". Admins have been notified."));
            }
        };
    }
    public AbstractPlayerCommand getReportsCommand(){
        return new AbstractPlayerCommand("reports","[Admin] View and manage player reports. /reports [list|resolve <id>|clear]"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String[]args=ctx.getInputString().trim().split("\\s+",2);
                String sub=args.length>0?args[0].toLowerCase():"list";
                switch(sub){
                    case"list",""->{List<Report> pending=reports.stream().filter(r->!r.isResolved()).collect(Collectors.toList());if(pending.isEmpty()){playerRef.sendMessage(Message.raw("[Reports] No pending reports."));break;}playerRef.sendMessage(Message.raw("[Reports] Pending ("+pending.size()+"):"));for(Report r:pending)playerRef.sendMessage(Message.raw("  #"+r.getId()+" ["+r.getTime()+"] §e"+r.getReporter()+"§r → §c"+r.getReported()+"§r: "+r.getReason()));}
                    case"resolve"->{if(args.length<2)break;try{int id=Integer.parseInt(args[1]);reports.stream().filter(r->r.getId()==id).findFirst().ifPresentOrElse(r->{r.resolve();save();playerRef.sendMessage(Message.raw("[Reports] Resolved #"+id));},()->playerRef.sendMessage(Message.raw("[Reports] Not found: #"+id)));}catch(Exception e){playerRef.sendMessage(Message.raw("Usage: /reports resolve <id>"));}}
                    case"clear"->{reports.removeIf(Report::isResolved);save();playerRef.sendMessage(Message.raw("[Reports] Cleared resolved reports."));}
                    default->playerRef.sendMessage(Message.raw("Usage: /reports list | resolve <id> | clear"));
                }
            }
        };
    }
}
