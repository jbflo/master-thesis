///////////////////////////////////////////////////////////////////////////////
//FILE:          ProjectionDevice.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     Projector plugin
//-----------------------------------------------------------------------------
//AUTHOR:        Arthur Edelstein
//COPYRIGHT:     University of California, San Francisco, 2010-2014
//LICENSE:       This file is distributed under the BSD license.
//               License text is included with the source distribution.
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.rapp;

import ij.process.FloatPolygon;
import java.util.List;

public interface RappDevice {
   // Properties of device.
   String getName();
   String getChannel();
   double getXRange();
   double getYRange();
   double getXMinimum();
   double getYMinimum();
 
   // ## Alert when something has changed.
   void addOnStateListener(OnStateListener listener);

   // ## Get/set internal exposure setting
   long getExposure();
   void setExposure(long interval_us);

   // ## Control illumination
   void turnOn();
   void turnOff();
   void displaySpot(double x, double y);
   void activateAllPixels();

   // ## ROIs
   void loadRois(List<FloatPolygon> rois);
   void setPolygonRepetitions(int reps);
   void runPolygons();

   void waitForDevice();
}
