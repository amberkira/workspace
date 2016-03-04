

package com.routegis.users;

import java.awt.*;

import com.routegis.users.util.HotSpotController;

import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.util.PrivateUtil;
import core.routegis.engine.util.layertree.LayerTree;


public class LayerTreeUsage extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected LayerTree layerTree;
        protected RenderableLayer hiddenLayer;

        protected HotSpotController controller;

        public AppFrame()
        {
            super(true, false, false); // Don't include the layer panel; we're using the on-screen layer tree.

            this.layerTree = new LayerTree();

            // Set up a layer to display the on-screen layer tree in the WorldWindow.
            this.hiddenLayer = new RenderableLayer();
            this.hiddenLayer.addRenderable(this.layerTree);
            this.getMainWin().getModel().getLayers().add(this.hiddenLayer);

            // Mark the layer as hidden to prevent it being included in the layer tree's model. Including the layer in
            // the tree would enable the user to hide the layer tree display with no way of bringing it back.
            this.hiddenLayer.setValue(AVKey.HIDDEN, true);

            // Refresh the tree model with the WorldWindow's current layer list.
            this.layerTree.getModel().refresh(this.getMainWin().getModel().getLayers());

            // Add a controller to handle input events on the layer tree.
            this.controller = new HotSpotController(this.getMainWin());

            // Size the World Window to take up the space typically used by the layer panel. This illustrates the
            // screen space gained by using the on-screen layer tree.
            Dimension size = new Dimension(1000, 600);
            this.setPreferredSize(size);
            this.pack();
            PrivateUtil.alignComponent(null, this, AVKey.CENTER);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Layer Tree", AppFrame.class);
    }
}
