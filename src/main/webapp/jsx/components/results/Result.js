import React, { useEffect, useCallback, useState, useRef } from "react";
import { Link, useHistory } from "react-router-dom";
import { Card } from "react-bootstrap";

import MatButton from "@material-ui/core/Button";
import HomeIcon from "@mui/icons-material/Home";
import Alert from "react-bootstrap/Alert";
import AddResultModal from "./AddResultModal";

import "../SampleCollection/sample.css";
import CircularProgress from "@mui/material/CircularProgress";
import CachedIcon from "@mui/icons-material/Cached";

import axios from "axios";
import { toast } from "react-toastify";
import { token, url } from "../../../api";

import { makeStyles } from "@material-ui/core/styles";

import ReplyIcon from "@mui/icons-material/Reply";
import { useReactToPrint } from "react-to-print";
import AddIcon from "@mui/icons-material/Add";
import PrintResults from "./PrintResults";

const useStyles = makeStyles((theme) => ({
  card: {
    margin: theme.spacing(20),
    display: "flex",
    flexDirection: "column",
    alignItems: "center",
  },
  form: {
    width: "100%", // Fix IE 11 issue.
    marginTop: theme.spacing(3),
  },
  submit: {
    margin: theme.spacing(3, 0, 2),
  },
  cardBottom: {
    marginBottom: 20,
  },
  Select: {
    height: 45,
    width: 350,
  },
  button: {
    margin: theme.spacing(1),
  },

  root: {
    "& > *": {
      margin: theme.spacing(1),
    },
  },
  input: {
    border: "2px solid #014d88",
    borderRadius: "0px",
    fontSize: "16px",
    color: "#000",
  },
  error: {
    color: "#f85032",
    fontSize: "11px",
  },
  success: {
    color: "#4BB543 ",
    fontSize: "11px",
  },
  inputGroupText: {
    backgroundColor: "#014d88",
    fontWeight: "bolder",
    color: "#fff",
    borderRadius: "0px",
  },
  label: {
    fontSize: "16px",
    color: "rgb(153, 46, 98)",
    fontWeight: "600",
  },
}));

const Result = (props) => {
  let history = useHistory();
  const manifestObj =
    history.location && history.location.state
      ? history.location.state.manifestObj
      : {};

  const classes = useStyles();
  const [loading, setLoading] = useState(true);
  const [results, setResults] = useState([]);

  const [open, setOpen] = useState(false);

  const handleOpen = () => setOpen(true);

  const toggleModal = () => setOpen(!open);

  const loadConfig = useCallback(async () => {
    try {
      const response = await axios.get(`${url}lims/config`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      localStorage.setItem("configId", JSON.stringify(response.data.id));
      setLoading(false);
    } catch (e) {
      toast.error("An error occurred while fetching config details", {
        position: toast.POSITION.TOP_RIGHT,
      });
      setLoading(false);
    }
  }, []);

  const componentRef = useRef();

  const loadResults = useCallback(async () => {
    try {
      const response = await axios.get(
        `${url}lims/results/manifests/${manifestObj.id}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setResults(response.data.results);
      setLoading(false);
    } catch (e) {
      setLoading(false);
    }
  }, [manifestObj.id]);

  const getPCResults = useCallback(async () => {
    try {
      const serverId = JSON.parse(localStorage.getItem("configId"));
      setResults([]);
      if (manifestObj.id !== 0) {
        const response = await axios.get(
          `${url}lims/manifest-results/${manifestObj.id}/${serverId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        console.log("lims", response.data.viralLoadTestReport);
        if (response.data.viralLoadTestReport !== null) {
          setResults(response.data.viralLoadTestReport);

          response.data.viralLoadTestReport.forEach((d) => {
            if (d.approvalDate !== "" && d.testResult !== "") {
              let result = {
                manifestRecordID: manifestObj.id,
                dateResultDispatched: d.dateResultDispatched,
                dateSampleReceivedAtPcrLab: d.dateSampleReceivedAtPcrLab,
                testResult: d.testResult,
                resultDate: d.resultDate,
                pcrLabSampleNumber: d.pcrLabSampleNumber,
                approvalDate: d.approvalDate,
                assayDate: d.assayDate,
                sampleTestable: d.sampleTestable,
                sampleStatus: d.sampleStatus,
                sampleID: d.sampleID,
                uuid: "",
                visitDate: d.visitDate,
                transferStatus: d.transferStatus,
                testedBy: d.transferStatus,
                approvedBy: d.approvedBy,
                dateTransferredOut: d.dateTransferredOut,
                reasonNotTested: d.reasonNotTested,
                otherRejectionReason: d.otherRejectionReason,
                sendingPCRLabID: d.sendingPCRLabID,
                sendingPCRLabName: d.sendingPCRLabName,
              };
              console.log("payload", result);
              axios
                .post(`${url}lims/results`, [result], {
                  headers: { Authorization: `Bearer ${token}` },
                })
                .then((resp) => {
                  //console.log("results saved", resp)
                });
            }
          });
        }
      } else {
        toast.success(
          "Sample results are currently been processed, check back in a bit",
          {
            position: toast.POSITION.TOP_RIGHT,
          }
        );
      }
      setLoading(false);
    } catch (e) {
      console.log(e.message);
    }
  }, [manifestObj.id]);

  useEffect(() => {
    loadConfig();
    loadResults();
    getPCResults();
  }, [loadConfig, loadResults, getPCResults]);

  const reload = (e) => {
    getPCResults();
  };

  // const handlePrint = useReactToPrint({
  //   content: () => componentRef.current,
  // });

  return (
    <div>
      <Card>
        <Card.Body>
          {results.length === 0 ? (
            <p>
              <CircularProgress color="primary" /> connecting to LIMS server...
            </p>
          ) : (
            " "
          )}
          <p style={{ textAlign: "right" }}>
            <MatButton
              variant="contained"
              color="dark"
              className={classes.button}
              startIcon={<AddIcon />}
              onClick={handleOpen}
            >
              Add Result
            </MatButton>
            <MatButton
              variant="contained"
              color="success"
              className={classes.button}
              startIcon={<CachedIcon />}
              onClick={reload}
            >
              Refresh
            </MatButton>
            {/* <MatButton
              variant="contained"
              color="success"
              className={classes.button}
              startIcon={<PrintIcon />}
              onClick={handlePrint}
            >
              Print
            </MatButton> */}

            <Link color="inherit" to={{ pathname: "/" }}>
              <MatButton
                variant="contained"
                color="primary"
                style={{
                  backgroundColor: "rgb(153, 46, 98)",
                  color: "#fff",
                }}
                startIcon={<ReplyIcon />}
              >
                back
              </MatButton>
            </Link>
          </p>
          <hr />
          {
            <>
              <Alert
                style={{
                  width: "100%",
                  fontSize: "20px",
                  backgroundColor: "#014d88",
                  color: "#fff",
                  textAlign: "center",
                }}
              >
                <Alert.Heading>PCR Sample Results</Alert.Heading>
              </Alert>
              <br />
              <PrintResults
                manifestObj={manifestObj}
                results={results}
                ref={componentRef}
              />
            </>
          }
        </Card.Body>
      </Card>
      {open ? (
        <AddResultModal
          modalstatus={open}
          togglestatus={toggleModal}
          manifestObj={manifestObj}
          results={results}
          reload={reload}
        />
      ) : (
        " "
      )}
    </div>
  );
};

export default Result;
