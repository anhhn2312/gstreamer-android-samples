/*
 * Copyright (C) 2012, Collabora Ltd.
 *   Author: Youness Alaoui
 *
 * Copyright (C) 2015, Collabora Ltd.
 *   Author: Justin Kim <justin.kim@collabora.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 *
 */
package org.freedesktop.gstreamer.androidmedia

import android.hardware.Camera

class GstAhcCallback(var mCallback: Long, var mUserData: Long) : Camera.PreviewCallback,
    Camera.ErrorCallback, Camera.AutoFocusCallback {
    override fun onPreviewFrame(data: ByteArray, camera: Camera) {
        gst_ah_camera_on_preview_frame(data, camera, mCallback, mUserData)
    }

    override fun onError(error: Int, camera: Camera) {
        gst_ah_camera_on_error(error, camera, mCallback, mUserData)
    }

    override fun onAutoFocus(success: Boolean, camera: Camera) {
        gst_ah_camera_on_auto_focus(success, camera, mCallback, mUserData)
    }

    companion object {
        @JvmStatic
        external fun gst_ah_camera_on_preview_frame(
            data: ByteArray?, camera: Camera?,
            callback: Long, user_data: Long
        )

        @JvmStatic
        external fun gst_ah_camera_on_error(
            error: Int, camera: Camera?,
            callback: Long, user_data: Long
        )

        @JvmStatic
        external fun gst_ah_camera_on_auto_focus(
            success: Boolean, camera: Camera?,
            callback: Long, user_data: Long
        )
    }
}