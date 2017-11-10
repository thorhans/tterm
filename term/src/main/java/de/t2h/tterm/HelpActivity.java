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

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

//T+{ ------------------------------------------------------------

/** Help.
 *
 * @author ThH
 */
public class HelpActivity extends Activity {

  WebView mWebView;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.help_activity);
      
      mWebView = new WebView(this);

      WebSettings settings = mWebView.getSettings();
      settings.setJavaScriptEnabled(true);  // Enable JavaScript.
      settings.setBuiltInZoomControls(true);
      settings.setDisplayZoomControls(false);
      
      setContentView(mWebView);
      try {
          InputStream fin = getAssets().open("1-index.xhtml");
          byte[] buffer = new byte[fin.available()];
          fin.read(buffer);
          fin.close();
          mWebView.loadDataWithBaseURL("file:///android_asset/", new String(buffer), 
              "application/xhtml+xml", "UTF-8", null);
      } catch (IOException e) {
          e.printStackTrace();
      }    
  }

  @Override
  public void onBackPressed() {
      if(mWebView.canGoBack())
          mWebView.goBack();
      else
          super.onBackPressed();
  }  
}
//T+} ------------------------------------------------------------
