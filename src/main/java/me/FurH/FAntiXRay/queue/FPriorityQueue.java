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

import java.util.ArrayList;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import net.minecraft.server.v1_5_R2.EntityPlayer;
import net.minecraft.server.v1_5_R2.Packet;
import net.minecraft.server.v1_5_R2.Packet51MapChunk;
import net.minecraft.server.v1_5_R2.Packet56MapChunkBulk;

public class FPriorityQueue extends ArrayList<Packet> {
    private static final long serialVersionUID = 1546534712446462L;
    private EntityPlayer player;
    
    public FPriorityQueue(EntityPlayer player) {
        this.player = player;
    }
    
    @Override
    public Packet remove(int index) {
        
        Packet packet = super.remove(index);
        if (packet != null) {
            if (packet instanceof Packet56MapChunkBulk) {
                Packet56MapChunkBulk p56 = (Packet56MapChunkBulk)packet;
                packet = FObfuscator.obfuscate(player, p56);
            } else
            if (packet instanceof Packet51MapChunk) {
                Packet51MapChunk p51 = (Packet51MapChunk)packet;
                packet = FObfuscator.obfuscate(player, p51);
            }
        }

        return packet;
    }
}