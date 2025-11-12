import React, { useEffect, useCallback, useState, useRef } from "react";
import { Link, useHistory } from "react-router-dom";
import { Card, ProgressBar } from "react-bootstrap";

import MatButton from "@material-ui/core/Button";
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
import AddIcon from "@mui/icons-material/Add";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
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
  const [status, setStatus] = useState(false);
  const [open, setOpen] = useState(false);
  const [percentage, setPercentage] = useState(0);
  const handleOpen = () => setOpen(true);
  const [download, setDownload] = useState(false);

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

  const getPCResults = useCallback(async () => {
    try {
      setPercentage(30);
      const serverId = JSON.parse(localStorage.getItem("configId"));
      setResults([]);
      if (manifestObj.id !== 0) {
        const response = await axios.get(
          `${url}lims/manifest-results/${manifestObj.id}/${serverId}`,
          { headers: { Authorization: `Bearer ${token}` } }
        );

        if (
          response.data.manifestID === null &&
          response.data.viralLoadTestReport === null
        ) {
          setStatus(true);
          toast.info("No Viral Laod sample results found for this Manifest", {
            position: toast.POSITION.TOP_RIGHT,
          });
        } else {
          setStatus(false);
          if (response.data.viralLoadTestReport !== null) {
            setPercentage(40);
            setResults(response.data.viralLoadTestReport);
            let limsResult = [];
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

                limsResult.push(result);
              }
            });
          }
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
      setStatus(true);
      toast.info("No Viral Laod sample results found for this Manifest", {
        position: toast.POSITION.TOP_RIGHT,
      });
    }
  }, [manifestObj.id]);

  useEffect(() => {
    loadConfig();
    // loadResults();
    getPCResults();
  }, [loadConfig, getPCResults]);

  const reload = (e) => {
    getPCResults();
  };

  const handleBulkDownload = async () => {
    try {
      setDownload(true);
      if (manifestObj.id !== 0) {
        console.log(manifestObj.id);
        const manifestId = manifestObj?.id;
        const configId = JSON.parse(localStorage.getItem("configId"));

        const response = await axios.get(
          `${url}lims/bulk-download?manifestId=${manifestId}&configId=${configId}&page=0&size=100`,
          {
            headers: { Authorization: `Bearer ${token}` },
            responseType: "blob",
          }
        );

        if (response.status === 200) {
          const contentDisposition = response.headers["content-disposition"];
          let fileName = `Lims_results_${new Date()
            .toISOString()
            .slice(0, 10)}.zip`;
          setDownload(false);
          if (contentDisposition) {
            const match = contentDisposition.match(/filename="?([^"]+)"?/);
            if (match?.[1]) {
              fileName = match[1];
            }
          }

          if (!fileName.toLowerCase().endsWith(".zip")) {
            fileName = fileName + ".zip";
          }

          // create download link
          const blob = new Blob([response.data], { type: "application/zip" });
          const webUrl = window.URL.createObjectURL(blob);
          const link = document.createElement("a");

          link.href = webUrl;
          link.setAttribute("download", fileName);
          document.body.appendChild(link);
          link.click();
          link.remove();

          // cleanup URL object
          window.URL.revokeObjectURL(webUrl);
        }
      }
    } catch (error) {
      console.error("Error downloading results:", error);
    }
  };

  return (
    <div>
      <Card>
        <Card.Body>
          {results.length === 0 && status === false ? (
            <p>
              <CircularProgress color="primary" /> connecting to LIMS server...
            </p>
          ) : (
            " "
          )}
          <p style={{ textAlign: "right" }}>
//            <MatButton
//              variant="contained"
//              color="dark"
//              className={classes.button}
//              startIcon={<ArrowDownwardIcon />}
//              onClick={handleBulkDownload}
//            >
//              Download Bulk results{" "}
//              {download && (
//                <span>
//                  <CircularProgress color="secondary" />
//                </span>
//              )}
//            </MatButton>
            <MatButton
              variant="contained"
              color="success"
              className={classes.button}
              startIcon={<CachedIcon />}
              onClick={reload}
            >
              Refresh
            </MatButton>

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
