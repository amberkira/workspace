package com.routegis.applications.window.features;

import com.routegis.applications.window.core.*;


public abstract class AbstractOpenResourceFeature extends AbstractFeature implements NetworkActivitySignal.NetworkUser
{
    protected Thread loadingThread;

    protected AbstractOpenResourceFeature(String s, String featureID, String largeIconPath, Registry registry)
    {
        super(s, featureID, largeIconPath, registry);
    }

    public boolean hasNetworkActivity()
    {
        return this.loadingThread != null && this.loadingThread.isAlive();
    }

    protected Thread runOpenThread(final Object source)
    {
        this.loadingThread = new Thread()
        {
            @Override
            public void run()
            {
                getController().getNetworkActivitySignal().addNetworkUser(AbstractOpenResourceFeature.this);

                try
                {
                    new SimpleImporter(source, getController()).startImport();
                }
                finally
                {
                    controller.getNetworkActivitySignal().removeNetworkUser(AbstractOpenResourceFeature.this);
                }
            }
        };

        this.loadingThread.start();

        return this.loadingThread;
    }
}
