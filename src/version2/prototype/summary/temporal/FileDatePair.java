package version2.prototype.summary.temporal;

import java.io.File;

import version2.prototype.DataDate;

public class FileDatePair {
    public File file;
    public DataDate date;

    public FileDatePair(File f, DataDate d)
    {
        file = f;
        date = d;
    }
}
