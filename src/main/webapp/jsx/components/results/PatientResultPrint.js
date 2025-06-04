import React, { useEffect, useCallback, useState, useRef } from "react";

import { Link, useHistory, useLocation } from "react-router-dom";
import { Card } from "react-bootstrap";

import MatButton from "@material-ui/core/Button";
import "../SampleCollection/sample.css";

import axios from "axios";
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

  //console.log(patientResults);

  const [patientInfo, setPatientInfo] = useState({});

  const pageStyle = `@media print {
                    body {
                      background: white;
                      margin: 0;
                      padding: 0;
                      -webkit-print-color-adjust: exact;
                      print-color-adjust: exact;
                      font-size: 14px;
                    }

                    .result-container {
                      box-shadow: none;
                      max-width: 100%;
                      padding: 0;
                      margin: 0;
                    }

                    .report-header {
                      display: flex;
                      justify-content: space-between;
                      align-items: center;
                      border-bottom: 2px solid #014d88;
                      padding-bottom: 8px;
                      margin-bottom: 15px;
                    }

                    .header-text {
                      flex: 1;
                    }

                    .report-title {
                      font-size: 18px;
                      font-weight: 600;
                      color: #014d88;
                      margin: 0;
                    }

                    .report-subtitle {
                      font-size: 14px;
                      color: #666;
                    }

                    .report-logo img {
                      width: 60px;
                      height: auto;
                      object-fit: contain;
                    }

                    .section {
                      margin-bottom: 10px;
                      border: 1px solid #014d88;
                      page-break-inside: avoid;
                    }

                    /* .report-table th {
                      background-color: #014d88 !important;
                      color: white !important;
                    } */

                    .report-table {
                      width: 100%;
                      border-collapse: collapse;
                      font-size: 12px;
                      margin-top: 5px;
                    }

                    .report-table th,
                    .report-table td {
                      padding: 6px 8px;
                      border: 1px solid #ccc;
                      text-align: left;
                      vertical-align: middle;
                    }

                    .report-footer {
                      text-align: right;
                      font-size: 10px;
                      color: #666;
                      margin-top: 20px;
                      page-break-inside: avoid;
                    }

                    @page {
                      size: A4 portrait;
                      margin: 10mm;
                    }
              }`;
  const componentRef = useRef();

  const handlePrint = useReactToPrint({
    content: () => componentRef.current,
    pageStyle,
  });

  const loadInfo = useCallback(async () => {
    let manifestSampleId = null;
    try {
      if (patientResults?.sampleID?.includes("/")) {
        manifestSampleId = patientResults?.sampleID?.replace(/\//g, "-");
      } else {
        manifestSampleId = patientResults?.sampleID;
      }
      const response = await axios.get(
        `${url}lims/manifest-samples-info-by-sampleid/${manifestSampleId}`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      //console.log(response);
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
