import React, { useEffect, useState, useRef, useCallback } from "react";
import { Link, useHistory } from "react-router-dom";
import { logo } from "../SampleCollection/pcr";
import { Card } from "react-bootstrap";

import "../SampleCollection/sample.css";
import axios from "axios";
import { token, url } from "../../../api";

import { makeStyles } from "@material-ui/core/styles";
import MatButton from "@material-ui/core/Button";
import PrintIcon from "@mui/icons-material/Print";
import { useReactToPrint } from "react-to-print";
import ReplyIcon from "@mui/icons-material/Reply";
import { Row, Table, Badge } from "reactstrap";

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

const Barcodes = (props) => {
  let history = useHistory();
  const manifestObj =
    history.location && history.location.state
      ? history.location.state.manifestObj
      : {};

  const classes = useStyles();

  const [localStore, SetLocalStore] = useState([]);
  const [serialNumbers, setSerialNumbers] = useState([]);

  const [send, setSend] = useState(false);

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

  const print = {
    width: "100%",
    borderCollapse: "collapse",
    fontFamily: "Arial",
  };

  const pageStyle = `@media print {
                  body {
                    background: white;
                    margin: 0;
                    padding: 0;
                    -webkit-print-color-adjust: exact;
                    print-color-adjust: exact;
                    font-size: 12px;
                  }

                  @page {
                    size: A4 portrait;
                    margin: 5mm;
                  }
                  
            }`;

  const componentRef = useRef();
  const handlePrint = useReactToPrint({
    content: () => componentRef.current,
    pageStyle,
  });

  const getBarcodes = async () => {
    await axios
      .get(`${url}barcodes/list?manifestId=${manifestObj.id}`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((resp) => {
        setSerialNumbers(resp.data.data);
      })
      .catch((ex) => {
        console.log(ex);
      });
  };

  useEffect(() => {
    loadConfig();
    getBarcodes();
    const manifests = JSON.parse(localStorage.getItem("manifest"));
    if (manifests) {
      SetLocalStore(manifests);
      localStorage.removeItem("manifest");
    } else {
      SetLocalStore(manifestObj);
    }
  }, [loadConfig]);

  return (
    <>
      <Card>
        <Card.Body>
          <>
            <p style={{ textAlign: "right" }}>
              <MatButton
                variant="contained"
                color="success"
                className={classes.button}
                startIcon={<PrintIcon />}
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
            </p>
            <hr />
            <div ref={componentRef}>
              <Row>
                <Table size="sm">
                  <tbody>
                    <tr>
                      <th scope="row"></th>
                      <th scope="row"></th>
                      <th scope="row"></th>
                      <th scope="row">
                        <h2 className="text-center">
                          NISRN SAMPLE BARCODES GENERATED
                        </h2>
                      </th>

                      <th scope="row">
                        <img
                          src={logo}
                          style={{ width: "80px", height: "80px" }}
                          alt=""
                        />
                      </th>
                    </tr>
                  </tbody>
                </Table>
              </Row>
              <br />
              <br />
              <Row>
                <Table bordered size="sm" style={print}>
                  <tbody>
                    <tr>
                      <th scope="row">Pick Up Date:</th>
                      <td>
                        {manifestObj.dateScheduledForPickup === null
                          ? " "
                          : manifestObj.dateScheduledForPickup?.replace(
                              "T",
                              " "
                            )}
                      </td>
                      <th scope="row">Destination:</th>
                      <td>{manifestObj.receivingLabName}</td>
                      <th scope="row">PCR Lab Number:</th>
                      <td>{manifestObj.receivingLabID}</td>
                    </tr>
                    <tr>
                      <th scope="row">Status:</th>
                      <td>{manifestObj.manifestStatus}</td>
                      <th scope="row">Manifest Id:</th>
                      <td>{manifestObj.manifestID}</td>
                      <th scope="row">Sample Temperature:</th>
                      <td>
                        {manifestObj.temperatureAtPickup === ""
                          ? "Not Provided"
                          : manifestObj.temperatureAtPickup}
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">Courier Name:</th>
                      <td>{manifestObj.courierRiderName}</td>
                      <th scope="row">Courier Contact:</th>
                      <td>{"+" + manifestObj.courierContact}</td>
                      <th scope="row">Test Type:</th>
                      <td>VL</td>
                    </tr>
                  </tbody>
                </Table>
              </Row>
              <Row>
                <Table striped bordered size="sm">
                  <thead style={{ backgroundColor: "#014d88", color: "#fff" }}>
                    <tr>
                      <th>Unique Number</th>
                      <th>Manifest ID</th>
                      <th>Sample ID</th>
                      <th>Barcode Serial</th>
                      <th>Status</th>
                      <th>Created By</th>
                      <th>Date Generated</th>
                    </tr>
                  </thead>
                  {serialNumbers.length === 0 ? (
                    <>
                      <br />
                      <h3
                        style={{
                          color: "blue",
                          textAlign: "center",
                        }}
                      >
                        No Barcodes Generated For this Manifest
                      </h3>
                    </>
                  ) : (
                    <tbody>
                      {serialNumbers &&
                        serialNumbers.map((data, i) => (
                          <tr key={i}>
                            <td>{data.uuid}</td>
                            <td>{data.manifestId}</td>

                            <td>{data.sampleId}</td>
                            <td>{data.serialNumber}</td>
                            <td>{data.status}</td>
                            <td>{data.createdBy}</td>
                            <td>{data.createdDate}</td>
                          </tr>
                        ))}
                    </tbody>
                  )}
                </Table>
              </Row>
            </div>
          </>
        </Card.Body>
      </Card>
    </>
  );
};

export default Barcodes;
