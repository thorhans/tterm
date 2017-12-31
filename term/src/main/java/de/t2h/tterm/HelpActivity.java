/*
 * Copyright (C) 2014 Thorbjørn Hansen.
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

package de.t2h.tterm;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Help.
 *
 * @author ThH
 */
public class HelpActivity extends Activity {

  WebView mWebView;
  
  @Override
  protected void onCreate (Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.help_activity);
      
      mWebView = new WebView(this);

      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);  // Enable JavaScript.
      settings.setBuiltInZoomControls(true);
      settings.setDisplayZoomControls(false);

      // Necessary since API 24 (7.0), otherwise you get a fatal exception when you click on a link. For
      // example, when you click on the link to `changelog.xhtml´, you get:
      //   android.os.FileUriExposedException: file:///android_asset/changelog.xhtml exposed beyond app
      //   through Intent.getData()
      //
      WebViewClient client = new WebViewClient() {
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, String url) {
              return false;
          }
      };
      mWebView.setWebViewClient(client);

      setContentView(mWebView);

      // LATER Is there some reason why this code is better than `loadUlr´?
      //
      //   try {
      //       InputStream fin = getAssets().open("1-index.xhtml");
      //       byte[] buffer = new byte[fin.available()];
      //       fin.read(buffer);
      //       fin.close();
      //       mWebView.loadDataWithBaseURL(
      //           /*baseUrl*/ "file:///android_asset/",
      //           /*data*/    new String(buffer),
      //           "application/xhtml+xml", "UTF-8", null);
      //   } catch(IOException e) {
      //       e.printStackTrace();
      //   }
      //
      mWebView.loadUrl("file:///android_asset/1-index.xhtml");
  }

  @Override
  public void onBackPressed() {
      if(mWebView.canGoBack())
          mWebView.goBack();
      else
          super.onBackPressed();
  }  
}
