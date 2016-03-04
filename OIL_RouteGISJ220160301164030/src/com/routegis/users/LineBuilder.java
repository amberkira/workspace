
package com.routegis.users;

import javax.swing.*;
import javax.swing.border.*;

import core.routegis.engine.*;
import core.routegis.engine.avlist.*;
import core.routegis.engine.event.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.*;
import core.routegis.engine.render.*;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;


public class LineBuilder extends AVListImpl
{
    private final WorldWindow wwd;
    private boolean armed = false;
    private ArrayList<Position> positions = new ArrayList<Position>();
    private final RenderableLayer layer;
    private final Polyline line;
    private boolean active = false;

    
    public LineBuilder(final WorldWindow wwd, RenderableLayer lineLayer, Polyline polyline)
    {
        this.wwd = wwd;

        if (polyline != null)
        {
            line = polyline;
        }
        else
        {
            this.line = new Polyline();
            this.line.setFollowTerrain(true);
        }
        this.layer = lineLayer != null ? lineLayer : new RenderableLayer();
        this.layer.addRenderable(this.line);
        this.wwd.getModel().getLayers().add(this.layer);

        this.wwd.getInputHandler().addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent mouseEvent)
            {
                if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    if (armed && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
                    {
                        if (!mouseEvent.isControlDown())
                        {
                            active = true;
                            addPosition();
                        }
                    }
                    mouseEvent.consume();
                }
            }

