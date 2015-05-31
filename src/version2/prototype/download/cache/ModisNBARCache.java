package version2.prototype.download.cache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import version2.prototype.DataDate;
import version2.prototype.ModisTile;
import version2.prototype.util.XmlUtils;

public class ModisNBARCache implements Cache {
    //private static final long serialVersionUID = 1L;
    private static final String ROOT_ELEMENT_NAME = "DateCache";

    //TODO: Add this node to the Cache Xml - Chris Plucker
    private static final String TILE_ELEMENT_NAME = "ModisTile";
    private static final String TILE_HORIZONTAL_ATTRIBUTE_NAME = "hTile";
    private static final String TILE_VERTICAL_ATTRIBUTE_NAME = "vTile";

    private static final String LAST_UPDATED_ATTRIBUTE_NAME = "lastUpdated";
    private static final String START_DATE_ATTRIBUTE_NAME = "startDate";
    private static final String YEAR_ELEMENT_NAME = "Year";
    private static final String YEAR_ATTRIBUTE_NAME = "value";
    private static final String DAY_ELEMENT_NAME = "Day";
    private static final String DAY_ATTRIBUTE_NAME = "value";
    private static final String HOUR_ELEMENT_NAME="Hour";

    private final DataDate mLastUpdated;
    private final DataDate mStartDate;
    private final List<DataDate> mDates;
    private final List<ModisTile> mTiles;

    public ModisNBARCache(DataDate dataDate, DataDate startDate, List<DataDate> finished, List<ModisTile> tiles) {
        final List<DataDate> listCopy = new ArrayList<DataDate>(finished);
        mLastUpdated = dataDate;
        mStartDate = startDate;
        mTiles = tiles;

        Collections.sort(listCopy);
        mDates = Collections.unmodifiableList(listCopy);
    }

    @Override
    public DataDate getLastUpdated() {
        return mLastUpdated;
    }

    public DataDate getStartDate() {
        return mStartDate;
    }

    public List<DataDate> getDates() {
        return mDates;
    }

    public List<ModisTile> getTiles() {
        return mTiles;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DateCache ? equals(obj) : false;
    }

    public boolean equals(ModisNBARCache o) {
        return mLastUpdated.equals(o.mLastUpdated) &&
                mStartDate.equals(o.mStartDate) &&
                mDates.equals(o.mDates);
    }

    public static ModisNBARCache fromFile(File file) throws IOException {
        final Element rootElement = XmlUtils.parseGzipped(file).getDocumentElement();
        System.out.println(file.getPath()+" is open");

        if (!rootElement.getNodeName().equals(ROOT_ELEMENT_NAME)) {
            throw new IOException("Unexpected root element name");
        }

        // Get last updated date
        final DataDate lastUpdated = DataDate.fromCompactString(
                rootElement.getAttribute(LAST_UPDATED_ATTRIBUTE_NAME));

        // Get start date
        final DataDate startDate = DataDate.fromCompactString(rootElement.getAttribute(START_DATE_ATTRIBUTE_NAME));

        // Read data dates
        final List<DataDate> dates = new ArrayList<DataDate>();
        final NodeList yearNodes = rootElement.getElementsByTagName(YEAR_ELEMENT_NAME);

        for (int i = 0; i < yearNodes.getLength(); ++i) {
            final Element yearElement = (Element)yearNodes.item(i);
            final int year = Integer.parseInt(yearElement.getAttribute(YEAR_ATTRIBUTE_NAME));
            final NodeList dayNodes = yearElement.getElementsByTagName(DAY_ELEMENT_NAME);

            for (int j = 0; j < dayNodes.getLength(); ++j) {
                final Element dayElement = (Element)dayNodes.item(j);
                final int day = Integer.parseInt(dayElement.getAttribute(DAY_ATTRIBUTE_NAME));
                final NodeList hourNodes=dayElement.getElementsByTagName(HOUR_ELEMENT_NAME);

                for(int k=0; k<hourNodes.getLength(); ++k){
                    final Element hourElement=(Element)hourNodes.item(k);
                    final int hour=Integer.parseInt(hourElement.getTextContent());

                    dates.add(DataDate.DataDateWithHour(hour, day, year));
                }
            }
        }

        // Read data for modis tiles
        final List<ModisTile> tiles = new ArrayList<ModisTile>();
        final NodeList tileNodes = rootElement.getElementsByTagName(TILE_ELEMENT_NAME);

        for (int i = 0; i < tileNodes.getLength(); ++i) {
            final Element tileElement = (Element)tileNodes.item(i);
            final int horizontalVal = Integer.parseInt(tileElement.getAttribute(TILE_HORIZONTAL_ATTRIBUTE_NAME));
            final int verticalVal = Integer.parseInt(tileElement.getAttribute(TILE_VERTICAL_ATTRIBUTE_NAME));

            tiles.add(new ModisTile(horizontalVal, verticalVal));
        }

        return new ModisNBARCache(lastUpdated, startDate, dates, tiles);
    }

    public void toFile(File file) throws IOException {
        final Document doc = XmlUtils.newDocument(ROOT_ELEMENT_NAME);
        final Element rootElement = doc.getDocumentElement();

        rootElement.setAttribute(LAST_UPDATED_ATTRIBUTE_NAME, mLastUpdated.toCompactString());
        rootElement.setAttribute(START_DATE_ATTRIBUTE_NAME, mStartDate.toCompactString());

        int currentYear = -1;
        Element yearElement = null;

        for (DataDate date : mDates) {
            // Create a year element for each new year
            if (date.getYear() != currentYear) {
                currentYear = date.getYear();
                yearElement = doc.createElement(YEAR_ELEMENT_NAME);

                yearElement.setAttribute(YEAR_ATTRIBUTE_NAME, Integer.toString(currentYear));
                rootElement.appendChild(yearElement);
            }

            final Element dayElement = doc.createElement(DAY_ELEMENT_NAME);
            dayElement.setAttribute(DAY_ATTRIBUTE_NAME, Integer.toString(date.getDayOfYear()));

            final Element hourElement=doc.createElement(HOUR_ELEMENT_NAME);
            hourElement.setTextContent(Integer.toString(date.getHour()));
            dayElement.appendChild(hourElement);
            yearElement.appendChild(dayElement);
        }

        XmlUtils.transformToGzippedFile(doc, file);
    }
}

