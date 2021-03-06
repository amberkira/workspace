package com.routegis.users.applet;

import netscape.javascript.JSObject;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.xml.parsers.*;
import javax.xml.stream.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import com.routegis.users.MeasureToolPanel;
import com.routegis.users.kml.*;
import com.routegis.users.util.*;

import core.routegis.engine.*;
import core.routegis.engine.avlist.*;
import core.routegis.engine.awt.*;
import core.routegis.engine.data.*;
import core.routegis.engine.event.SelectEvent;
import core.routegis.engine.exception.PrivateRuntimeException;
import core.routegis.engine.formats.gpx.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.*;
import core.routegis.engine.layers.Earth.*;
import core.routegis.engine.ogc.kml.*;
import core.routegis.engine.ogc.kml.impl.*;
import core.routegis.engine.render.*;
import core.routegis.engine.render.Polygon;
import core.routegis.engine.util.*;
import core.routegis.engine.util.layertree.*;
import core.routegis.engine.util.measure.MeasureTool;
import core.routegis.engine.util.measure.MeasureToolController;
import core.routegis.engine.view.orbit.*;
import sun.misc.BASE64Decoder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.util.*;

public class CommonApplet extends JApplet
{
	private static final long 	serialVersionUID = 8985791227168826681L;
	private static final double HUE_BLUE = 240d / 360d;
	private static final double HUE_RED = 0d / 360d;
	private static final int 	BUFFER_SIZE = 1024;
   
    protected HotSpotController hotSpotController;
    protected KMLApplicationController kmlAppController;
    protected BalloonController balloonController;
    protected ToolTipController toolTipController;
    protected HighlightController highlightController;

    private int lastTabIndex = -1;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private TerrainProfileLayer profile = new TerrainProfileLayer();
    private PropertyChangeListener measureToolListener = new MeasureToolListener();
    
	protected String 			mode;
	
    protected WindowGLCanvas 	hwnd;
    protected String			ServerURI;
    
    protected LayerTree 		layerTree;
    protected ViewControlsLayer viewControlsLayer;

    protected RenderableLayer 	hiddenLayer;					//控件
    
    protected RenderableLayer 	labelsLayer;					//地标建筑
    protected IconLayer			iconLayer;						//动态图标
    protected RenderableLayer 	nodeLayer;						//移动目标
    protected RenderableLayer	specialLayer;					//海洋环境
    protected RenderableLayer 	personnelLayer;					//人员信息
    protected IconLayer 		weatherLayer;					//天气信息
    protected RenderableLayer 	deviceLayer;					//接入设备
    protected RenderableLayer 	atmosphereLayer;				//风向信息
    protected RenderableLayer	analyticSurfaceLayer;

    protected StatusBarPanel 		statusPanel;
    protected FaultsMonitoringPanel faultsPanel;
    protected FaultsMonitoringModel faultsMonitoring;
    
    private Thread synchronization = new synchronizationThread("synchronization", this);
    
    public CommonApplet()
    {
    }

