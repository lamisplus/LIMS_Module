import React, { useEffect, useState, useRef, useCallback } from "react";
import { Link, useHistory } from "react-router-dom";
import ProgressBar from "../SampleCollection/Progressbar";
import { Card } from "react-bootstrap";
import Alert from "react-bootstrap/Alert";

import "../SampleCollection/sample.css";
import axios from "axios";
import { token, url } from "../../../api";
import CircularProgress from "@mui/material/CircularProgress";

import { makeStyles } from "@material-ui/core/styles";
import ManifestPrint from "./ManifestPrint";
import MatButton from "@material-ui/core/Button";
import PrintIcon from "@mui/icons-material/Print";
import { useReactToPrint } from "react-to-print";
import ReplyIcon from "@mui/icons-material/Reply";
import ListAltIcon from "@mui/icons-material/ListAlt";
import SendIcon from "@mui/icons-material/Send";
import ConfigModal from "../SampleCollection/ConfigModal";
import QrCode2Icon from "@mui/icons-material/QrCode2";
import jsPDF from "jspdf";
import { toast } from "react-toastify";

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

const PrintManifest = (props) => {
  let history = useHistory();
  const sampleObj =
    history.location && history.location.state
      ? history.location.state.sampleObj
      : {};
  //console.log("props", sampleObj);

  const classes = useStyles();

  const [saved, setSaved] = useState(false);
  const [localStore, SetLocalStore] = useState([]);
  const [send, setSend] = useState(false);
  const [progress, setProgress] = useState(0);
  const [download, setDownload] = useState(false);
  const [open, setOpen] = useState(false);

  const loadConfig = useCallback(async () => {
    try {
      const response = await axios.get(`${url}lims/config`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      localStorage.setItem("configId", JSON.stringify(response.data.id));
    } catch (e) {
      console.log(e);
    }
  }, []);

  const handleOpen = () => setOpen(true);

  const toggleModal = () => setOpen(!open);

  const pageStyle = `@media print {
                  body {
                    background: white;
                    margin: 0;
                    padding: 0;
                    -webkit-print-color-adjust: exact;
                    print-color-adjust: exact;
                    font-size: 12px;
                  }

                  .result-container {
                    box-shadow: none;
                    max-width: 100%;
                    padding: 0;
                    margin: 0;
                  }

                  .report-header {
                    margin-bottom: 10px;
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

                  .report-table td,
                  .report-table th {
                    padding: 5px;
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

  useEffect(() => {
    loadConfig();
    const manifests = JSON.parse(localStorage.getItem("manifest"));
    if (manifests) {
      SetLocalStore(manifests);
      localStorage.removeItem("manifest");
    } else {
      SetLocalStore(sampleObj);
    }
  }, [loadConfig]);

  const handleFailure = (status) => {
    //setFailed(!failed);
  };

  const sendManifest = async (e) => {
    e.preventDefault();
    handleOpen();
    setProgress(10);
  };

  const handleProgress = (progessCount) => {
    setProgress(progessCount);
  };

  const getBarcode = async () => {
    const serverId = JSON.parse(localStorage.getItem("configId"));
    const manifestId = sampleObj.id;
    const count = sampleObj.sampleInformation.length;
    setDownload(true);
    await axios
      .post(
        `${url}barcodes/generate?configId=${serverId}&manifestId=${manifestId}&count=${count}`,
        {},
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      )
      .then((resp) => {
        let barcodes = resp.data.data;

        if (resp.status === 200) {
          handleDownloadPDF(barcodes);
        } else {
          toast.info(resp.data.message, {
            position: toast.POSITION.TOP_RIGHT,
          });
        }
        setDownload(false);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleDownloadPDF = async (barcodes) => {
    const pdf = new jsPDF("p", "mm", "a4");
    const pageWidth = pdf.internal.pageSize.getWidth();
    const pageHeight = pdf.internal.pageSize.getHeight();
    const margin = 10;
    const colWidth = (pageWidth - margin * 2) / 3;
    const rowHeight = 40;
    let x = margin;
    let y = margin;

    for (let i = 0; i < barcodes.length; i++) {
      const barcode = barcodes[i];
      const img = new Image();
      img.src = barcode.barcodeImage;

      await new Promise((resolve) => (img.onload = resolve));

      pdf.setLineWidth(0.1);
      pdf.setDrawColor(180, 180, 180);
      pdf.setLineDash([1, 1]);
      pdf.rect(x, y, colWidth - 2, rowHeight, "S");

      pdf.setFontSize(10);
      pdf.setTextColor(0, 0, 0);
      pdf.text(`Serial: ${barcode.serialNumber}`, x + 4, y + 8);
      pdf.addImage(img, "PNG", x + 4, y + 10, colWidth - 10, 20);

      x += colWidth;

      if ((i + 1) % 3 === 0) {
        x = margin;
        y += rowHeight + 5;
      }

      if (y + rowHeight > pageHeight - margin) {
        pdf.addPage();
        x = margin;
        y = margin;
      }
    }

    pdf.save("barcodes.pdf");
  };

  return (
    <>
      <Card>
        <Card.Body>
          {Object.keys(localStore).length === 0 ? (
            <Alert
              variant="danger"
              style={{ width: "100%", fontSize: "18px", textAlign: "center" }}
            >
              <b>Sample Manifest</b> not created yet. pls complete the manifest
              form.
            </Alert>
          ) : (
            <>
              <p style={{ textAlign: "right" }}>
                {localStore.manifestStatus === "Ready" ? (
                  <MatButton
                    variant="contained"
                    color="success"
                    className={classes.button}
                    startIcon={<SendIcon />}
                    disabled={!send ? false : true}
                    onClick={sendManifest}
                  >
                    Resend Manifest
                  </MatButton>
                ) : (""
//                  <MatButton
//                    variant="contained"
//                    color="primary"
//                    style={{
//                      backgroundColor: "#014d88",
//                      color: "#fff",
//                    }}
//                    startIcon={<QrCode2Icon />}
//                    onClick={getBarcode}
//                  >
//                    Generate Barcode{" "}
//                    {download && (
//                      <span>
//                        <CircularProgress color="secondary" />
//                      </span>
//                    )}
//                  </MatButton>
                )}

                <MatButton
                  variant="contained"
                  color="success"
                  className={classes.button}
                  startIcon={<PrintIcon />}
                  disabled={!send ? false : true}
                  onClick={handlePrint}
                >
                  Print
                </MatButton>

                <Link
                  color="inherit"
                  to={{
                    pathname: "/result",
                    state: { manifestObj: localStore },
                  }}
                >
                  <MatButton
                    variant="contained"
                    color="secondary"
                    className={classes.button}
                    style={{ backgroundColor: "#014d88", color: "#fff" }}
                    startIcon={<ListAltIcon />}
                  >
                    Results
                  </MatButton>
                </Link>

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
              {progress !== 0 ? (
                <>
                  <span>Sending manifest to PCR Lab</span>
                  <ProgressBar value={progress} />
                </>
              ) : (
                " "
              )}
              <ManifestPrint sampleObj={localStore} ref={componentRef} />
            </>
          )}
        </Card.Body>
      </Card>
      {open ? (
        <ConfigModal
          modalstatus={open}
          togglestatus={toggleModal}
          manifestsId={sampleObj.id}
          saved={saved}
          handleProgress={handleProgress}
          handleFailure={handleFailure}
        />
      ) : (
        " "
      )}
    </>
  );
};

export default PrintManifest;
