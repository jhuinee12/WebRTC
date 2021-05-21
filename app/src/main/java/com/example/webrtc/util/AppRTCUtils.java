/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.webrtc.util;

import android.os.Build;
import android.util.Log;

/**
 * AppRTCUtils provides helper functions for managing thread safety.
 * AudioManager, BluetoothManager, ProximitySensor에서 로그를 찍거나 시스템 정보를 출력하기 위한 클래스
 */
public final class AppRTCUtils {
  private AppRTCUtils() {}

  /** Helper method which throws an exception  when an assertion has failed.
   * assertion이 실패할 때 예외처리 헬퍼 메서드
   * - assertion? 프로그램 안에 추가하는 참·거짓을 미리 가정하는 문*/
  public static void assertIsTrue(boolean condition) {
    if (!condition) {
      throw new AssertionError("Expected condition to be true");
    }
  }

  /** Helper method for building a string of thread information.
   * 쓰레드 정보를 문자열로 변경해서 넘겨주는 메서드 : ProximitySensor에서 로그찍을 때 사용*/
  public static String getThreadInfo() {
    return "@[name=" + Thread.currentThread().getName() + ", id=" + Thread.currentThread().getId()
        + "]";
  }

  /** Information about the current build, taken from system properties.
   * 시스템 속성에서 가져온 현재 빌드에 대한 정보 */
  public static void logDeviceInfo(String tag) {
    Log.d(tag, "Android SDK: " + Build.VERSION.SDK_INT + ", "
            + "Release: " + Build.VERSION.RELEASE + ", "
            + "Brand: " + Build.BRAND + ", "
            + "Device: " + Build.DEVICE + ", "
            + "Id: " + Build.ID + ", "
            + "Hardware: " + Build.HARDWARE + ", "
            + "Manufacturer: " + Build.MANUFACTURER + ", "
            + "Model: " + Build.MODEL + ", "
            + "Product: " + Build.PRODUCT);
  }
}
