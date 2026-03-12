package com.howlstudio.playerreport;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
/** PlayerReport — Players report rule violations. Admins review queue with /reports. Persistent log. */
public final class PlayerReportPlugin extends JavaPlugin {
    private ReportManager mgr;
    public PlayerReportPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        System.out.println("[Reports] Loading...");
        mgr=new ReportManager(getDataDirectory());
        CommandManager cmd=CommandManager.get();
        cmd.register(mgr.getReportCommand());
        cmd.register(mgr.getReportsCommand());
        System.out.println("[Reports] Ready. "+mgr.getPendingCount()+" pending reports.");
    }
    @Override protected void shutdown(){if(mgr!=null)mgr.save();System.out.println("[Reports] Stopped.");}
}
