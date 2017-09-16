
var maker = require("de.t2h/maker.js");

// ------------------------------------------------------------
// 1
// ------------------------------------------------------------

var args = {
  out:     "../lib/doc.js",
  add:     [ "./doc.js" ],
};

maker.build(args);

// ------------------------------------------------------------
// 2
// ------------------------------------------------------------

maker.copyFile("d:/web/proj/de.t2h/css/t2h.css", "../lib/t2h.css");
maker.copyFile("d:/web/proj/de.t2h/toc/toc.css", "../lib/toc.css");
maker.copyFile("tterm.css", "../lib/tterm.css");
