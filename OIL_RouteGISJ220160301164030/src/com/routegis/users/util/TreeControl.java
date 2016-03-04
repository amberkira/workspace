

package com.routegis.users.util;

import com.routegis.users.ApplicationTemplate;

import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.util.tree.*;


public class TreeControl extends ApplicationTemplate
{
    private static final String ICON_PATH = "images/16x16-icon-earth.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        HotSpotController controller;

        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            BasicTree tree = new BasicTree();

            BasicTreeLayout layout = new BasicTreeLayout(tree, 100, 200);
            layout.getFrame().setFrameTitle("TreeControl");
            tree.setLayout(layout);

            BasicTreeModel model = new BasicTreeModel();

            BasicTreeNode root = new BasicTreeNode("Root", ICON_PATH);
            model.setRoot(root);

            BasicTreeNode child = new BasicTreeNode("Child 1", ICON_PATH);
            child.setDescription("This is a child node");
            child.addChild(new BasicTreeNode("Subchild 1,1"));
            child.addChild(new BasicTreeNode("Subchild 1,2"));
            child.addChild(new BasicTreeNode("Subchild 1,3", ICON_PATH));
            root.addChild(child);

            child = new BasicTreeNode("Child 2", ICON_PATH);
            child.addChild(new BasicTreeNode("Subchild 2,1"));
            child.addChild(new BasicTreeNode("Subchild 2,2"));
            child.addChild(new BasicTreeNode("Subchild 2,3"));
            root.addChild(child);

            child = new BasicTreeNode("Child 3");
            child.addChild(new BasicTreeNode("Subchild 3,1"));
            child.addChild(new BasicTreeNode("Subchild 3,2"));
            child.addChild(new BasicTreeNode("Subchild 3,3"));
            root.addChild(child);

            tree.setModel(model);

            tree.expandPath(root.getPath());

            controller = new HotSpotController(this.getMainWin());

            layer.addRenderable(tree);

            // Add the layer to the model.
            insertBeforeCompass(this.getMainWin(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getMainWin());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Tree Control", AppFrame.class);
    }
}
