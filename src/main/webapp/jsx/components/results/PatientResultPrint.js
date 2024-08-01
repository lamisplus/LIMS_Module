import React, { useEffect, useCallback, useState, useRef } from "react";

import { Link, useHistory, useLocation } from "react-router-dom";
import { Card } from "react-bootstrap";

import MatButton from "@material-ui/core/Button";
import HomeIcon from "@mui/icons-material/Home";
import Alert from "react-bootstrap/Alert";
import AddResultModal from "./AddResultModal";

import "../SampleCollection/sample.css";

import CachedIcon from "@mui/icons-material/Cached";

import axios from "axios";
import { toast } from "react-toastify";
import { token, url } from "../../../api";

import { makeStyles } from "@material-ui/core/styles";

import ReplyIcon from "@mui/icons-material/Reply";
import PrintIcon from "@mui/icons-material/Print";
import { useReactToPrint } from "react-to-print";
import PatientResult from "./PatientResult";

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

const PatientResultPrint = (props) => {
  const location = useLocation();

  const patientResults = location && location.state ? location.state.data : {};

  //console.log(patientResults)

  const [patientInfo, setPatientInfo] = useState({});

  const componentRef = useRef();

  const handlePrint = useReactToPrint({
    content: () => componentRef.current,
  });

  const loadInfo = useCallback(async () => {
    try {
      const response = await axios.get(
        `${url}lims/manifest-samples-info-by-sampleid/${patientResults.sampleID}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      console.log(response);
      setPatientInfo(response.data);
    } catch (e) {
      console.err(e);
    }
  }, [patientResults.sampleID]);

  useEffect(() => {
    loadInfo();
  }, [loadInfo]);

  return (
    <div>
      <Card>
        <Card.Body>
          <p style={{ textAlign: "right" }}>
            <MatButton
              variant="contained"
              color="success"
              startIcon={<PrintIcon />}
              onClick={handlePrint}
            >
              Print
            </MatButton>{" "}
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
              <br />
              <PatientResult samples={patientInfo} ref={componentRef} />
            </>
          }
        </Card.Body>
      </Card>
    </div>
  );
};

export default PatientResultPrint;
