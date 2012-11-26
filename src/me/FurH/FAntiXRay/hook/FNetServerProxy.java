package me.FurH.FAntiXRay.hook;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet0KeepAlive;
import net.minecraft.server.Packet101CloseWindow;
import net.minecraft.server.Packet102WindowClick;
import net.minecraft.server.Packet106Transaction;
import net.minecraft.server.Packet107SetCreativeSlot;
import net.minecraft.server.Packet108ButtonClick;
import net.minecraft.server.Packet10Flying;
import net.minecraft.server.Packet14BlockDig;
import net.minecraft.server.Packet15Place;
import net.minecraft.server.Packet16BlockItemSwitch;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.Packet19EntityAction;
import net.minecraft.server.Packet202Abilities;
import net.minecraft.server.Packet203TabComplete;
import net.minecraft.server.Packet204LocaleAndViewDistance;
import net.minecraft.server.Packet205ClientCommand;
import net.minecraft.server.Packet250CustomPayload;
import net.minecraft.server.Packet255KickDisconnect;
import net.minecraft.server.Packet3Chat;
import net.minecraft.server.Packet7UseEntity;
import net.minecraft.server.Packet9Respawn;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;

/**
 *
 * @author FurmigaHumana
 */
public class FNetServerProxy extends NetServerHandler {
    public INetworkManager networkManager;
    public NetServerHandler netServerHandler;
    
    public FNetServerProxy(MinecraftServer minecraftserver, NetServerHandler instance) {
        super(minecraftserver, instance.networkManager, instance.player);
        this.netServerHandler = instance;
        networkManager = netServerHandler.networkManager;
    }

    public FNetServerProxy(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
        this.netServerHandler = new NetServerHandler(minecraftserver, networkmanager, entityplayer);
        networkManager = netServerHandler.networkManager;
    }

    @Override
    public CraftPlayer getPlayer() {
        return this.netServerHandler.getPlayer();
    }
    
    @Override
    public void d() {
        this.netServerHandler.d();
    }
    
    @Override
    public void disconnect(String s) {
        this.netServerHandler.disconnect(s);
    }
    
    @Override
    public void a(Packet10Flying packet10flying) {
        this.netServerHandler.a(packet10flying);
    }
    
    @Override
    public void a(double d0, double d1, double d2, float f, float f1) {
        this.netServerHandler.a(d0, d1, d2, f, f1);
    }
    
    @Override
    public void teleport(Location dest) {
        this.netServerHandler.teleport(dest);
    }
    
    @Override
    public void a(Packet14BlockDig packet14blockdig) {
        this.netServerHandler.a(packet14blockdig);
    }
    
    @Override
    public void a(Packet15Place packet15place) {
        this.netServerHandler.a(packet15place);
    }
    
    @Override
    public void a(String s, Object[] aobject) {
        this.netServerHandler.a(s, aobject);
    }
    
    @Override
    public void onUnhandledPacket(Packet packet) {
        this.netServerHandler.onUnhandledPacket(packet);
    }
    
    @Override
    public void sendPacket(Packet packet) {
        this.netServerHandler.sendPacket(packet);
    }
    
    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch) {
        this.netServerHandler.a(packet16blockitemswitch);
    }
    
    @Override
    public void a(Packet3Chat packet3chat) {
        this.netServerHandler.a(packet3chat);
    }
    
    @Override
    public void chat(String s, boolean async) {
        this.netServerHandler.chat(s, async);
    }
    
    @Override
    public void a(Packet18ArmAnimation packet18armanimation) {
        this.netServerHandler.a(packet18armanimation);
    }
    
    @Override
    public void a(Packet19EntityAction packet19entityaction) {
        this.netServerHandler.a(packet19entityaction);
    }
    
    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect) {
        this.netServerHandler.a(packet255kickdisconnect);
    }
    
    @Override
    public int lowPriorityCount() {
        return this.netServerHandler.lowPriorityCount();
    }
    
    @Override
    public void a(Packet7UseEntity packet7useentity) {
        this.netServerHandler.a(packet7useentity);
    }
    
    @Override
    public void a(Packet205ClientCommand packet205clientcommand) {
        this.netServerHandler.a(packet205clientcommand);
    }
    
    @Override
    public boolean b() {
        return this.netServerHandler.b();
    }
    
    @Override
    public void a(Packet9Respawn packet9respawn) {
        this.netServerHandler.a(packet9respawn);
    }
    
    @Override
    public void handleContainerClose(Packet101CloseWindow packet101closewindow) {
        this.netServerHandler.handleContainerClose(packet101closewindow);
    }
    
    @Override
    public void a(Packet102WindowClick packet102windowclick) {
        this.netServerHandler.a(packet102windowclick);
    }
    
    @Override
    public void a(Packet108ButtonClick packet108buttonclick) {
        this.netServerHandler.a(packet108buttonclick);
    }
    
    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot) {
        this.netServerHandler.a(packet107setcreativeslot);
    }
    
    @Override
    public void a(Packet106Transaction packet106transaction) {
        this.netServerHandler.a(packet106transaction);
    }
    
    /*@Override
    public void a(Packet130UpdateSign packet130updatesign) {
        this.netServerHandler.a(packet130updatesign);
    }*/
    
    @Override
    public void a(Packet0KeepAlive packet0keepalive) {
        this.netServerHandler.a(packet0keepalive);
    }
    
    @Override
    public boolean a() {
        return this.netServerHandler.a();
    }
    
    @Override
    public void a(Packet202Abilities packet202abilities) {
        this.netServerHandler.a(packet202abilities);
    }
    
    @Override
    public void a(Packet203TabComplete packet203tabcomplete) {
        this.netServerHandler.a(packet203tabcomplete);
    }
    
    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance) {
        this.netServerHandler.a(packet204localeandviewdistance);
    }
    
    @Override
    public void a(Packet250CustomPayload packet250custompayload) {
        this.netServerHandler.a(packet250custompayload);
    }
}