    public void init()
    {
        try
        {
            // Check for initial configuration values
            String value = getParameter("INITIAL_LATITUDE");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_LATITUDE, Double.parseDouble(value));
            value = getParameter("INITIAL_LONGITUDE");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_LONGITUDE, Double.parseDouble(value));
            value = getParameter("INITIAL_ALTITUDE");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_ALTITUDE, Double.parseDouble(value));
            value = getParameter("INITIAL_HEADING");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_HEADING, Double.parseDouble(value));
            value = getParameter("INITIAL_PITCH");
            if (value != null)
                Configuration.setValue(AVKey.INITIAL_PITCH, Double.parseDouble(value));
            value = getParameter("ServerURI");
            if (value != null){
            	ServerURI = value;
            }else{
            	ServerURI = "127.0.0.1/app/routeoa";
            }
            
            mode = getParameter("SYSTEMMODE");
            if (mode == null){
            	mode = "";
            }

            {
	            // Create Window GL Canvas
	            this.hwnd = new WindowGLCanvas();
	            this.getContentPane().add(this.hwnd, BorderLayout.CENTER);
	
	            // Create the default model as described in the current MainClass properties.
	            Model m = (Model) MainClass.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
	            this.hwnd.setModel(m);
	            
	            this.toolTipController = new ToolTipController(this.getMainWin(), AVKey.DISPLAY_NAME, null);
	            this.highlightController = new HighlightController(this.getMainWin(), SelectEvent.ROLLOVER);
	            this.hotSpotController = new HotSpotController(this.hwnd);
	            this.kmlAppController = new KMLApplicationController(this.hwnd);
	            this.balloonController = new BalloonController(this.hwnd)
	            {
	                @Override
	                protected void addDocumentLayer(KMLRoot document)
	                {
	                    addKMLLayer(document);
	                }
	            };

	            Layer WMS_layer = new RouteGISWMSLayer();
	            m.getLayers().add(WMS_layer);
	            
	            //TiledImageLayer BMNG256_layer = (TiledImageLayer) BasicFactory.create(AVKey.LAYER_FACTORY, "config/Earth/BMNG256.xml");
	            //wwd.getModel().getLayers().add(BMNG256_layer);
	            
	            this.labelsLayer = new RenderableLayer();
	            this.labelsLayer.setName(" 地标建筑");
	
	            this.iconLayer = new IconLayer();
	            this.iconLayer.setName(" 动态图标");
	            
	            this.atmosphereLayer = new RenderableLayer();
	            this.atmosphereLayer.setName(" 大气信息");
	            
	            this.nodeLayer = new RenderableLayer();
	            this.nodeLayer.setName(" 移动目标");
	            
	            this.specialLayer = new RenderableLayer();
	            this.specialLayer.setName(" 海洋环境");
	            
	            this.deviceLayer = new RenderableLayer();
	            this.deviceLayer.setName(" 视频监控");
	            
	            this.personnelLayer = new RenderableLayer();
	            this.personnelLayer.setName(" 人员信息");
	            
	            this.weatherLayer = new IconLayer();
	            this.weatherLayer.setName(" 天气信息");
	            this.weatherLayer.setEnabled(false);
	            
	            LayerList layers = hwnd.getModel().getLayers();
	            int targetPosition = layers.size() - 1;
	            for (Layer l : layers)
	            {
	                if (l.getName().indexOf("Compass") != -1)
	                {
	                    targetPosition = layers.indexOf(l);
	                    break;
	                }
	            }
	            layers.add(targetPosition, new CrosshairLayer());
	            layers.add(targetPosition, this.labelsLayer);
	            layers.add(targetPosition, this.iconLayer);
	            layers.add(targetPosition, this.nodeLayer);
	            layers.add(targetPosition, this.specialLayer);
	            layers.add(targetPosition, this.atmosphereLayer);
	            layers.add(targetPosition, this.personnelLayer);
	            layers.add(targetPosition, this.weatherLayer);
	            layers.add(targetPosition, this.deviceLayer);
	            
	            //wwd.getModel().getLayers().add(new LayerManagerLayer(this.wwd));
	            
	            if (mode.equalsIgnoreCase("analytic")){
	            	initAnalyticSurfaceLayer();
	            	{
		            	// Add terrain profile layer
		                profile.setEventSource(this.hwnd);
		                profile.setFollow(TerrainProfileLayer.FOLLOW_PATH);
		                profile.setShowProfileLine(false);
		                
		                this.hwnd.getModel().getLayers().add(profile);

		                // Add + tab
		                tabbedPane.add(new JPanel());
		                tabbedPane.setTitleAt(0, "+");
		                tabbedPane.addChangeListener(new ChangeListener()
		                {
		                    public void stateChanged(ChangeEvent changeEvent)
		                    {
		                        if (tabbedPane.getSelectedIndex() == 0)
		                        {
		                            // Add new measure tool in a tab when '+' selected
		                            MeasureTool measureTool = new MeasureTool(getMainWin());
		                            measureTool.setController(new MeasureToolController());
		                            tabbedPane.add(new MeasureToolPanel(getMainWin(), measureTool));
		                            tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "" + (tabbedPane.getTabCount() - 1));
		                            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		                            switchMeasureTool();
		                        }
		                        else
		                        {
		                            switchMeasureTool();
		                        }
		                    }
		                });

		                // Add measure tool control panel to tabbed pane
		                MeasureTool measureTool = new MeasureTool(this.getMainWin());
		                measureTool.setController(new MeasureToolController());
		                tabbedPane.add(new MeasureToolPanel(this.getMainWin(), measureTool));
		                tabbedPane.setTitleAt(1, "1");
		                tabbedPane.setSelectedIndex(1);
		                switchMeasureTool();
		            }
	            	this.getContentPane().add(this.tabbedPane, BorderLayout.WEST);
	            }
	            
	            
	            
	            this.hiddenLayer = new RenderableLayer(); 
	            this.hiddenLayer.setValue(AVKey.HIDDEN, true);
	
	            m.getLayers().add(this.hiddenLayer);
	            
	            // Give the KML app controller a reference to the BalloonController so that the app controller can open
	            // KML feature balloons when feature's are selected in the on-screen layer tree.
	            this.kmlAppController.setBalloonController(balloonController);
	            
	            LayerList layerList = m.getLayers();
	            for (Layer layer : layerList)
	            {
	            	String name = layer.getName();
	            	if (name.charAt(0) == ' ')
	            	{
	            		
	            	}else{
	            		layer.setValue(AVKey.HIDDEN, true);
	            	}
	            }
	            
	            if (mode.isEmpty()){
	            	synchronization.start();
	            	this.viewControlsLayer = new ViewControlsLayer();
	            	this.layerTree = new LayerTree();
	            	this.hiddenLayer.addRenderable(layerTree);
	            	this.layerTree.getModel().refresh(m.getLayers());
	            	m.getLayers().add(this.viewControlsLayer);
	                this.hwnd.addSelectListener(new ViewControlsSelectListener(this.hwnd, this.viewControlsLayer));
	                
	                faultsMonitoring = new FaultsMonitoringModel();
	            	faultsPanel = new FaultsMonitoringPanel(this, this.hwnd, this.faultsMonitoring, this.ServerURI);
	            	this.getContentPane().add(this.faultsPanel, BorderLayout.SOUTH);
	            }else{
	            	StatusBar statusBar = new StatusBar();
		            this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
		            // Forward events to the status bar to provide the cursor position info.
		            statusBar.setEventSource(this.hwnd);
	            }
            }
            try
            {
                JSObject win = JSObject.getWindow(this);
                win.call("appletInit", null);
            }
            catch (Exception ignore)
            {
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    public WorldWindow getMainWin()
    {
        return this.hwnd;
    }
    public class MeasureToolListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            // Measure shape position list changed - update terrain profile
            if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                    || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE))
            {
                updateProfile(((MeasureTool)event.getSource()));
            }
        }
    }
    private void switchMeasureTool()
    {
        // Disarm last measure tool when changing tab and switching tool
        if (lastTabIndex != -1)
        {
            MeasureTool mt = ((MeasureToolPanel)tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
            mt.setArmed(false);
            mt.removePropertyChangeListener(measureToolListener);
        }
        // Update terrain profile from current measure tool
        lastTabIndex = tabbedPane.getSelectedIndex();
        MeasureTool mt = ((MeasureToolPanel)tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
        mt.addPropertyChangeListener(measureToolListener);
        updateProfile(mt);
    }

    private void updateProfile(MeasureTool mt)
    {
        ArrayList<? extends LatLon> positions = mt.getPositions();
        if (positions != null && positions.size() > 1)
        {
            profile.setPathPositions(positions);
            profile.setEnabled(true);
        }
        else
            profile.setEnabled(false);
        
        getMainWin().redraw();
    }
    public void removeJPanel(JPanel p){
    	this.getContentPane().remove(p);
    	if (p==this.faultsPanel){
    		faultsPanel = null;
    		statusPanel = new StatusBarPanel(this, this.hwnd);
        	this.getContentPane().add(this.statusPanel, BorderLayout.SOUTH);
    	}else if (p==this.statusPanel){
    		statusPanel = null;
    		faultsPanel = new FaultsMonitoringPanel(this, this.hwnd, this.faultsMonitoring, this.ServerURI);
        	this.getContentPane().add(this.faultsPanel, BorderLayout.SOUTH);
    	}
    	this.getContentPane().revalidate();
    }
    public void start()
    {
        // Call javascript appletStart()
        try
        {
            JSObject win = JSObject.getWindow(this);
            win.call("appletStart", null);
        }
        catch (Exception ignore)
        {
        }
    }

    public void stop()
    {
        // Call javascript appletSop()
        try
        {
            JSObject win = JSObject.getWindow(this);
            win.call("appletStop", null);
        }
        catch (Exception ignore)
        {
        }

        // Shut down RouteGIS SDK when the browser stops this Applet.
        MainClass.shutDown();
    }
    
    protected static class StatusBarPanel extends JPanel
    {
    	protected CommonApplet app;
    	public StatusBarPanel(CommonApplet app, WindowGLCanvas hwnd){
    		this.app = app;
    		StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(hwnd);
            
    		JButton hideButton = new JButton("显示实时预警");
            hideButton.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e) {
            		setHideShow();
                }
            });
            this.add(hideButton, BorderLayout.WEST);
            this.add(statusBar, BorderLayout.EAST);
    	}
    	public void setHideShow(){
        	this.app.removeJPanel(this);
        }
    }
    @SuppressWarnings("unchecked")
    protected static class FaultsMonitoringPanel extends JPanel
    {
        private JComboBox editModeComboBox;
        protected JTable entryTable;
        protected boolean ignoreSelectEvents = false;
        protected CommonApplet app;
        private FaultsMonitoringModel model;
        public FaultsMonitoringPanel(CommonApplet app, WindowGLCanvas wwd, FaultsMonitoringModel model, String ServerURI)
        {
        	this.app = app;
        	this.model = model;
        	this.initComponents();
        }
        
        public int[] getSelectedIndices()
        {
            return this.entryTable.getSelectedRows();
        }

        public void setSelectedIndices(int[] indices)
        {
            this.ignoreSelectEvents = true;

            if (indices != null && indices.length != 0)
            {
                for (int index : indices)
                {
                    this.entryTable.setRowSelectionInterval(index, index);
                }
            }
            else
            {
                this.entryTable.clearSelection();
            }

            this.ignoreSelectEvents = false;
        }

        public String getSelectedEditMode()
        {
            return (String) this.editModeComboBox.getSelectedItem();
        }

        public void setSelectedEditMode(String editMode)
        {
            this.editModeComboBox.setSelectedItem(editMode);
        }
        
        public void setHideShow(){
        	this.app.removeJPanel(this);
        }

        protected void initComponents()
        {
            final JCheckBox enableEditCheckBox;
            final JCheckBox aboveGroundCheckBox;

            JPanel newShapePanel = new JPanel();
            {
                JButton newShapeButton = new JButton("告警发布");
                newShapeButton.setToolTipText("人工录入告警信息");

                JLabel editModeLabel = new JLabel("告警过滤:");
                this.editModeComboBox = new JComboBox();
                this.editModeComboBox.setEditable(false);
                this.editModeComboBox.setToolTipText("改变当前告警信息显示方式，只显示关注的");
                this.editModeComboBox.setName("告警信息过滤");

                enableEditCheckBox = new JCheckBox("不同颜色显示告警类型");
                enableEditCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                enableEditCheckBox.setToolTipText("Allow modifications to shapes");

                aboveGroundCheckBox = new JCheckBox("动态更新告警信息");
                aboveGroundCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                aboveGroundCheckBox.setToolTipText("Restrict shape movement to stay above ground");

                JPanel gridPanel = new JPanel(new GridLayout(0, 1, 0, 5)); // rows, cols, hgap, vgap
                gridPanel.add(newShapeButton);
                gridPanel.add(enableEditCheckBox);
                gridPanel.add(aboveGroundCheckBox);
                gridPanel.add(editModeLabel);
                gridPanel.add(this.editModeComboBox);

                newShapePanel.setLayout(new BorderLayout());
                newShapePanel.add(gridPanel, BorderLayout.NORTH);
            }

            JPanel entryPanel = new JPanel();
            {
                this.entryTable = new JTable(model);
                this.entryTable.setColumnSelectionAllowed(false);
                this.entryTable.setRowSelectionAllowed(true);
                this.entryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                this.entryTable.setToolTipText("<html>单击显示故障范围<br>双击显示详细状态</html>");

                JScrollPane tablePane = new JScrollPane(this.entryTable);
                tablePane.setPreferredSize(new Dimension(200, 100));

                entryPanel.setLayout(new BorderLayout(0, 0)); // hgap, vgap
                entryPanel.add(tablePane, BorderLayout.CENTER);
            }

            this.setLayout(new BorderLayout(15, 0)); // hgap, vgap
            this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
            this.add(newShapePanel, BorderLayout.WEST);
            this.add(entryPanel, BorderLayout.CENTER);
            
            JButton hideButton = new JButton(">");
            hideButton.addActionListener(new ActionListener(){
            	public void actionPerformed(ActionEvent e) {
            		setHideShow();
                }
            });
            this.add(hideButton, BorderLayout.EAST);
        }
    }
    

    protected static class FaultsMonitoringModel extends AbstractTableModel
    {
        protected static String[] columnName = {"描述"};
        protected static Class[] columnClass = {String.class};
        protected static String[] columnAttribute = {AVKey.DISPLAY_NAME};

        protected ArrayList<RenderableLayer> entryList = new ArrayList<RenderableLayer>();

        public FaultsMonitoringModel()
        {
        }

        public String getColumnName(int columnIndex)
        {
            return columnName[columnIndex];
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            return columnClass[columnIndex];
        }

        public int getRowCount()
        {
            return this.entryList.size();
        }

        public int getColumnCount()
        {
            return 1;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return true;
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
        	RenderableLayer entry = this.entryList.get(rowIndex);
            return entry.getValue(columnAttribute[columnIndex]);
        }

        public void setValueAt(Object aObject, int rowIndex, int columnIndex)
        {
        	RenderableLayer entry = this.entryList.get(rowIndex);
            String key = columnAttribute[columnIndex];
            entry.setValue(key, aObject);
        }

        public java.util.List<RenderableLayer> getEntries()
        {
            return Collections.unmodifiableList(this.entryList);
        }

        public void setEntries(Iterable<? extends RenderableLayer> entries)
        {
            this.entryList.clear();
            if (entries != null)
            {
                for (RenderableLayer entry : entries)
                {
                    this.entryList.add(entry);
                }
            }

            this.fireTableDataChanged();
        }

        public void addEntry(RenderableLayer entry)
        {
            this.entryList.add(entry);
            int index = this.entryList.size() - 1;
            this.fireTableRowsInserted(index, index);
        }

        public void removeEntry(RenderableLayer entry)
        {
            int index = this.entryList.indexOf(entry);
            if (index != -1)
            {
                this.entryList.remove(entry);
                this.fireTableRowsDeleted(index, index);
            }
        }

        public void removeAllEntries()
        {
            this.entryList.clear();
            this.fireTableDataChanged();
        }

        public RenderableLayer getEntry(int index)
        {
            return this.entryList.get(index);
        }

        public RenderableLayer setEntry(int index, RenderableLayer entry)
        {
            return this.entryList.set(index, entry);
        }

        public int getIndexForEntry(RenderableLayer entry)
        {
            return this.entryList.indexOf(entry);
        }
    }
    
    protected void initAnalyticSurfaceLayer()
    {
        this.analyticSurfaceLayer = new RenderableLayer();
        this.analyticSurfaceLayer.setPickEnabled(false);
        this.analyticSurfaceLayer.setName("Analytic Surfaces");

        LayerList layers = hwnd.getModel().getLayers();
        int targetPosition = layers.size() - 1;
        for (Layer l : layers)
        {
            if (l.getName().indexOf("Compass") != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition,  this.analyticSurfaceLayer);
        
        createRandomAltitudeSurface(HUE_BLUE, HUE_RED, 40, 40, this.analyticSurfaceLayer);
        
        createRandomColorSurface(HUE_BLUE, HUE_RED, 40, 40, this.analyticSurfaceLayer);

        // Load the static precipitation data. Since it comes over the network, load it in a separate thread to
        // avoid blocking the example if the load is slow or fails.
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                createPrecipitationSurface(HUE_BLUE, HUE_RED, analyticSurfaceLayer);
            }
        });
        t.start();
    }
    
    // ============== Public API - Javascript ======================= //

    
    public void gotoLatLon(double lat, double lon)
    {
        this.gotoLatLon(lat, lon, Double.NaN, 0, 0);
    }

    
    public void gotoLatLon(double lat, double lon, double zoom, double heading, double pitch)
    {
        BasicOrbitView view = (BasicOrbitView) this.hwnd.getView();
        if (!Double.isNaN(lat) || !Double.isNaN(lon) || !Double.isNaN(zoom))
        {
            lat = Double.isNaN(lat) ? view.getCenterPosition().getLatitude().degrees : lat;
            lon = Double.isNaN(lon) ? view.getCenterPosition().getLongitude().degrees : lon;
            zoom = Double.isNaN(zoom) ? view.getZoom() : zoom;
            heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
            pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;
            view.addPanToAnimator(Position.fromDegrees(lat, lon, 0),
                Angle.fromDegrees(heading), Angle.fromDegrees(pitch), zoom, true);
        }
    }

    
    public void setHeadingAndPitch(double heading, double pitch)
    {
        BasicOrbitView view = (BasicOrbitView) this.hwnd.getView();
        if (!Double.isNaN(heading) || !Double.isNaN(pitch))
        {
            heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
            pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;

            view.addHeadingPitchAnimator(
                view.getHeading(), Angle.fromDegrees(heading), view.getPitch(), Angle.fromDegrees(pitch));
        }
    }

    
    public void setZoom(double zoom)
    {
        BasicOrbitView view = (BasicOrbitView) this.hwnd.getView();
        if (!Double.isNaN(zoom))
        {
            view.addZoomAnimator(view.getZoom(), zoom);
        }
    }
    
    public void loadXML(String fname, String uri)
    {
    	((synchronizationThread) this.synchronization).LoadURI(uri, fname);
    }

    
    public WindowGLCanvas getCanvas()
    {
        return this.hwnd;
    }

    
    public OrbitView getOrbitView()
    {
        if (this.hwnd.getView() instanceof OrbitView)
            return (OrbitView) this.hwnd.getView();
        return null;
    }

    
    public Layer getLayerByName(String layerName)
    {
        for (Layer layer : hwnd.getModel().getLayers())
        {
            if (layer.getName().indexOf(layerName) != -1)
                return layer;
        }
        return null;
    }

    
    public void addLabel(String text, double lat, double lon, String font, String color)
    {
        GlobeAnnotation ga = new GlobeAnnotation(text, Position.fromDegrees(lat, lon, 0),
            Font.decode(font), Color.decode(color));
        ga.getAttributes().setBackgroundColor(Color.BLACK);
        ga.getAttributes().setDrawOffset(new Point(0, 0));
        ga.getAttributes().setFrameShape(AVKey.SHAPE_NONE);
        ga.getAttributes().setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        ga.getAttributes().setTextAlign(AVKey.CENTER);
        this.labelsLayer.addRenderable(ga);
    }
    
    public void addIcon(String text, double lat, double lon, String iconpath)
    {
    	Iterable<PrivateIcon> iconlist = this.iconLayer.getIcons();
    	Iterator<? extends PrivateIcon> iterator = iconlist.iterator();
    	while (iterator.hasNext()) {
    		PrivateIcon icon = iterator.next();
    		if (icon.getToolTipText().equals(text)){
    			icon.setPosition(new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0));
    			if (!iconpath.isEmpty()){
    				icon.setImageSource(iconpath);
    			}
    			return;
    		}
    	}
    	
    	UserFacingIcon icon = new UserFacingIcon(iconpath,
    			new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0));
    	icon.setSize(new Dimension(64, 64));
    	icon.setToolTipText(text);
    	icon.setShowToolTip(true);
    	
    	this.iconLayer.addIcon(icon);
    }
    
    public void addNode(String text, InputStream in)
    {
    	try {
    		ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);
            
			GpxReader reader = new GpxReader();
			reader.readStream(in);
            Iterator<Position> positions = reader.getTrackPositionIterator();
            
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            while (positions.hasNext())
            {
            	pathPositions.add(positions.next());
            }
            
            Path path = new Path(pathPositions);

            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            
            this.nodeLayer.addRenderable(path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    public void addPolygon(String name, String uri){
		try {
			
			URL url = new URL(uri);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", this.getAppletInfo()); //$NON-NLS-1$
			connection.setConnectTimeout(35000);
		
			InputStream is = connection.getInputStream();
			StringBuilder responseBody = new StringBuilder();
			if (is != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8")); //$NON-NLS-1$
				String s;
				boolean first = true;
				while ((s = in.readLine()) != null) {
					if(first){
						first = false;
					} else {
						responseBody.append("\n"); //$NON-NLS-1$
					}
					responseBody.append(s);
					
				}
				is.close();
			}
			addPolygon(name, new ByteArrayInputStream(responseBody.toString().getBytes()));  
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void addPolygon(String text, InputStream in)
    {
    	GpxReader reader;
		try {
			reader = new GpxReader();
		
			reader.readStream(in);
	        Iterator<Position> positions = reader.getTrackPositionIterator();
	        
	        ArrayList<Position> pathPositions = new ArrayList<Position>();
	        while (positions.hasNext())
	        {
	        	pathPositions.add(positions.next());
	        }
	        
	        ShapeAttributes normalAttributes = new BasicShapeAttributes();
            normalAttributes.setInteriorMaterial(Material.YELLOW);
            normalAttributes.setOutlineOpacity(0.5);
            normalAttributes.setInteriorOpacity(0.8);
            normalAttributes.setOutlineMaterial(Material.GREEN);
            normalAttributes.setOutlineWidth(2);
            normalAttributes.setDrawOutline(true);
            normalAttributes.setDrawInterior(true);
            normalAttributes.setEnableLighting(true);

            ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
            highlightAttributes.setOutlineMaterial(Material.WHITE);
            highlightAttributes.setOutlineOpacity(1);
            
	        Polygon pgon = new Polygon(pathPositions);
	        pgon.setValue(AVKey.DISPLAY_NAME, text);
            pgon.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            pgon.setAttributes(normalAttributes);
            pgon.setHighlightAttributes(highlightAttributes);
            //pgon.setRotation(-170d);
            this.labelsLayer.addRenderable(pgon);
            
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void addKMLLayer(KMLRoot kmlRoot)
    {
        // Create a KMLController to adapt the KMLRoot to the RouteGIS SDK renderable interface.
        KMLController kmlController = new KMLController(kmlRoot);

        // Adds a new layer containing the KMLRoot to the end of the WorldWindow's layer list. This
        // retrieves the layer name from the KMLRoot's DISPLAY_NAME field.
        RenderableLayer layer = new RenderableLayer();
        layer.setName((String) kmlRoot.getField(AVKey.DISPLAY_NAME));
        layer.addRenderable(kmlController);
        this.hwnd.getModel().getLayers().add(layer);

        // Adds a new layer tree node for the KMLRoot to the on-screen layer tree, and makes the new node visible
        // in the tree. This also expands any tree paths that represent open KML containers or open KML network
        // links.
        KMLLayerTreeNode layerNode = new KMLLayerTreeNode(layer, kmlRoot);
        this.layerTree.getModel().addLayer(layerNode);
        this.layerTree.makeVisible(layerNode.getPath());
        layerNode.expandOpenContainers(this.layerTree);

        layerNode.addPropertyChangeListener(AVKey.RETRIEVAL_STATE_SUCCESSFUL, new PropertyChangeListener()
        {
            public void propertyChange(final PropertyChangeEvent event)
            {
                if (event.getSource() instanceof KMLNetworkLinkTreeNode)
                {
                    // Manipulate the tree on the EDT.
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            ((KMLNetworkLinkTreeNode) event.getSource()).expandOpenContainers(layerTree);
                            hwnd.redraw();
                        }
                    });
                }
            }
        });
    }
    public void streamCopy(InputStream in, OutputStream out) throws IOException{
		byte[] b = new byte[BUFFER_SIZE];
		int read;

		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

    protected void createRandomAltitudeSurface(double minHue, double maxHue, int width, int height,
        RenderableLayer outLayer)
    {
        double minValue = -200e3;
        double maxValue = 200e3;

        AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(Sector.fromDegrees(5, 80, 0, -80));
        surface.setAltitude(400e3);
        surface.setDimensions(width, height);
        surface.setClientLayer(outLayer);
        outLayer.addRenderable(surface);

        BufferWrapper firstBuffer = randomGridValues(width, height, minValue, maxValue);
        BufferWrapper secondBuffer = randomGridValues(width, height, minValue * 2d, maxValue / 2d);
        mixValuesOverTime(2000L, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue, surface);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setShadowOpacity(0.5);
        surface.setSurfaceAttributes(attr);

        final double altitude = surface.getAltitude();
        final double verticalScale = surface.getVerticalScale();
        Format legendLabelFormat = new DecimalFormat("# km")
        {
            public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
            {
                double altitudeMeters = altitude + verticalScale * number;
                double altitudeKm = altitudeMeters * PrivateMath.METERS_TO_KILOMETERS;
                return super.format(altitudeKm, result, fieldPosition);
            }
        };

        AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(minValue, maxValue, minHue, maxHue,
            AnalyticSurfaceLegend.createDefaultColorGradientLabels(minValue, maxValue, legendLabelFormat),
            AnalyticSurfaceLegend.createDefaultTitle("GNSS-R数据"));
        legend.setOpacity(0.8);
        legend.setScreenLocation(new Point(650, 300));
        outLayer.addRenderable(createLegendRenderable(surface, 300, legend));
    }

    protected void createRandomColorSurface(double minHue, double maxHue, int width, int height,
        RenderableLayer outLayer)
    {
        double minValue = -200e3;
        double maxValue = 200e3;

        AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(Sector.fromDegrees(39, 49, 106, 116));
        surface.setAltitudeMode(MainClass.CLAMP_TO_GROUND);
        surface.setDimensions(width, height);
        surface.setClientLayer(outLayer);
        outLayer.addRenderable(surface);

        BufferWrapper firstBuffer = randomGridValues(width, height, minValue, maxValue);
        BufferWrapper secondBuffer = randomGridValues(width, height, minValue * 2d, maxValue / 2d);
        mixValuesOverTime(2000L, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue, surface);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setDrawShadow(false);
        attr.setInteriorOpacity(0.6);
        attr.setOutlineWidth(3);
        surface.setSurfaceAttributes(attr);
    }

    protected void mixValuesOverTime(
        final long timeToMix,
        final BufferWrapper firstBuffer, final BufferWrapper secondBuffer,
        final double minValue, final double maxValue, final double minHue, final double maxHue,
        final AnalyticSurface surface)
    {
        Timer timer = new Timer(20, new ActionListener()
        {
            protected long startTime = -1;

            public void actionPerformed(ActionEvent e)
            {
                if (this.startTime < 0)
                    this.startTime = System.currentTimeMillis();

                double t = (double) (e.getWhen() - this.startTime) / (double) timeToMix;
                int ti = (int) Math.floor(t);

                double a = t - ti;
                if ((ti % 2) == 0)
                    a = 1d - a;

                surface.setValues(createMixedColorGradientGridValues(
                    a, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue));

                if (surface.getClientLayer() != null)
                    surface.getClientLayer().firePropertyChange(AVKey.LAYER, null, surface.getClientLayer());
            }
        });
        timer.start();
    }

    public Iterable<? extends AnalyticSurface.GridPointAttributes> createMixedColorGradientGridValues(double a,
        BufferWrapper firstBuffer, BufferWrapper secondBuffer, double minValue, double maxValue,
        double minHue, double maxHue)
    {
        ArrayList<AnalyticSurface.GridPointAttributes> attributesList
            = new ArrayList<AnalyticSurface.GridPointAttributes>();

        long length = Math.min(firstBuffer.length(), secondBuffer.length());
        for (int i = 0; i < length; i++)
        {
            double value = PrivateMath.mixSmooth(a, firstBuffer.getDouble(i), secondBuffer.getDouble(i));
            attributesList.add(
                AnalyticSurface.createColorGradientAttributes(value, minValue, maxValue, minHue, maxHue));
        }

        return attributesList;
    }

    //
    //
    //

    protected void createPrecipitationSurface(double minHue, double maxHue, final RenderableLayer outLayer)
    {
        BufferWrapperRaster raster = loadRasterElevations("data/wa-precip-24hmam-5km.tif");
        if (raster == null)
            return;

        double[] extremes = PrivateBufferUtil.computeExtremeValues(raster.getBuffer(), raster.getTransparentValue());
        if (extremes == null)
            return;

        final AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(raster.getSector());
        surface.setDimensions(raster.getWidth(), raster.getHeight());
        surface.setValues(AnalyticSurface.createColorGradientValues(
            raster.getBuffer(), raster.getTransparentValue(), extremes[0], extremes[1], minHue, maxHue));
        surface.setVerticalScale(5e3);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setDrawOutline(false);
        attr.setDrawShadow(false);
        attr.setInteriorOpacity(0.6);
        surface.setSurfaceAttributes(attr);

        Format legendLabelFormat = new DecimalFormat("# ft")
        {
            public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
            {
                double valueInFeet = number * PrivateMath.METERS_TO_FEET;
                return super.format(valueInFeet, result, fieldPosition);
            }
        };

        final AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(extremes[0], extremes[1],
            minHue, maxHue,
            AnalyticSurfaceLegend.createDefaultColorGradientLabels(extremes[0], extremes[1], legendLabelFormat),
            AnalyticSurfaceLegend.createDefaultTitle("海浪变化"));
        legend.setOpacity(0.8);
        legend.setScreenLocation(new Point(100, 300));

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                surface.setClientLayer(outLayer);
                outLayer.addRenderable(surface);
                outLayer.addRenderable(createLegendRenderable(surface, 300, legend));
            }
        });
    }

    protected Renderable createLegendRenderable(final AnalyticSurface surface, final double surfaceMinScreenSize,
        final AnalyticSurfaceLegend legend)
    {
        return new Renderable()
        {
            public void render(DrawContext dc)
            {
                Extent extent = surface.getExtent(dc);
                if (!extent.intersects(dc.getView().getFrustumInModelCoordinates()))
                    return;

                if (PrivateMath.computeSizeInWindowCoordinates(dc, extent) < surfaceMinScreenSize)
                    return;

                legend.render(dc);
            }
        };
    }
    protected BufferWrapperRaster loadRasterElevations(String path)
    {
        // Download the data and save it in a temp file.
        File file = Util.saveResourceToTempFile(path, "." + InOut.getSuffix(path));

        // Create a raster reader for the file type.
        DataRasterReaderFactory readerFactory = (DataRasterReaderFactory) MainClass.createConfigurationComponent(
            AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
        DataRasterReader reader = readerFactory.findReaderFor(file, null);

        try
        {
            // Before reading the raster, verify that the file contains elevations.
            AVList metadata = reader.readMetadata(file, null);
            if (metadata == null || !AVKey.ELEVATION.equals(metadata.getStringValue(AVKey.PIXEL_FORMAT)))
            {
                String msg = Logging.getMessage("ElevationModel.SourceNotElevations", file.getAbsolutePath());
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            // Read the file into the raster.
            DataRaster[] rasters = reader.read(file, null);
            if (rasters == null || rasters.length == 0)
            {
                String msg = Logging.getMessage("ElevationModel.CannotReadElevations", file.getAbsolutePath());
                Logging.logger().severe(msg);
                throw new PrivateRuntimeException(msg);
            }

            // Determine the sector covered by the elevations. This information is in the GeoTIFF file or auxiliary
            // files associated with the elevations file.
            Sector sector = (Sector) rasters[0].getValue(AVKey.SECTOR);
            if (sector == null)
            {
                String msg = Logging.getMessage("DataRaster.MissingMetadata", AVKey.SECTOR);
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            // Request a sub-raster that contains the whole file. This step is necessary because only sub-rasters
            // are reprojected (if necessary); primary rasters are not.
            int width = rasters[0].getWidth();
            int height = rasters[0].getHeight();

            DataRaster subRaster = rasters[0].getSubRaster(width, height, sector, rasters[0]);

            // Verify that the sub-raster can create a ByteBuffer, then create one.
            if (!(subRaster instanceof BufferWrapperRaster))
            {
                String msg = Logging.getMessage("ElevationModel.CannotCreateElevationBuffer", path);
                Logging.logger().severe(msg);
                throw new PrivateRuntimeException(msg);
            }

            return (BufferWrapperRaster) subRaster;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    //
    //
    //

    protected static final int DEFAULT_RANDOM_ITERATIONS = 1000;
    protected static final double DEFAULT_RANDOM_SMOOTHING = 0.5d;

    public BufferWrapper randomGridValues(int width, int height, double min, double max, int numIterations,
        double smoothness, BufferFactory factory)
    {
        int numValues = width * height;
        double[] values = new double[numValues];

        for (int i = 0; i < numIterations; i++)
        {
            double offset = 1d - (i / (double) numIterations);

            int x1 = (int) Math.round(Math.random() * (width - 1));
            int x2 = (int) Math.round(Math.random() * (width - 1));
            int y1 = (int) Math.round(Math.random() * (height - 1));
            int y2 = (int) Math.round(Math.random() * (height - 1));
            int dx1 = x2 - x1;
            int dy1 = y2 - y1;

            for (int y = 0; y < height; y++)
            {
                int dy2 = y - y1;
                for (int x = 0; x < width; x++)
                {
                    int dx2 = x - x1;

                    if ((dx2 * dy1 - dx1 * dy2) >= 0)
                        values[x + y * width] += offset;
                }
            }
        }

        smoothValues(width, height, values, smoothness);
        scaleValues(values, numValues, min, max);
        BufferWrapper buffer = factory.newBuffer(numValues);
        buffer.putDouble(0, values, 0, numValues);

        return buffer;
    }

    public BufferWrapper randomGridValues(int width, int height, double min, double max)
    {
        return randomGridValues(width, height, min, max, DEFAULT_RANDOM_ITERATIONS, DEFAULT_RANDOM_SMOOTHING,
            new BufferFactory.DoubleBufferFactory());
    }

    protected void scaleValues(double[] values, int count, double minValue, double maxValue)
    {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < count; i++)
        {
            if (min > values[i])
                min = values[i];
            if (max < values[i])
                max = values[i];
        }

        for (int i = 0; i < count; i++)
        {
            values[i] = (values[i] - min) / (max - min);
            values[i] = minValue + values[i] * (maxValue - minValue);
        }
    }

    protected void smoothValues(int width, int height, double[] values, double smoothness)
    {
        // top to bottom
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x, width, height, smoothness);
        }

        // bottom to top
        int lastRowOffset = (height - 1) * width;
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x + lastRowOffset, -width, height, smoothness);
        }

        // left to right
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, y * width, 1, width, smoothness);
        }

        // right to left
        int lastColOffset = width - 1;
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, lastColOffset + y * width, -1, width, smoothness);
        }
    }

    protected void smoothBand(double[] values, int start, int stride, int count, double smoothness)
    {
        double prevValue = values[start];
        int j = start + stride;

        for (int i = 0; i < count - 1; i++)
        {
            values[j] = smoothness * prevValue + (1 - smoothness) * values[j];
            prevValue = values[j];
            j += stride;
        }
    }
    
	private class synchronizationThread extends Thread {
		private CommonApplet app;
		
		public synchronizationThread(String name, CommonApplet app) {
			super(name);
			this.app = app;
		}
		public void LoadKML(String uri, String cachefile)
		{
			BufferedInputStream inputStream = null;
			FileOutputStream stream = null;
			File fileToSave = null;
			
			File dir = new File(Configuration.getUserHomeDirectory());
			if (dir.getParentFile().canWrite()) {
				dir.mkdirs();
				if (dir.exists()) {
					fileToSave = new File(dir, cachefile);
				}
			}
			try {
				URL url = new URL(uri);
				URLConnection connection = url.openConnection();
				//connection.setRequestProperty("User-Agent", app.getAppletInfo()); //$NON-NLS-1$
				connection.setConnectTimeout(35000);
				inputStream = new BufferedInputStream(connection.getInputStream(), 8 * 1024);
	
				if (fileToSave!=null){
					//写到硬盘缓存
					stream = new FileOutputStream(fileToSave);
					streamCopy(inputStream, stream);
					stream.flush();
					
					KMLRoot kmlRoot = KMLRoot.createAndParse( fileToSave );
					
					app.addKMLLayer(kmlRoot);
				}
			}catch (IOException | XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
			}
			
		}
		public void LoadURI(String uri, String cachefile)
		{
			BufferedInputStream inputStream = null;
			FileOutputStream stream = null;
			File fileToSave = null;
			
			File dir = new File(Configuration.getUserHomeDirectory());
			if (dir.getParentFile().canWrite()) {
				dir.mkdirs();
				if (dir.exists()) {
					fileToSave = new File(dir, cachefile);
				}
			}
			try {
				URL url = new URL(uri);
				URLConnection connection = url.openConnection();
				//connection.setRequestProperty("User-Agent", app.getAppletInfo()); //$NON-NLS-1$
				connection.setConnectTimeout(35000);
				inputStream = new BufferedInputStream(connection.getInputStream(), 8 * 1024);
	
				if (fileToSave!=null){
					//写到硬盘缓存
					stream = new FileOutputStream(fileToSave);
					streamCopy(inputStream, stream);
					stream.flush();
					
		            PointPlacemarkAttributes h_poi_users_attrs = new PointPlacemarkAttributes();
		            h_poi_users_attrs.setImageAddress("images/pushpins/h_poi_users.png");
		            h_poi_users_attrs.setImageColor(new Color(1f, 1f, 1f, 1.0f));
		            h_poi_users_attrs.setScale(1.0);
//		            attrs.setImageOffset(new Offset(19d, 8d, AVKey.PIXELS, AVKey.PIXELS));
		            h_poi_users_attrs.setLabelOffset(new Offset(1.d, 0.4d, AVKey.FRACTION, AVKey.FRACTION));
		            
		            PointPlacemarkAttributes h_poi_wave_attrs = new PointPlacemarkAttributes();
		            h_poi_wave_attrs.setImageAddress("images/pushpins/h_poi_wave.png");
		            h_poi_wave_attrs.setImageColor(new Color(1f, 1f, 1f, 1.0f));
		            h_poi_wave_attrs.setScale(1.0);
		            h_poi_wave_attrs.setLineMaterial(Material.GREEN);
		            h_poi_wave_attrs.setLineWidth(5d);
		            h_poi_wave_attrs.setImageOffset(new Offset(28d, 28d, AVKey.PIXELS, AVKey.PIXELS));
		            h_poi_wave_attrs.setLabelOffset(new Offset(1.d, 0.4d, AVKey.FRACTION, AVKey.FRACTION));
		            
		            PointPlacemarkAttributes h_poi_wind_attrs = new PointPlacemarkAttributes();
		            h_poi_wind_attrs.setLabelColor("ffff0000");
		            h_poi_wind_attrs.setLineMaterial(Material.MAGENTA);
		            h_poi_wind_attrs.setLineWidth(2d);
		            h_poi_wind_attrs.setUsePointAsDefaultImage(true);
		            h_poi_wind_attrs.setScale(10d);
		            				
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();  
					Document document = db.parse(new FileInputStream(fileToSave));
					
					NodeList poilist = document.getElementsByTagName("POI"); 
					for(int i = 0; i < poilist.getLength(); i++)  
					{
						Element poi = (Element)poilist.item(i);  
						String name = poi.getAttribute("name");
						String iconpath = poi.getAttribute("iconpath");
						double longitude = Double.valueOf(poi.getAttribute("lon"));
						double latitude = Double.valueOf(poi.getAttribute("lat"));
						
						app.addIcon(name, latitude, longitude, iconpath);
					}
					
					NodeList labellist = document.getElementsByTagName("LABEL"); 
					
					app.personnelLayer.removeAllRenderables();
					app.specialLayer.removeAllRenderables();
					app.iconLayer.removeAllIcons();
					
					ShapeAttributes attrs = new BasicShapeAttributes();
		            attrs.setInteriorMaterial(Material.YELLOW);
		            attrs.setInteriorOpacity(0.7);
		            attrs.setEnableLighting(true);
		            attrs.setOutlineMaterial(Material.RED);
		            attrs.setOutlineWidth(2d);
		            attrs.setDrawInterior(true);
		            attrs.setDrawOutline(false);
		            
		            BalloonAttributes BalloonAttrs = new BasicBalloonAttributes();
		            BalloonAttrs.setSize(new Size(Size.NATIVE_DIMENSION, 0d, null, Size.NATIVE_DIMENSION, 0d, null));
		            
		            
					for(int i = 0; i < labellist.getLength(); i++)  
					{
						Element poi = (Element)labellist.item(i);  
						String name = poi.getAttribute("name");
						String type = poi.getAttribute("type");
						double lon = Double.valueOf(poi.getAttribute("lon"));
						double lat = Double.valueOf(poi.getAttribute("lat"));
						if(type.equalsIgnoreCase("poi_users")){
							Position pos = Position.fromDegrees(lat, lon, 0.0);
							PointPlacemark pp = new PointPlacemark(pos);
				            pp.setLabelText(name);
				            if (poi.getNodeValue()!=null){
				            	AbstractBrowserBalloon balloon = new GlobeBrowserBalloon(poi.getNodeValue(), pos);
				            	balloon.setAttributes(BalloonAttrs);
				            	balloon.setVisible(false);
				            	pp.setValue(AVKey.BALLOON, balloon);
				            }
				            pp.setLineEnabled(false);
				            pp.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
				            pp.setAttributes(h_poi_users_attrs);
				            
				            app.personnelLayer.addRenderable(pp);
				            
						}else if (
								type.equalsIgnoreCase("poi_wave"))
						{
							double significant_wave_height = Double.valueOf(poi.getAttribute("swh"));
							double limnimeter = Double.valueOf(poi.getAttribute("l"));
							
							PointPlacemark pp = new PointPlacemark(Position.fromDegrees(lat, lon, 500.0));
				            pp.setLabelText(String.format("%.2f/%.2f(米)",significant_wave_height, limnimeter));
				            pp.setLineEnabled(true);
				            pp.setValue(AVKey.DISPLAY_NAME, name);
				            pp.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
				            
				            /*
				            h_poi_wave_attrs.setLineMaterial(new Material()));
				            */
				            
				            pp.setAttributes(h_poi_wave_attrs);
					    	app.specialLayer.addRenderable(pp);

						}else if (
								type.equalsIgnoreCase("poi_wind"))
						{
							double angle = Double.valueOf(poi.getAttribute("o"));
							double speed = Double.valueOf(poi.getAttribute("s"));
							
							PointPlacemark pp = new PointPlacemark(Position.fromDegrees(lat, lon, 1000.0));

				            pp = new PointPlacemark(Position.fromDegrees(lat, lon, 1000.0));
				            pp.setLabelText(String.format("%.2f(米/秒)",speed));
				            pp.setValue(AVKey.DISPLAY_NAME, name);
				            pp.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
				            pp.setLineEnabled(true);
				            pp.setAttributes(h_poi_wind_attrs);
					    	
					    	app.atmosphereLayer.addRenderable(pp);
						}else if (
								type.equalsIgnoreCase("fault"))
						{
							UserFacingIcon icon = new UserFacingIcon("images/ico-alarm.png",
					    			new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0));
					    	icon.setSize(new Dimension(56,56));
					    	icon.setToolTipText(name);
					    	icon.setShowToolTip(true);
					    	
					    	app.iconLayer.addIcon(icon);
						}else{
							app.addLabel(name, lat, lon, "宋体", "#808080");
						}
					}
					
					NodeList gpxlist = document.getElementsByTagName("GPX"); 
					for(int i = 0; i < gpxlist.getLength(); i++)  
					{
						Element gpx = (Element)gpxlist.item(i);  
						String name = gpx.getAttribute("name");
						String type = gpx.getAttribute("type");
						String b64data = gpx.getTextContent();
						
						byte[] bytegpx = (new BASE64Decoder()).decodeBuffer(b64data);
						ByteArrayInputStream is = new ByteArrayInputStream(bytegpx);
						if (type.equalsIgnoreCase("polygon")){
							addPolygon(name, is);
						}else{
							addNode(name, is);
						}
						
						is.close();
					}
				}
			}
			catch (IOException | ParserConfigurationException | SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
			}
		}
		
		@Override
		public void run() {
			LoadKML("http://"+app.ServerURI+"/gis.php?id="+app.getAppletInfo()+"&m=kml", "InitializePosition.kml");
			LoadURI("http://"+app.ServerURI+"/gis.php?id="+app.getAppletInfo()+"&m=init", "InitializePosition.xml");
			for(;;){
				LoadURI("http://"+app.ServerURI+"/gis.php?id="+app.getAppletInfo(), "DynamicPosition.xml");
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
