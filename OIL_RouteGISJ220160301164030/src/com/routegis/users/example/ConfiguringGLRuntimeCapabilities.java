
package com.routegis.users.example;

import javax.media.opengl.GLAutoDrawable;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.*;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.render.GLRuntimeCapabilities;
import core.routegis.engine.util.Logging;


public class ConfiguringGLRuntimeCapabilities extends ApplicationTemplate
{
    static
    {
        Configuration.setValue(AVKey.WORLD_WINDOW_CLASS_NAME, MyGLAutoDrawable.class.getName());
    }

    
    public static class MyGLAutoDrawable extends WorldWindowGLAutoDrawable
    {
        
        public MyGLAutoDrawable()
        {
        }

        
        public void init(GLAutoDrawable glAutoDrawable)
        {
            // Invoked when the GL context changes. The host machine capabilities may have changed, so re-configure the
            // OpenGL features used by the RouteGIS SDK.
            super.init(glAutoDrawable);
            this.configureGLRuntimeCaps();
        }

        
        protected void configureGLRuntimeCaps()
        {
            // Get a reference to the OpenGL Runtime Capabilities associated with this WorldWindow's SceneController.
            SceneController sc = this.getSceneController();
            if (sc == null)
                return;

            // Note: if your application uses a WWJ version prior to SVN revision #12956, then replace any calls to
            // SceneController.getGLRuntimeCapabilities() with
            //  SceneController.getDrawContext().getGLRuntimeCapabilities().

            GLRuntimeCapabilities glrc = sc.getGLRuntimeCapabilities();
            if (glrc == null)
            {
                String message = Logging.getMessage("nullValue.GLRuntimeCapabilitiesIsNull");
                Logging.logger().warning(message);
                return;
            }

            // Configure which OpenGL features may be used by the RouteGIS SDK. Configuration values for features
            // which are not available on the host machine are ignored. This example shows configuration of the OpenGL
            // framebuffer objects feature.
            glrc.setFramebufferObjectEnabled(this.isEnableFramebufferObjects());
        }

        
        protected boolean isEnableFramebufferObjects()
        {
            // Applications inject their logic for determining whether or not to enable use of OpenGL framebuffer
            // objects in the RouteGIS SDK. If OpenGL framebuffer objects are not available on the host machine,
            // this setting is ignored.
            return false;
        }
    }

    public static void main(String[] args)
    {
        start("RouteGIS SDK Configuring GL Runtime Capabilities", AppFrame.class);
    }
}
