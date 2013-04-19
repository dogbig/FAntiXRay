/*
 * Copyright (C) 2011-2013 FurmigaHumana.  All rights reserved.
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

package me.FurH.FAntiXRay.queue;

import java.util.concurrent.ConcurrentLinkedQueue;
import me.FurH.FAntiXRay.update.FBlockUpdate;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.Packet;
import net.minecraft.server.v1_5_R2.Packet14BlockDig;
import net.minecraft.server.v1_5_R2.Packet15Place;

/**
 *
 * @author FurmigaHumana
 */
public class FPacketQueue extends ConcurrentLinkedQueue<Packet> {
    private static final long serialVersionUID = -15478984462251L;
    private EntityPlayer player;
    
    public FPacketQueue(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public boolean add(Packet packet) {

        if (packet instanceof Packet15Place) {
            FBlockUpdate.update(player, (Packet15Place)packet);
        } else
        if (packet instanceof Packet14BlockDig) {
            FBlockUpdate.update(player, (Packet14BlockDig)packet);
        }

        return super.add(packet);
    }
}