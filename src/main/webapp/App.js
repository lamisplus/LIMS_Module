import React from "react";
import {
  MemoryRouter as Router,
  Switch,
  Route,
} from "react-router-dom";

import { ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import "./vendor/bootstrap-select/dist/css/bootstrap-select.min.css";
import "./css/style.css";
import Home from './jsx/components/Home'

export default function App() {
  return (

      <div>
      <ToastContainer />
        <Switch>
          <Route path="/">
            <Home />
          </Route>
        </Switch>
      </div>
 
  );
}




