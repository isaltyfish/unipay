package net.verytools.unipay.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IOUtilsTest {

    @Test
    public void testReadKey() throws IOException {
        InputStream in = IOUtils.readKey("classpath:/zfb_test.properties");
        assertSignType(in);
        assertSignType(IOUtils.readKey("classpath:zfb_test.properties"));
    }

    private void assertSignType(InputStream in) throws IOException {
        Properties p = new Properties();
        p.load(in);
        Assert.assertEquals(p.getProperty("sign_type"), "RSA2");
    }

}
