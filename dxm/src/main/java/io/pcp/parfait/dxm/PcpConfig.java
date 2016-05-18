package io.pcp.parfait.dxm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.io.Closeables;

public class PcpConfig {

    private final static String pcpConf = System.getenv("PCP_CONF");
    private final static String pcpDir = System.getenv("PCP_DIR");

    public File getRoot() {
        return new File(pcpDir, "/");
    }

    private File getConfigFile() {
        if (pcpConf == null) {
            File etcDir = new File(getRoot(), "etc");
            return new File(etcDir, "pcp.conf");
        }
        return new File(pcpConf);
    }

    public String getValue(String key) {
        Properties properties = new Properties();
        File configuration = getConfigFile();
        InputStream is = null;
        String value = System.getenv(key);

        if (value != null) {
            return value;
        }

        try {
            is = new FileInputStream(configuration);
            properties.load(is);
            value = properties.get(key).toString();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            // just drop these, and go with defaults
        }
        finally {
            Closeables.closeQuietly(is);
        }
        return value;
    }
}
