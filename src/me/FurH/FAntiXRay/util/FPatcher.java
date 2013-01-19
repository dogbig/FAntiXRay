package me.FurH.FAntiXRay.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import me.FurH.FAntiXRay.FAntiXRay;

/**
 *
 * @author FurmigaHumana
 */
public class FPatcher {
    
    //Work in progress
    
    public static void load() {
        File file = new File(FAntiXRay.getPlugin().getDataFolder(), "craftbukkit.jar");
        File patch = new File(FAntiXRay.getPlugin().getDataFolder(), "classes.jar");
        File directory = new File(file.getParent(), "extracted");

        if (patch.exists()) {
            patch.delete();
        }
        
        boolean spigot = false;
        if (file.exists()) {
            try {
                extractJar(directory, file, null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            file.renameTo(new File(file, "craftbukkit_old.jar"));
            
            File test = new File(directory + "META-INF" + File.separator + "maven" + File.separator + "org.spigotmc");
            if (test.exists()) {
                FUtil.ccFile(FAntiXRay.getPlugin().getResource("spigot.jar"), patch);
                System.out.println("SPIGOT DETECTED");
                spigot = true;
            } else {
                FUtil.ccFile(FAntiXRay.getPlugin().getResource("craftbukkit.jar"), patch);
                System.out.println("CRAFTBUKKT DETECTED");
            }
            
            try {
                extractJar(directory, patch, null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            try {
                zipDir(directory, new File(file.getParent(), "craftbukkit_patched.jar"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private static void zipDir(File directory, File zip) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream( zip ));
        zip(directory, directory, zos);
        zos.close();
    }
    
    private static final void zip(File directory, File base, ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;

        for (int i = 0, n = files.length; i < n; i++) {
            if (files[i].isDirectory()) {
                zip(files[i], base, zos);
            } else {
                FileInputStream in = new FileInputStream(files[i]);
                
                String name = files[i].getPath().substring(base.getPath().length() + 1);
                name = name.replace("\\", "/");
                
                ZipEntry entry = new ZipEntry(name);
                zos.putNextEntry(entry);

                System.out.println("Compressing: " + entry.getName());

                while (-1 != (read = in.read(buffer))) {
                    zos.write(buffer, 0, read);
                }
                in.close();
            }
        }
    }
    
    public static void extractJar(File directory, File zipFile, HashSet<String> extract) throws IOException {
        ZipFile zip = null;

        File file = null;
        InputStream is = null;
        OutputStream os = null;
        
        if (extract == null) {
            extract = new HashSet<>();
            extract.add("FAntiXRay");
            extract.add("FUtils");
            extract.add("NetworkManager");
            extract.add("Packet51MapChunk");
            extract.add("Packet56MapChunkBulk");
            extract.add("PlayerConnection");
        }
        
        byte[] buffer = new byte[ 2048 ];
        
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            if (!directory.exists() || !directory.isDirectory()) {
                throw new IOException("Invalid Directory: " + directory.getAbsolutePath());  
            }

            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> elements = zip.entries();
            
            while (elements.hasMoreElements()) {
                ZipEntry entry = elements.nextElement();
                file = new File(directory, entry.getName());
                
                boolean conflict = false;
                for (String s : extract) {
                    if (file.getName().startsWith(s)) {
                        conflict = true;
                    }
                }

                if (conflict) {
                    continue;
                }

                System.out.println("Extracting file: " + entry.getName() + "\n");

                if (entry.isDirectory() && !file.exists()) {
                    file.mkdirs();
                    continue;
                }
                
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                
                try {
                    is = zip.getInputStream(entry);
                    os = new FileOutputStream(file);
                    int Byte = 0;
                    
                    if (is == null) {
                        throw new ZipException("Failed to read zip file: "+entry.getName());  
                    }
                    
                    while ((Byte = is.read(buffer)) > 0) {
                        os.write(buffer, 0, Byte);
                    }
                } finally {
                    if( is != null ) {
                        try {
                            is.close();
                        } catch( Exception ex1 ) {}
                    }
                    if( os != null ) {
                        try {
                            os.close();
                        } catch( Exception ex1 ) {}
                    }
                }
            }
        } finally {
            if( zip != null ) {
                try {
                    zip.close();
                } catch( Exception e ) {}
            }
        }
    }
}
