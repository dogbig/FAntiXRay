package me.FurH.FAntiXRay.database;


import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import me.FurH.Core.CorePlugin;
import me.FurH.Core.database.CoreSQLDatabase;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.file.FileUtils;
import me.FurH.Core.util.Communicator;
import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.cache.FCacheData;
import me.FurH.FAntiXRay.configuration.FConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class FSQLDatabase extends CoreSQLDatabase {

    private FAntiXRay plugin;
    private String prefix = "xray_";
    
    public FSQLDatabase(CorePlugin plugin) {
        super(plugin, "xray_", "SQLite", "", "", "", "", "");
        this.plugin = (FAntiXRay) plugin;
    }
    
    public void deleteAll() {
        for (World world : Bukkit.getWorlds()) {
            queue("DROP TABLE `"+prefix+world.getName()+"`;");
        }
    }
    
    public void load() {
        
        for (World world : Bukkit.getWorlds()) {
            load(world);
        }
        
    }
    
    public void load(World world) {

        Communicator com = plugin.getCommunicator();

        try {
            createTable("CREATE TABLE IF NOT EXISTS `"+prefix+world.getName()+"` (key INT, hash INT, engine INT, data BLOB, time INT, PRIMARY KEY (`key`))");
        } catch (CoreException ex) {
            com.error(ex, "Failed to create '"+world.getName()+"' world table");
        }

        try {
            createIndex("CREATE INDEX `"+prefix+world.getName()+"_search` ON `"+prefix+world.getName()+"` (key);");
        } catch (CoreException ex) {
            com.error(ex, "Failed to create '"+world.getName()+"' world index");
        }

    }
    
    public void setChunkData(String world, long key, long hash, int engine, byte[] inflatedBuffer) throws Exception {
        PreparedStatement ps = prepare("INSERT OR IGNORE INTO `"+prefix+world+"` (key, hash, engine, data, time) VALUES ('"+key+"', '"+hash+"', '"+engine+"', ?, '"+System.currentTimeMillis()+"'); "
                + "UPDATE `"+prefix+world+"` SET hash = '"+hash+"', data = ? WHERE key LIKE '"+key+"';");
        ps.setBytes(1, compress(inflatedBuffer));
        ps.execute();
    }
    
    public FCacheData getDataFrom(String world, long key, long hash, int engine) {
        FCacheData ret = null;
        
        //long start = System.currentTimeMillis();
        
        Communicator com = plugin.getCommunicator();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {

            ps = getQuery("SELECT key, hash, engine, data, time FROM `"+prefix+world+"` WHERE key = '"+key+"' ORDER BY 'time' DESC LIMIT 1;");
            rs = ps.getResultSet();

            if (rs.next()) {

                long key0 = rs.getLong("key");
                long hash0 = rs.getLong("hash");
                int engine0 = rs.getInt("engine");

                if (hash != hash0) {
                    queue("DELETE FROM `"+prefix+world+"` WHERE key = '"+key+"';"); return null;
                }
                
                if (engine != engine0) {
                    return null;
                }

                byte[] data = rs.getBytes("data");

                ret = new FCacheData(world, key0, decompress(data), hash0, engine0);
            }

        } catch (Exception ex) {
            com.error(ex, "Failed to get obfuscated data from key: " + key + ", hash: " + hash);
        } finally {
            FileUtils.closeQuietly(rs);
        }

        //System.out.println("SQL: " + (Math.abs(System.currentTimeMillis() - start)));
        
        return ret;
    }
    
    public byte[] compress(byte[] data) {

        //long start = System.currentTimeMillis();
        
        ByteArrayOutputStream baos = null;
        Deflater def = null;

        try {
            
            FConfiguration config = FAntiXRay.getConfiguration();

            int level = config.cache_compression;
            if (level > Deflater.BEST_COMPRESSION) {
                config.cache_compression = Deflater.BEST_COMPRESSION;
            }

            def = new Deflater(level);
            def.setInput(data);

            baos = new ByteArrayOutputStream(data.length);
            def.finish();

            byte[] buffer = new byte[ 128 ];
            while (!def.finished()) {
                int read = def.deflate(buffer);
                baos.write(buffer, 0, read);
            }

            byte[] compress = baos.toByteArray();
            
            //System.out.println("COMPRESS: " + (Math.abs(System.currentTimeMillis() - start)) + ", DATA: " + data.length + ", TO: " + compress.length);
            
            return compress;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            FileUtils.closeQuietly(baos);
            try {
                if (def != null) {
                    def.end();
                }
            } catch (Exception ex) { }
        }
        
        return null;
    }

    public byte[] decompress(byte[] data) {

        //long start = System.currentTimeMillis();
        
        ByteArrayOutputStream baos = null;
        Inflater inf = null;

        try {

            inf = new Inflater();
            inf.setInput(data);

            baos = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[ 128 ];

            while (!inf.finished()) {
                int read = inf.inflate(buffer);
                baos.write(buffer, 0, read);
            }

            byte[] decompress = baos.toByteArray();
            
            //System.out.println("DECOMPRESS: " + (Math.abs(System.currentTimeMillis() - start)) + ", DATA: " + data.length + ", TO: " + decompress.length);
            
            return decompress;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            FileUtils.closeQuietly(baos);
            try {
                if (inf != null) {
                    inf.end();
                }
            } catch (Exception ex) { }
        }

        return null;
    }
}
