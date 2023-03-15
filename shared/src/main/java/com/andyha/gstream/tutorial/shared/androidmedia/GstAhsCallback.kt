/*
 * Copyright (C) 2016 SurroundIO
 *   Author: Martin Kelly <martin@surround.io>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 */
package org.freedesktop.gstreamer.androidmedia

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class GstAhsCallback(
    var mSensorCallback: Long,
    var mAccuracyCallback: Long, var mUserData: Long
) : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent) {
        gst_ah_sensor_on_sensor_changed(event, mSensorCallback, mUserData)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        gst_ah_sensor_on_accuracy_changed(
            sensor, accuracy,
            mAccuracyCallback, mUserData
        )
    }

    companion object {
        @JvmStatic
        external fun gst_ah_sensor_on_sensor_changed(
            event: SensorEvent?,
            callback: Long, user_data: Long
        )

        @JvmStatic
        external fun gst_ah_sensor_on_accuracy_changed(
            sensor: Sensor?, accuracy: Int,
            callback: Long, user_data: Long
        )
    }
}