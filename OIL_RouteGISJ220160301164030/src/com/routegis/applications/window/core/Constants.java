package com.routegis.applications.window.core;


public interface Constants
{
    // Names and titles
    static final String APPLICATION_DISPLAY_NAME
        = "com.routegis.applications.worldwindow.ApplicationDisplayName";

    // Services
    public static final String IMAGE_SERVICE = "com.routegis.applications.worldwindow.ImageService";

    // Core object IDs
    static final String APP_PANEL = "com.routegis.applications.worldwindow.AppPanel";
    static final String APP_FRAME = "com.routegis.applications.worldwindow.AppFrame";
    static final String APPLET_PANEL = "com.routegis.applications.worldwindow.AppletPanel";
    static final String CONTROLS_PANEL = "com.routegis.applications.worldwindow.ControlsPanel";
    static final String MENU_BAR = "com.routegis.applications.worldwindow.MenuBar";
    static final String NETWORK_STATUS_SIGNAL = "com.routegis.applications.worldwindow.NetworkStatusSignal";
    static final String TOOL_BAR = "com.routegis.applications.worldwindow.ToolBar";
    static final String STATUS_PANEL = "com.routegis.applications.worldwindow.StatusPanel";
    static final String WW_PANEL = "com.routegis.applications.worldwindow.WWPanel";

    // Miscellaneous
    static final String ACCELERATOR_SUFFIX = ".Accelerator";
    static final String ACTION_COMMAND = "com.routegis.applications.worldwindow.ActionCommand";
    static final String CONTEXT_MENU_INFO = "com.routegis.applications.worldwindow.ContextMenuString";
    static final String FILE_MENU = "com.routegis.applications.worldwindow.feature.FileMenu";
    static final String INFO_PANEL_TEXT = "com.routegis.applications.worldwindow.InfoPanelText";
    static final String ON_STATE = "com.routegis.applications.worldwindow.OnState";
    static final String RADIO_GROUP = "com.routegis.applications.worldwindow.StatusBarMessage";
    static final String STATUS_BAR_MESSAGE = "com.routegis.applications.worldwindow.StatusBarMessage";

    // Layer types
    static final String INTERNAL_LAYER = "com.routegis.applications.worldwindow.InternalLayer";
        // application controls, etc.
    static final String ACTIVE_LAYER = "com.routegis.applications.worldwindow.ActiveLayer";
        // force display in active layers
    static final String USER_LAYER = "com.routegis.applications.worldwindow.UserLayer"; // User-generated layers
    static final String SCREEN_LAYER = "com.routegis.applications.worldwindow.ScreenLayer";
    // in-screen application controls, etc.

    // Feature IDs
    static final String FEATURE = "com.routegis.applications.worldwindow.feature";
    static final String FEATURE_ID = "com.routegis.applications.worldwindow.FeatureID";
    static final String FEATURE_ACTIVE_LAYERS_PANEL
        = "com.routegis.applications.worldwindow.feature.ActiveLayersPanel";
    static final String FEATURE_COMPASS = "com.routegis.applications.worldwindow.feature.Compass";
    static final String FEATURE_CROSSHAIR = "com.routegis.applications.worldwindow.feature.Crosshair";
    static final String FEATURE_COORDINATES_DISPLAY
        = "com.routegis.applications.worldwindow.feature.CoordinatesDisplay";
    static final String FEATURE_EXTERNAL_LINK_CONTROLLER
        = "com.routegis.applications.worldwindow.feature.ExternalLinkController";
    static final String FEATURE_GAZETTEER = "com.routegis.applications.worldwindow.feature.Gazetteer";
    static final String FEATURE_GAZETTEER_PANEL = "com.routegis.applications.worldwindow.feature.GazetteerPanel";
    static final String FEATURE_GRATICULE = "com.routegis.applications.worldwindow.feature.Graticule";
    static final String FEATURE_ICON_CONTROLLER = "com.routegis.applications.worldwindow.feature.IconController";
    static final String FEATURE_IMPORT_IMAGERY = "com.routegis.applications.worldwindow.feature.ImportImagery";
    static final String FEATURE_INFO_PANEL_CONTROLLER
        = "com.routegis.applications.worldwindow.feature.InfoPanelController";
    static final String FEATURE_LAYER_MANAGER_DIALOG
        = "com.routegis.applications.worldwindow.feature.LayerManagerDialog";
    static final String FEATURE_LAYER_MANAGER = "com.routegis.applications.worldwindow.feature.LayerManager";
    static final String FEATURE_LAYER_MANAGER_PANEL
        = "com.routegis.applications.worldwindow.feature.LayerManagerPanel";
    static final String FEATURE_LATLON_GRATICULE
        = "com.routegis.applications.worldwindow.feature.LatLonGraticule";
    static final String FEATURE_MEASUREMENT = "com.routegis.applications.worldwindow.feature.Measurement";
    static final String FEATURE_MEASUREMENT_DIALOG
        = "com.routegis.applications.worldwindow.feature.MeasurementDialog";
    static final String FEATURE_MEASUREMENT_PANEL
        = "com.routegis.applications.worldwindow.feature.MeasurementPanel";
    static final String FEATURE_NAVIGATION = "com.routegis.applications.worldwindow.feature.Navigation";
    static final String FEATURE_OPEN_FILE = "com.routegis.applications.worldwindow.feature.OpenFile";
    static final String FEATURE_OPEN_URL = "com.routegis.applications.worldwindow.feature.OpenURL";
    static final String FEATURE_SCALE_BAR = "com.routegis.applications.worldwindow.feature.ScaleBar";
    static final String FEATURE_TOOLTIP_CONTROLLER
        = "com.routegis.applications.worldwindow.feature.ToolTipController";
    static final String FEATURE_UTM_GRATICULE = "com.routegis.applications.worldwindow.feature.UTMGraticule";
    static final String FEATURE_WMS_PANEL = "com.routegis.applications.worldwindow.feature.WMSPanel";
    static final String FEATURE_WMS_DIALOG = "com.routegis.applications.worldwindow.feature.WMSDialog";

    // Specific properties
    static final String FEATURE_OWNER_PROPERTY = "com.routegis.applications.worldwindow.FeatureOwnerProperty";
    static final String TOOL_BAR_ICON_SIZE_PROPERTY
        = "com.routegis.applications.worldwindow.ToolBarIconSizeProperty";
}
