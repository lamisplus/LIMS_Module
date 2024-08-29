import React, { useEffect, useCallback, useState } from "react";
import Stepper from "@mui/material/Stepper";
import Step from "@mui/material/Step";
import StepLabel from "@mui/material/StepLabel";
import Button from "@mui/material/Button";
import SampleOrderLists from "./SampleOrderLists";
import CreateAManifest from "../manifest/CreateAManifest";
import PrintManifest from "../manifest/PrintManifest";
import Stack from "@mui/material/Stack";
import axios from "axios";
import { token, url } from "../../../api";

function SampleCollection() {
  const [activeStep, setActiveStep] = React.useState(0);
  const [permissions, setPermissions] = useState([]);
  const [config, setConfig] = useState({});
  const [submitted, setSubmitted] = useState(0);
  const [previous, setPrevious] = useState(0);

  const loadConfig = useCallback(async () => {
    try {
      const response = await axios.get(`${url}lims/config`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setConfig(response.data);
    } catch (e) {
      console.log(e);
    }
  }, []);

  //console.log("steps", previous);

  useEffect(() => {
    userPermission();
    loadConfig();
  }, [loadConfig]);

  const userPermission = () => {
    axios
      .get(`${url}account`, { headers: { Authorization: `Bearer ${token}` } })
      .then((response) => {
        //console.log("permission", response.data.permissions)
        setPermissions(response.data.permissions);
      })
      .catch((error) => {});
  };

  const nextStep = () => {
    if (activeStep < 2) setActiveStep((currentStep) => currentStep + 1);
  };

  const previousStep = () => {
    if (activeStep !== 0) setActiveStep((currentStep) => currentStep - 1);
  };

  const renderContent = (step) => {
    switch (step) {
      case 0:
        return <SampleOrderLists setSubmitted={setSubmitted} />;
      case 1:
        return (
          <CreateAManifest
            setSubmitted={setSubmitted}
            setPrevious={setPrevious}
          />
        );
      case 2:
        return <PrintManifest />;
      default:
        return <div>Not Found</div>;
    }
  };
  return (
    <div>
      <br />
      <Stepper activeStep={activeStep}>
        <Step>
          <StepLabel>Select Collected Samples</StepLabel>
        </Step>
        <Step>
          <StepLabel>Complete & Send Manifest Form</StepLabel>
        </Step>
        <Step>
          <StepLabel>Print Manifest</StepLabel>
        </Step>
      </Stepper>
      <br />
      <>
        {permissions.includes("all_permission") ||
        permissions.includes("create_manifest") ||
        permissions.includes("view_manifest") ? (
          <Stack
            direction="row"
            spacing={2}
            m={1}
            display="flex"
            justifyContent="flex-end"
            alignItems="flex-end"
          >
            <Button
              variant="outlined"
              color="primary"
              onClick={() => previousStep()}
              disabled={
                Object.keys(config).length === 0
                  ? true
                  : false || activeStep === previous || activeStep === submitted
                  ? true
                  : false
              }
            >
              Previous Page
            </Button>{" "}
            <Button
              variant="outlined"
              color="primary"
              onClick={() => nextStep()}
              disabled={
                Object.keys(config).length === 0
                  ? true
                  : false || activeStep === submitted
                  ? true
                  : false
              }
            >
              Next Page
            </Button>
          </Stack>
        ) : (
          " "
        )}
        {renderContent(activeStep)}
      </>
    </div>
  );
}

export default SampleCollection;
