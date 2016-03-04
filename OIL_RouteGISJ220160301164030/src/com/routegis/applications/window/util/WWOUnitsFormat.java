

package com.routegis.applications.window.util;

import core.routegis.engine.util.UnitsFormat;


public class WWOUnitsFormat extends UnitsFormat
{
    private boolean showUTM = true;
    private boolean showWGS84 = true;

    public WWOUnitsFormat()
    {
        super(UnitsFormat.KILOMETERS, UnitsFormat.SQUARE_KILOMETERS, false);
    }

    public boolean isShowUTM()
    {
        return this.showUTM;
    }

    public void setShowUTM(boolean showUTM)
    {
        this.showUTM = showUTM;
    }

    public boolean isShowWGS84()
    {
        return this.showWGS84;
    }

    public void setShowWGS84(boolean showWGS84)
    {
        this.showWGS84 = showWGS84;
    }

    public String datumNL()
    {
        return this.datum() + NL;
    }

    public String datum()
    {
        return String.format(this.getLabel(LABEL_DATUM) + " %s", this.isShowWGS84() ? "WGS84" : "NAD27");
    }
}
