import React, { useEffect, useState, useRef } from "react";
import { Link, useHistory } from "react-router-dom";
import ProgressBar from "../SampleCollection/Progressbar";
import { Card } from "react-bootstrap";
import Alert from "react-bootstrap/Alert";

import "../SampleCollection/sample.css";

import { makeStyles } from "@material-ui/core/styles";
import ManifestPrint from "./ManifestPrint";
import MatButton from "@material-ui/core/Button";
import PrintIcon from "@mui/icons-material/Print";
import { useReactToPrint } from "react-to-print";
import ReplyIcon from "@mui/icons-material/Reply";
import ListAltIcon from "@mui/icons-material/ListAlt";
import SendIcon from "@mui/icons-material/Send";
import ConfigModal from "../SampleCollection/ConfigModal";

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
  //console.log("props",sampleObj)
  const classes = useStyles();

  const [saved, setSaved] = useState(false);
  const [localStore, SetLocalStore] = useState([]);
  const [send, setSend] = useState(false);
  const [progress, setProgress] = useState(0);
  const [failed, setFailed] = useState(false);

  const [open, setOpen] = useState(false);

  const handleOpen = () => setOpen(true);

  const toggleModal = () => setOpen(!open);

  const componentRef = useRef();
  const handlePrint = useReactToPrint({
    content: () => componentRef.current,
  });

  useEffect(() => {
    const manifests = JSON.parse(localStorage.getItem("manifest"));
    if (manifests) {
      SetLocalStore(manifests);
      localStorage.removeItem("manifest");
    } else {
      SetLocalStore(sampleObj);
    }
  }, []);

  const sendManifest = async (e) => {
    e.preventDefault();
    handleOpen();
    setProgress(10);
  };

  const handleProgress = (progessCount) => {
    setProgress(progessCount);
  };

  const handleFailure = (status) => {
    //setFailed(!failed);
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
                    Send Manifest
                  </MatButton>
                ) : (
                  " "
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
