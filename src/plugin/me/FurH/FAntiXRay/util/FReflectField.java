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

package me.FurH.FAntiXRay.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 * @author FurmigaHumana
 */
public class FReflectField {
    
    /* thanks to polygenelubricants */
    public static void setFinalField(Object obj, String x, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(x);
            field.setAccessible(true);
            
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(obj, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    /* get a private field */
    public static Object getPrivateField(Object obj, String x) {
        try {
            Field f = obj.getClass().getDeclaredField(x);
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    /* set a private field */
    public static void setPrivateField(Object object, String fieldName, Object value) {    
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
}