            public void mouseReleased(MouseEvent mouseEvent)
            {
                if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    if (positions.size() == 1)
                        removePosition();
                    active = false;
                    mouseEvent.consume();
                }
            }

            public void mouseClicked(MouseEvent mouseEvent)
            {
                if (armed && mouseEvent.getButton() == MouseEvent.BUTTON1)
                {
                    if (mouseEvent.isControlDown())
                        removePosition();
                    mouseEvent.consume();
                }
            }
        });

        this.wwd.getInputHandler().addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent mouseEvent)
            {
                if (armed && (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0)
                {
                    // Don't update the polyline here because the wwd current cursor position will not
                    // have been updated to reflect the current mouse position. Wait to update in the
                    // position listener, but consume the event so the view doesn't respond to it.
                    if (active)
                        mouseEvent.consume();
                }
            }
        });

        this.wwd.addPositionListener(new PositionListener()
        {
            public void moved(PositionEvent event)
            {
                if (!active)
                    return;

                if (positions.size() == 1)
                    addPosition();
                else
                    replacePosition();
            }
        });
    }

    
    public RenderableLayer getLayer()
    {
        return this.layer;
    }

    
    public Polyline getLine()
    {
        return this.line;
    }

    
    public void clear()
    {
        while (this.positions.size() > 0)
            this.removePosition();
    }

    
    public boolean isArmed()
    {
        return this.armed;
    }

    
    public void setArmed(boolean armed)
    {
        this.armed = armed;
    }

    private void addPosition()
    {
        Position curPos = this.wwd.getCurrentPosition();
        if (curPos == null)
            return;

        this.positions.add(curPos);
        this.line.setPositions(this.positions);
        this.firePropertyChange("LineBuilder.AddPosition", null, curPos);
        this.wwd.redraw();
    }

    private void replacePosition()
    {
        Position curPos = this.wwd.getCurrentPosition();
        if (curPos == null)
            return;

        int index = this.positions.size() - 1;
        if (index < 0)
            index = 0;

        Position currentLastPosition = this.positions.get(index);
        this.positions.set(index, curPos);
        this.line.setPositions(this.positions);
        this.firePropertyChange("LineBuilder.ReplacePosition", currentLastPosition, curPos);
        this.wwd.redraw();
    }

    private void removePosition()
    {
        if (this.positions.size() == 0)
            return;

        Position currentLastPosition = this.positions.get(this.positions.size() - 1);
        this.positions.remove(this.positions.size() - 1);
        this.line.setPositions(this.positions);
        this.firePropertyChange("LineBuilder.RemovePosition", currentLastPosition, null);
        this.wwd.redraw();
    }

    // ===================== Control Panel ======================= //
    // The following code is an example program illustrating LineBuilder usage. It is not required by the
    // LineBuilder class, itself.

    private static class LinePanel extends JPanel
    {
        private final WorldWindow wwd;
        private final LineBuilder lineBuilder;
        private JButton newButton;
        private JButton pauseButton;
        private JButton endButton;
        private JLabel[] pointLabels;

        public LinePanel(WorldWindow wwd, LineBuilder lineBuilder)
        {
            super(new BorderLayout());
            this.wwd = wwd;
            this.lineBuilder = lineBuilder;
            this.makePanel(new Dimension(200, 400));
            lineBuilder.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                {
                    fillPointsPanel();
                }
            });
        }

        private void makePanel(Dimension size)
        {
            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
            newButton = new JButton("New");
            newButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    lineBuilder.clear();
                    lineBuilder.setArmed(true);
                    pauseButton.setText("Pause");
                    pauseButton.setEnabled(true);
                    endButton.setEnabled(true);
                    newButton.setEnabled(false);
                    ((Component) wwd).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            });
            buttonPanel.add(newButton);
            newButton.setEnabled(true);

            pauseButton = new JButton("Pause");
            pauseButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    lineBuilder.setArmed(!lineBuilder.isArmed());
                    pauseButton.setText(!lineBuilder.isArmed() ? "Resume" : "Pause");
                    ((Component) wwd).setCursor(Cursor.getDefaultCursor());
                }
            });
            buttonPanel.add(pauseButton);
            pauseButton.setEnabled(false);

            endButton = new JButton("End");
            endButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    lineBuilder.setArmed(false);
                    newButton.setEnabled(true);
                    pauseButton.setEnabled(false);
                    pauseButton.setText("Pause");
                    endButton.setEnabled(false);
                    ((Component) wwd).setCursor(Cursor.getDefaultCursor());
                }
            });
            buttonPanel.add(endButton);
            endButton.setEnabled(false);

            JPanel pointPanel = new JPanel(new GridLayout(0, 1, 0, 10));
            pointPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            this.pointLabels = new JLabel[20];
            for (int i = 0; i < this.pointLabels.length; i++)
            {
                this.pointLabels[i] = new JLabel("");
                pointPanel.add(this.pointLabels[i]);
            }

            // Put the point panel in a container to prevent scroll panel from stretching the vertical spacing.
            JPanel dummyPanel = new JPanel(new BorderLayout());
            dummyPanel.add(pointPanel, BorderLayout.NORTH);

            // Put the point panel in a scroll bar.
            JScrollPane scrollPane = new JScrollPane(dummyPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            if (size != null)
                scrollPane.setPreferredSize(size);

            // Add the buttons, scroll bar and inner panel to a titled panel that will resize with the main window.
            JPanel outerPanel = new JPanel(new BorderLayout());
            outerPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Line")));
            outerPanel.setToolTipText("Line control and info");
            outerPanel.add(buttonPanel, BorderLayout.NORTH);
            outerPanel.add(scrollPane, BorderLayout.CENTER);
            this.add(outerPanel, BorderLayout.CENTER);
        }

        private void fillPointsPanel()
        {
            int i = 0;
            for (Position pos : lineBuilder.getLine().getPositions())
            {
                if (i == this.pointLabels.length)
                    break;

                String las = String.format("Lat %7.4f\u00B0", pos.getLatitude().getDegrees());
                String los = String.format("Lon %7.4f\u00B0", pos.getLongitude().getDegrees());
                pointLabels[i++].setText(las + "  " + los);
            }
            for (; i < this.pointLabels.length; i++)
                pointLabels[i++].setText("");
        }
    }

    
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, false, false);

            LineBuilder lineBuilder = new LineBuilder(this.getMainWin(), null, null);
            this.getContentPane().add(new LinePanel(this.getMainWin(), lineBuilder), BorderLayout.WEST);
        }
    }

    
    public static void main(String[] args)
    {
        //noinspection deprecation
        ApplicationTemplate.start("RouteGIS SDK Line Builder", LineBuilder.AppFrame.class);
    }
}
