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

var authoring = require("d:/web/proj/de.t2h/authoring/authoring.es6")
var toc       = require("d:/web/proj/de.t2h/toc/toc.es6");

// ============================================================

// var prefix = ""; // "file:///android_asset/";

authoring.addMeta("viewport", "width=device-width");
authoring.addCss(prefix + "lib/t2h.css");
authoring.addCss(prefix + "lib/tterm.css");
authoring.addCss(prefix + "lib/toc.css");

authoring.checkImages();
