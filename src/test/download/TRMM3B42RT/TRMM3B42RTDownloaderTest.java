/**
 *
 */
package test.download.TRMM3B42RT;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import version2.prototype.ConfigReadException;
import version2.prototype.util.PostgreSQLConnection;
import version2.prototype.util.Schemas;

/**
 * @author michael.devos
 *
 */
public class TRMM3B42RTDownloaderTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * Test method for {@link version2.prototype.download.TRMM3B42RT.TRMM3B42RTDownloader#download()}.
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ConfigReadException
     */
    @Test
    public final void testDownload() throws ConfigReadException, ClassNotFoundException, SQLException, ParserConfigurationException, SAXException, IOException {
        Schemas.CreateProjectPluginSchema(PostgreSQLConnection.getConnection(), "balh", "sdfajlk ", "TRMM3B42RT", null, null, LocalDate.now().minusDays(10), 1, null, null, false);
        fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link version2.prototype.download.TRMM3B42RT.TRMM3B42RTDownloader#TRMM3B42RTDownloader(version2.prototype.DataDate, java.lang.String, version2.prototype.PluginMetaData.PluginMetaDataCollection.DownloadMetaData)}.
     */
    @Test
    public final void testTRMM3B42RTDownloader() {
        fail("Not yet implemented"); // TODO
    }

}
