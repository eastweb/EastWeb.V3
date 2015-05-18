package version2.prototype.download.ModisLST;

import java.io.*;

import org.w3c.dom.*;

import version2.prototype.*;
import version2.prototype.download.ModisId;
import version2.prototype.util.XmlUtils;



public final class ModisLSTDownloadMetadata implements Comparable<ModisLSTDownloadMetadata> {
    private static final String ROOT_ELEMENT_NAME = "ModisLSTDownloadMetadata";
    private static final String DOWNLOADED_ATTRIBUTE_NAME = "downloaded";

    private final ModisId mModisId;
    private final DataDate mDownloaded;

    public ModisLSTDownloadMetadata(ModisId modisId, DataDate downloaded) {
        mModisId = modisId;
        mDownloaded = downloaded;
    }

    public ModisId getModisId() {
        return mModisId;
    }

    public DataDate getDownloaded() {
        return mDownloaded;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModisLSTDownloadMetadata) {
            return equals((ModisLSTDownloadMetadata)obj);
        } else {
            return false;
        }
    }

    public boolean equals(ModisLSTDownloadMetadata o) {
        return mModisId.equals(o.mModisId) &&
                mDownloaded.equals(o.mDownloaded);
    }

    public boolean equalsIgnoreDownloaded(ModisLSTDownloadMetadata o) {
        return mModisId.equals(o.mModisId);
    }

    @Override
    public int hashCode() {
        int hash = mModisId.hashCode();
        hash = 17 * hash + mDownloaded.hashCode();
        return hash;
    }

    @Override
    public int compareTo(ModisLSTDownloadMetadata o) {
        int cmp = mModisId.compareTo(o.mModisId);
        if (cmp != 0) {
            return cmp;
        }

        return mDownloaded.compareTo(mDownloaded);
    }

    public Element toXml(Document doc) {
        final Element rootElement = doc.createElement(ROOT_ELEMENT_NAME);
        //rootElement.appendChild(ModisId.toXml(doc));
        rootElement.setAttribute(DOWNLOADED_ATTRIBUTE_NAME, mDownloaded.toCompactString());
        return rootElement;
    }

    public static ModisLSTDownloadMetadata fromXml(Element rootElement) throws IOException {
        if (rootElement.getNodeName() != ROOT_ELEMENT_NAME) {
            throw new IOException("Unexpected root element name");
        }

        final ModisId modisId = ModisId.fromXml(XmlUtils.getChildElement(rootElement));
        final DataDate downloaded = DataDate.fromCompactString(rootElement.getAttribute(DOWNLOADED_ATTRIBUTE_NAME));

        return new ModisLSTDownloadMetadata(modisId, downloaded);
    }

    public void toFile(File file) throws IOException {
        final Document doc = XmlUtils.newDocument(ROOT_ELEMENT_NAME);
        doc.replaceChild(toXml(doc), doc.getDocumentElement());
        XmlUtils.transformToGzippedFile(doc, file);
    }

    public static ModisLSTDownloadMetadata fromFile(File file) throws IOException {
        return fromXml(XmlUtils.parseGzipped(file).getDocumentElement());
    }
}