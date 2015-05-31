package version2.prototype.download.ModisNBAR;

import java.io.*;

import org.w3c.dom.*;

import version2.prototype.DataDate;
import version2.prototype.util.XmlUtils;

public class ModisNBARDownloadMetadata {
    private static final String ROOT_ELEMENT_NAME = "ModisNBARDownloadMetadata";
    private static final String DATE_ATTRIBUTE_NAME = "date";
    private static final String TIMESTAMP_ATTRIBUTE_NAME = "timestamp";

    private final DataDate mDate;
    private final long mTimestamp;

    public ModisNBARDownloadMetadata(DataDate date, long timestamp){
        mDate = date;
        mTimestamp = timestamp;
    }

    public DataDate getDate(){
        return mDate;
    }

    public long getTimestamp(){
        return mTimestamp;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof ModisNBARDownloadMetadata){
            return equals((ModisNBARDownloadMetadata)obj);
        }else{
            return false;
        }
    }

    public boolean equals (ModisNBARDownloadMetadata obj){
        return ((mDate.equals(obj.mDate)) && (mTimestamp == obj.mTimestamp));
    }

    public boolean equalsIgnoreTimestamp(ModisNBARDownloadMetadata obj){
        return (mDate.equals(obj.mDate));
    }

    public int compareTo(ModisNBARDownloadMetadata obj)
    {
        int cmp = mDate.compareTo(obj.mDate);

        if(cmp != 0) {
            return cmp;
        }

        return Long.valueOf(mTimestamp).compareTo(Long.valueOf(obj.mTimestamp));
    }

    @Override
    public int hashCode(){
        return mDate.hashCode() * 17 + Long.valueOf(mTimestamp).hashCode();
    }

    public Element toXml(Document doc){
        final Element rootElement = doc.createElement(ROOT_ELEMENT_NAME);

        rootElement.setAttribute(DATE_ATTRIBUTE_NAME, mDate.toCompactString());
        rootElement.setAttribute(TIMESTAMP_ATTRIBUTE_NAME, Long.toString(mTimestamp));

        return rootElement;
    }

    public static ModisNBARDownloadMetadata fromXml(Element rootElement) throws IOException{
        if(!rootElement.getNodeName().equals(ROOT_ELEMENT_NAME)){
            throw new IOException("Unexpected root element name");
        }

        final DataDate date = DataDate.fromCompactString(rootElement.getAttribute(DATE_ATTRIBUTE_NAME));
        final long timestamp = Long.parseLong(rootElement.getAttribute(TIMESTAMP_ATTRIBUTE_NAME));

        return new ModisNBARDownloadMetadata(date, timestamp);
    }

    public void toFile(File file) throws IOException {
        final Document doc = XmlUtils.newDocument(ROOT_ELEMENT_NAME);

        doc.replaceChild(toXml(doc), doc.getDocumentElement());
        XmlUtils.transformToGzippedFile(doc, file);
    }

    public static ModisNBARDownloadMetadata fromFile(File file) throws IOException {
        return fromXml(XmlUtils.parseGzipped(file).getDocumentElement());
    }
}