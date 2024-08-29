import React, { useState, Fragment, useEffect } from "react";
import { makeStyles } from "@material-ui/core/styles";
import { Row, Col, Card, Tab, Tabs } from "react-bootstrap";
import SampleCollection from "./SampleCollection/SamplesCollection";
import SamplesTracker from "./SampleCollection/SampleTracker";
import ManifestList from "./manifest/ManifestList";
import { labObj } from "./sampleObj";
import Login from "./SampleCollection/Login";
import axios from "axios";
import { token, url } from "../../api";

const divStyle = {
  borderRadius: "2px",
  fontSize: 14,
};

const Home = (props) => {
  const [key, setKey] = useState("manifest-list");
  const [config, setConfig] = useState({});

  const urlTabs =
    props.location && props.location.state ? props.location.state : null;
  const [permissions, setPermissions] = useState([]);

  const userPermission = () => {
    axios
      .get(`${url}account`, { headers: { Authorization: `Bearer ${token}` } })
      .then((response) => {
        setPermissions(response.data.permissions);
      })
      .catch((error) => {});
  };

  useEffect(() => {
    userPermission();

    switch (urlTabs) {
      case "existing-manifest":
        return setKey("manifest-list");
      case "collect-sample":
        return setKey("collection");
      case "config":
        return setKey("config");
      case "sample-tracker":
        return setKey("tracker");
      default:
        return setKey("manifest-list");
    }
  }, [urlTabs]);

  return (
    <Fragment>
      <Row>
        <Col xl={12}>
          <Card style={divStyle}>
            <Card.Body>
              {/* <!-- Nav tabs --> */}
              <div className="custom-tab-1">
                <Tabs
                  id="controlled-tab-example"
                  activeKey={key}
                  onSelect={(k) => setKey(k)}
                  className="mb-3"
                >
                  <Tab eventKey="manifest-list" title="Manifest List">
                    <ManifestList config={config} setConfig={setConfig} />
                  </Tab>
                  {/* {permissions.includes("create_manifest") ||
                    (permissions.includes("all_permission") && (
                      <Tab eventKey="collection" title="Create Manifest">
                        <SampleCollection />
                      </Tab>
                    ))} */}
                  {/* {permissions.includes("create_manifest") || permissions.includes("view_manifest") ||
                    (permissions.includes("all_permission") && ( */}
                  <Tab eventKey="tracker" title="Samples Tracker">
                    <SamplesTracker />
                  </Tab>
                  {/* ))} */}
                  {permissions.includes("set_configuration") ||
                    (permissions.includes("all_permission") && (
                      <Tab eventKey="config" title="Configuration">
                        <Login
                          setKey={setKey}
                          config={config}
                          setConfig={setConfig}
                        />
                      </Tab>
                    ))}
                </Tabs>
              </div>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Fragment>
  );
};

export default Home;
