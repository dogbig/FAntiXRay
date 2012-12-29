/*
 * Copyright (C) 2011-2012 FurmigaHumana.  All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.FurH.FAntiXRay.hook;

import net.minecraft.server.v1_4_6.EntityPlayer;
import net.minecraft.server.v1_4_6.INetworkManager;
import net.minecraft.server.v1_4_6.MinecraftServer;
import net.minecraft.server.v1_4_6.Packet;
import net.minecraft.server.v1_4_6.Packet0KeepAlive;
import net.minecraft.server.v1_4_6.Packet101CloseWindow;
import net.minecraft.server.v1_4_6.Packet102WindowClick;
import net.minecraft.server.v1_4_6.Packet106Transaction;
import net.minecraft.server.v1_4_6.Packet107SetCreativeSlot;
import net.minecraft.server.v1_4_6.Packet108ButtonClick;
import net.minecraft.server.v1_4_6.Packet10Flying;
import net.minecraft.server.v1_4_6.Packet14BlockDig;
import net.minecraft.server.v1_4_6.Packet15Place;
import net.minecraft.server.v1_4_6.Packet16BlockItemSwitch;
import net.minecraft.server.v1_4_6.Packet18ArmAnimation;
import net.minecraft.server.v1_4_6.Packet19EntityAction;
import net.minecraft.server.v1_4_6.Packet202Abilities;
import net.minecraft.server.v1_4_6.Packet203TabComplete;
import net.minecraft.server.v1_4_6.Packet204LocaleAndViewDistance;
import net.minecraft.server.v1_4_6.Packet205ClientCommand;
import net.minecraft.server.v1_4_6.Packet250CustomPayload;
import net.minecraft.server.v1_4_6.Packet255KickDisconnect;
import net.minecraft.server.v1_4_6.Packet3Chat;
import net.minecraft.server.v1_4_6.Packet7UseEntity;
import net.minecraft.server.v1_4_6.Packet9Respawn;
import net.minecraft.server.v1_4_6.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerConnection extends PlayerConnection {
    public PlayerConnection playerConnection;
    public EntityPlayer player;
    
    public FPlayerConnection(MinecraftServer minecraftserver, INetworkManager inetworkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, inetworkmanager, entityplayer);
        this.player = entityplayer;
        this.playerConnection = new PlayerConnection(minecraftserver, inetworkmanager, entityplayer);
    }

    @Override
    public CraftPlayer getPlayer() {
        return this.playerConnection.getPlayer();
    }

    @Override
    public void d() {
        this.playerConnection.d();
    }

    @Override
    public void disconnect(String s) {
        this.playerConnection.disconnect(s);
    }

    @Override
    public void a(Packet10Flying packet10flying) {
        this.playerConnection.a(packet10flying);
    }

    @Override
    public void a(double d0, double d1, double d2, float f, float f1) {
        this.playerConnection.a(d0, d1, d2, f, f1);
    }

    @Override
    public void teleport(Location dest) {
        this.playerConnection.teleport(dest);
    }

    @Override
    public void a(Packet14BlockDig packet14blockdig) {
        this.playerConnection.a(packet14blockdig);
    }

    @Override
    public void a(Packet15Place packet15place) {
        this.playerConnection.a(packet15place);
    }

    @Override
    public void a(String s, Object[] aobject) {
        this.playerConnection.a(s, aobject);
    }

    @Override
    public void onUnhandledPacket(Packet packet) {
        this.playerConnection.onUnhandledPacket(packet);
    }

    @Override
    public void sendPacket(Packet packet) {
        this.playerConnection.sendPacket(packet);
    }

    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch) {
        this.playerConnection.a(packet16blockitemswitch);
    }

    @Override
    public void a(Packet3Chat packet3chat) {
        this.playerConnection.a(packet3chat);
    }

    @Override
    public void chat(String s, boolean async) {
        this.playerConnection.chat(s, async);
    }

    @Override
    public void a(Packet18ArmAnimation packet18armanimation) {
        this.playerConnection.a(packet18armanimation);
    }

    @Override
    public void a(Packet19EntityAction packet19entityaction) {
        this.playerConnection.a(packet19entityaction);
    }

    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect) {
        this.playerConnection.a(packet255kickdisconnect);
    }

    @Override
    public int lowPriorityCount() {
        return this.playerConnection.lowPriorityCount();
    }

    @Override
    public void a(Packet7UseEntity packet7useentity) {
        this.playerConnection.a(packet7useentity);
    }

    @Override
    public void a(Packet205ClientCommand packet205clientcommand) {
        this.playerConnection.a(packet205clientcommand);
    }

    @Override
    public boolean b() {
        return this.playerConnection.b();
    }

    @Override
    public void a(Packet9Respawn packet9respawn) {
        this.playerConnection.a(packet9respawn);
    }

    @Override
    public void handleContainerClose(Packet101CloseWindow packet101closewindow) {
        this.playerConnection.handleContainerClose(packet101closewindow);
    }

    @Override
    public void a(Packet102WindowClick packet102windowclick) {
        this.playerConnection.a(packet102windowclick);
    }

    @Override
    public void a(Packet108ButtonClick packet108buttonclick) {
        this.playerConnection.a(packet108buttonclick);
    }

    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot) {
        this.playerConnection.a(packet107setcreativeslot);
    }

    @Override
    public void a(Packet106Transaction packet106transaction) {
        this.playerConnection.a(packet106transaction);
    }

    /*@Override
    public void a(Packet130UpdateSign packet130updatesign) {
        this.playerConnection.a(packet130updatesign);
    }*/

    @Override
    public void a(Packet0KeepAlive packet0keepalive) {
        this.playerConnection.a(packet0keepalive);
    }

    @Override
    public boolean a() {
        return this.playerConnection.a();
    }

    @Override
    public void a(Packet202Abilities packet202abilities) {
        this.playerConnection.a(packet202abilities);
    }

    @Override
    public void a(Packet203TabComplete packet203tabcomplete) {
        this.playerConnection.a(packet203tabcomplete);
    }

    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance) {
        this.playerConnection.a(packet204localeandviewdistance);
    }

    @Override
    public void a(Packet250CustomPayload packet250custompayload) {
        this.playerConnection.a(packet250custompayload);
    }
}