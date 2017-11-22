/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.graphics.RectF;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;

import java.util.ArrayList;

/**
 * A very simple Processor which receives detected TextBlocks and adds them to the overlay
 * as OcrGraphics.
 */
public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

    private GraphicOverlay<OcrGraphic> mGraphicOverlay;


    private Context context;
    OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
        mGraphicOverlay = ocrGraphicOverlay;
    }

    /**
     * Called by the detector to deliver detection results.
     * If your application called for it, this could be a place to check for
     * equivalent detections by tracking TextBlocks that are similar in location and content from
     * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
     * multiple detections.
     */
    @Override
    public void receiveDetections(Detector.Detections<TextBlock> detections) {
        mGraphicOverlay.clear();
        SparseArray<TextBlock> items = detections.getDetectedItems();
        float[] left = new float[items.size()];
        float [] right = new float[items.size()];
        float [] top = new float[items.size()];
        float [] bottom = new float[items.size()];
        int count = 0;
        float width, height, width1, height1;
        float x, y, x1, y1;
        boolean flag = true;
        for (int i = 0; i < items.size(); ++i) {
            flag = true;
            TextBlock item = items.valueAt(i);
            RectF rect = new RectF(item.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);

                width1 = rect.right - rect.left;
                height1 = rect.bottom - rect.top;

                for (int j = 0; j < count; j++) {
                    width = right[j] - left[j];
                    height = bottom[j] - top[j];
                    x = left[j] + (width) / 2;
                    y = top[j] + (height) / 2;
                    x1 = rect.left + width1 / 2;
                    y1 = rect.top + (height1) / 2;

                    if (Math.abs(x - x1) + 10 < (width + width1) / 2 || Math.abs(y - y1) + 10 < (height + height1) / 2) {
                         left[j]= Math.min(left[j], rect.left);
                         right[j] = Math.max(right[j], rect.right);
                         top[j] = Math.min(top[j], rect.top);
                         bottom[j] = Math.max(bottom[j], rect.bottom);
                         flag = false;
                         break;
                    }
                }

                if (flag){
                    left[count] = rect.left;
                    right[count] = rect.right;
                    top[count] = rect.top;
                    bottom[count] = rect.bottom;
                    count++;
                }
        }

        for (int i = 0 ; i < count ; i++){
            TextBlock item = items.valueAt(i);
            OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item, new RectF(left[i], top[i], right[i], bottom[i]));
            mGraphicOverlay.add(graphic);
        }

    }

    public float scaleX(float horizontal) {
        return horizontal;
    }

    /**
     * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
     */
    public float scaleY(float vertical) {
        return vertical;
    }

    /**
     * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateX(float x) {
//        if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
//            return mOverlay.getWidth() - scaleX(x);
//        } else {
            return scaleX(x);
//        }
    }

    /**
     * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateY(float y) {
        return scaleY(y);
    }
    /**
     * Frees the resources associated with this detection processor.
     */
    @Override
    public void release() {
        mGraphicOverlay.clear();
    }
}
