/*
 *  Copyright 2015 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.example.webrtc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

/**
 * Asynchronous http requests implementation.
 * 웹소켓을 통해 https://appr.tc 와 통신하여 Room을 생성하기 위한 헬퍼 클래스.
 */
public class AsyncHttpURLConnection {
  private static final int HTTP_TIMEOUT_MS = 8000;
  private static final String HTTP_ORIGIN = "https://appr.tc";
  private final String method;  // GET VS POST
  private final String url; // RoomUrl
  private final String message;
  private final AsyncHttpEvents events;
  private String contentType; // utf-8 등등 타입 지정

  /**
   * Http requests callbacks.
   */
  public interface AsyncHttpEvents {
    void onHttpError(String errorMessage);
    void onHttpComplete(String response);
  }

  public AsyncHttpURLConnection(String method, String url, String message, AsyncHttpEvents events) {
    this.method = method;
    this.url = url;
    this.message = message;
    this.events = events;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public void send() {
    new Thread(this ::sendHttpMessage).start();
  }

  private void sendHttpMessage() {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      byte[] postData = new byte[0];
      if (message != null) {
        postData = message.getBytes("UTF-8");
      }
      connection.setRequestMethod(method);  // 통신 방식 설정 GET vs POST : 매개변수로 받아옴
      connection.setUseCaches(false); // 캐싱데이터를 받을지 안받을지
      connection.setDoInput(true);  // Server 통신에서 입력 가능한 상태로 만듬 (읽기모드 지정)
      connection.setConnectTimeout(HTTP_TIMEOUT_MS);  // 연결시간 초과 설정
      connection.setReadTimeout(HTTP_TIMEOUT_MS);  // 데이터 읽어오는 시간 초과 설정
      // TODO(glaznev) - query request origin from pref_room_server_url_key preferences.
      connection.addRequestProperty("origin", HTTP_ORIGIN);
      boolean doOutput = false;
      if (method.equals("POST")) {
        doOutput = true;  // POST 방식 연결 성공
        connection.setDoOutput(true);  // Server 통신에서 출력 가능한 상태로 만듬 (쓰기모드 지정)
        connection.setFixedLengthStreamingMode(postData.length);  // 고정 길이 스트링 모드 : 요청 데이터의 크기를 미리 알 수 있는 경우
      }
      
      // utf-8 등등 타입 지정
      // 이 프로젝트에서는 setContentType() 메서드를 호출하지 않았으므로 null 값 들어옴
      if (contentType == null) {
        connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
      } else {
        connection.setRequestProperty("Content-Type", contentType);
      }

      // Send POST request.
      // POST 방식이 성공했다!
      if (doOutput && postData.length > 0) {
        // OutputStream : 데이터가 나가는 통로의 역할에 관해 규정하고 있는 추상 클래스
        // OutputStream이 갖춰야 할 것 : 데이터 쓰기, 버퍼 비우기, 통로 끊기
        OutputStream outStream = connection.getOutputStream();
        outStream.write(postData);
        outStream.close();
      }

      // Get response.
      int responseCode = connection.getResponseCode();
      // 요청 정상 처리 응답 코드 : 200
      // 그 외의 코드는 에러 처리
      if (responseCode != 200) {
        events.onHttpError("Non-200 response to " + method + " to URL: " + url + " : "
            + connection.getHeaderField(null));
        connection.disconnect();
        return;
      }
      // InputStream : 데이터를 byte 단위로 읽어들이는 통로
      // InputStream이 갖춰야 할 것 : 데이터 읽기, 특정 시점으로 되돌아가기, 얼마나 데이터가 남았는지 보여주기, 통로 끊기
      InputStream responseStream = connection.getInputStream();
      String response = drainStream(responseStream);
      responseStream.close();
      connection.disconnect();
      events.onHttpComplete(response);
    } catch (SocketTimeoutException e) {
      events.onHttpError("HTTP " + method + " to " + url + " timeout");
    } catch (IOException e) {
      events.onHttpError("HTTP " + method + " to " + url + " error: " + e.getMessage());
    }
  }

  // Return the contents of an InputStream as a String.
  private static String drainStream(InputStream in) {
    Scanner s = new Scanner(in, "UTF-8").useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
