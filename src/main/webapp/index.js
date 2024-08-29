import React from "react";
import ReactDOM from "react-dom";
import App from "./App";
import { BrowserRouter } from "react-router-dom";

import reportWebVitals from "./reportWebVitals";
import SimpleReactLightbox from "simple-react-lightbox";
import ThemeContext from "./context/ThemeContext";

ReactDOM.render(
  <React.StrictMode>
    <SimpleReactLightbox>
      <BrowserRouter basename="/">
        <ThemeContext>
          <App />
        </ThemeContext>
      </BrowserRouter>
    </SimpleReactLightbox>
  </React.StrictMode>,
  document.getElementById("root")
);
reportWebVitals();
