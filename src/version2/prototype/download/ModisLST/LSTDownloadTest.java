package version2.prototype.download.ModisLST;

import java.io.IOException;
import org.xml.sax.SAXException;

import version2.prototype.ConfigReadException;
import version2.prototype.DataDate;
import version2.prototype.ModisTile;
import version2.prototype.download.DownloadFailedException;


public class LSTDownloadTest {

    public static void main(String[] args) throws ConfigReadException, IOException,DownloadFailedException, SAXException,NullPointerException, Exception {

        int h=10;
        int v=8;
        ModisTile mt=new ModisTile(h,v);
        DataDate md=new DataDate(13,03,2000);
        String op="/Users/fish/Desktop/LST";
        String op1="/Users/fish/Desktop/LSTQC";
        String mode="HTTP";
        String mHostURL="http://e4ftl01.cr.usgs.gov/MOLT/";
        ModisLSTDownloader lst=new ModisLSTDownloader(md,mt,op,op1,mode,mHostURL);
        lst.download();
        System.out.println("Hello ModisLST product!!!");
    }

}
